/* PAMM: Personal Assistant Macro Maker.
 * Copyright (C) 2019 Landon Shumway.
 * https://github.com/landonrs/PAMMB
 *
 * PAMM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PAMM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package speechHandling;

import Audio.MediaPlayerUtil;
import db.SQLiteDbFacade;
import eventHandling.EventPerformer;
import eventHandling.EventRecorder;
import frontEnd.AssistantModeController;
import frontEnd.MacroSetterController;
import frontEnd.ViewLoader;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import macro.Macro;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SpeechCommandHandler {

    private static SpeechInterpreter interpreter;
    private static ACTIVE_STATE currentState;
    private static volatile boolean runningAssistantMode;
    private static volatile boolean runningCreateMode;
    private static volatile boolean startedVariableStep;
    // when set to true, PAMM asks for confirmation before running macros
    private static volatile boolean confirmationMode;
    // used to determine if fields need to be reinitialized whenever the grammar is updated
    private static volatile boolean grammarUpdated = false;

    // used to make sure speechRecognition has updated grammar before using it
    private static CompletableFuture<Boolean> updated = null;
    // verifies that microphone has finished listening in assistant mode
    private static CompletableFuture<Boolean> assistantModeFinished = null;
    // this stage is used to prompt the user to speak if the microphone is still being used in
    // assistant mode
    private static Stage microphoneTestStage = ViewLoader.generateDialog("views/microphoneTestView.fxml");
    static {
        microphoneTestStage.initStyle(StageStyle.UNDECORATED);
    }

    // this counter is used to track how many unrecognized commands have been heard in a row
    // if the program fails to process three commands in a row, we display the command list so the user can
    // run a command manually
    private static int unrecognizedCount = 0;
    // the number of errors that can occur in a row before we implement fail safe command display
    private static final int MAX_ERROR_FAIL_SAFE = 3;


//    private static final String GRAMMAR_PATH = System.getenv("LOCALAPPDATA") + "\\PAMM\\data\\PAMM.gram";
//    // used to locate grammar file used by sphinx interpreter
//    public static final String GRAMMAR_DIR = System.getenv("LOCALAPPDATA") + "\\PAMM\\data";


    private static final String GRAMMAR_PATH = (new File(".").getAbsolutePath()) + "/PAMM.gram";
    // used to locate grammar file used by sphinx interpreter
    public static final String GRAMMAR_DIR = (new File(".").getAbsolutePath());


    // used to find commands when grammar file is updated
    private static final String COMMANDLINE = "public <command> = [(please | (run command))] (";
    private static final String COMMANDPHRASE = "run command";
    private static final String POLITEPHRASE = "please";
    private static final String UNKNOWNREPSONSE = "Command not recognized";

    // commands used in assistant mode
    private static final String ACTIVATE_PHRASE = "hey there pam";
    private static final String SHOW_COMMANDS_PHRASE = "show my commands";
    private static final String STOP_LISTENING = "stop listening";
    private static final String NEVER_MIND_PHRASE = "never mind";
    private static final String CONTINUOUS_PHRASE = "turn on continuous mode";
    private static final String RETURN_PHRASE = "return to menu";
    private static final String CANCEL_PHRASE = "cancel command";
    private static final String CONFIRM_PHRASE = "yes";
    private static final String DENY_PHRASE = "no";
    private static final String COMMAND_PHRASE = "(optional: [please] or [run command]) <macro name>";

    // text to notify user when to start speaking
    private static final String READY_PHRASE = "speak command now";
    private static final String ACTIVATE_INSTRUCTION = "Activate me with 'hey there PAMM'";

    // used to display system commands to user
    private static final String[] SYSTEM_COMMANDS = {ACTIVATE_PHRASE, SHOW_COMMANDS_PHRASE, STOP_LISTENING, NEVER_MIND_PHRASE,
    CONTINUOUS_PHRASE, RETURN_PHRASE, CANCEL_PHRASE, COMMAND_PHRASE};

    // commands used when creating macros
    public static final String STOP_RECORDING_PHRASE = "finish recording";
    private static final String START_VAR_STEP_PHRASE = "start variable step";
    private static final String FINISH_VAR_STEP_PHRASE = "finish variable step";

    private static void initializeFields(SpeechInterpreter someInterpreter) {

        // make sure directory exists for storing app data
        Path path = Paths.get(GRAMMAR_DIR);
        //if directory exists?
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                //fail to create directory
                e.printStackTrace();
            }
        }
        //create grammar file if it does not exist
        File gramFile = new File(GRAMMAR_PATH);
        try {
            if(gramFile.createNewFile()){
                System.out.println("created new grammar file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        interpreter = someInterpreter;
        currentState = ACTIVE_STATE.IDLE;
        runningAssistantMode = false;
        runningCreateMode = false;
        startedVariableStep = false;
    }


    /**
     * sets or resets fields and configures new interpreter instance
     *
     * Whenever the user saves, deletes, or edits a macro name, the grammar
     * file gets updated {@see #updateGrammar}. In order for the interpreter
     * to recognize these new names, it must be reconfigured each time the
     * grammar file is updated.
     */
    static void initialize() {
        if (grammarUpdated) {
            initializeFields(new SphinxInterpreter());
            grammarUpdated = false;
        }
    }

    public static List getSystemCommandNames() {
        return Arrays.asList(SYSTEM_COMMANDS);
    }

    /**
     * This method runs while the user is in the Assistant mode view
     *
     * @param controller - the controller used to display info to the user
     */
    public static void runAssistantMode(AssistantModeController controller) {
        // reset our state from previous session
        currentState = ACTIVE_STATE.IDLE;
        unrecognizedCount = 0;
        confirmationMode = false;
        runningAssistantMode = true;
        assistantModeFinished = new CompletableFuture<>();

        interpreter.startListening();
        System.out.println("starting assistant mode");
        while(runningAssistantMode) {
            String speechInput = interpreter.getTextFromSpeech();
            interpreter.pauseListening();


            if(speechInput != null && runningAssistantMode) {
                System.out.println("result: " + speechInput + " current state " + currentState);
                handleAssistantCommand(speechInput, controller, false);
            }

            interpreter.resumeListening();
        }

        System.out.println("Stopping assistant mode");
        interpreter.pauseListening();
        assistantModeFinished.complete(true);
        Platform.runLater(() -> microphoneTestStage.hide());

    }

    /**
     * This method tracks the state of the assistant mode and handles valid speech requests from
     * the user.
     *
     * @param speechInput - the speech request that was captured by the interpreter
     * @param controller - the assistant mode controller, used to display speech requests back to user
     * @param manualMode set to true if user is invoking this command by selecting a button through the GUI
     */
    public static void handleAssistantCommand(String speechInput, AssistantModeController controller, boolean manualMode) {

        if(speechInput.equals(RETURN_PHRASE) &&
                (currentState == ACTIVE_STATE.ACTIVATED || currentState == ACTIVE_STATE.CONTINUOUS_MODE)){
            runningAssistantMode = false;
            if(ViewLoader.listStageOpen) {
                Platform.runLater(() -> ViewLoader.hideCustomCommandList());
            }
            Platform.runLater(() -> ViewLoader.hideSystemCommands());
            controller.loadHomeView();
            return;
        }

        //cancel commands if running
        if (speechInput.equals(CANCEL_PHRASE) && currentState == ACTIVE_STATE.RUNNING_MACRO) {
            EventPerformer.stopMacro();
        }

        // activate PAMM
        if(currentState == ACTIVE_STATE.IDLE && speechInput.equals(ACTIVATE_PHRASE)) {
            currentState = ACTIVE_STATE.ACTIVATED;
            MediaPlayerUtil.playActivationSound();
            controller.playActiviationAnimation();
            Platform.runLater(() -> ViewLoader.showPrimaryStage());
            setAndClearDisplayText("hey there PAMM", controller);
            setDisplayText(READY_PHRASE, controller);

        }

        // show command list
        else if((currentState == ACTIVE_STATE.ACTIVATED || currentState == ACTIVE_STATE.CONTINUOUS_MODE)
        && speechInput.equals(SHOW_COMMANDS_PHRASE)) {
            Platform.runLater(() -> ViewLoader.displayCommandList(controller));
        }

        else if((currentState == ACTIVE_STATE.ACTIVATED || currentState == ACTIVE_STATE.CONTINUOUS_MODE)
                && (speechInput.equals(STOP_LISTENING) || speechInput.equals(NEVER_MIND_PHRASE))) {
            currentState = ACTIVE_STATE.IDLE;
            controller.dimCircle();
            setAndClearDisplayText(speechInput, controller);
            Platform.runLater(() -> {
                if(ViewLoader.listStageOpen){
                    ViewLoader.hideCustomCommandList();
                }
                ViewLoader.minimizePrimaryStage();
                ViewLoader.hideSystemCommands();

            });

        }

        else if(currentState == ACTIVE_STATE.ACTIVATED) {
            if(speechInput.equals(CONTINUOUS_PHRASE)) {
                currentState = ACTIVE_STATE.CONTINUOUS_MODE;
                controller.lightUpCircle();
                setAndClearDisplayText(speechInput, controller);
                setDisplayText(READY_PHRASE, controller);
            }
            else {
                String macroName = getCommandFromSpeech(speechInput);
                Macro userMacro = SQLiteDbFacade.getInstance().loadMacro(macroName);
                if(userMacro != null) {
                    if(confirmationMode && !manualMode) {
                        setAndClearDisplayText(macroName + "?", controller);
                        // if user says no, leave without performing command
                        if(!runConfirmationMode(controller)){
                            return;
                        }
                    }
                    setAndClearDisplayText(speechInput, controller);
                    currentState = ACTIVE_STATE.RUNNING_MACRO;
                    // we recognized the command, reset error counter
                    unrecognizedCount = 0;
                    Platform.runLater(() -> {
                        if(ViewLoader.listStageOpen) {
                            ViewLoader.hideCustomCommandList();
                        }
                        ViewLoader.hideSystemCommands();
                        ViewLoader.hidePrimaryStage();
                        EventPerformer.performMacro(userMacro);
                        // After macro has been performed, return to idle state and wait
                        currentState = ACTIVE_STATE.IDLE;
                        ViewLoader.showPrimaryStage();
                        controller.displaySpeechOnPlatThread(ACTIVATE_INSTRUCTION);
                    });

                    // set look of view for when the macro is completed in the other thread
                    controller.dimCircle();

                }
                else {
                    checkFailSafe(controller);
                    setDisplayText(READY_PHRASE, controller);
                }

            }
        }

        else if(currentState == ACTIVE_STATE.CONTINUOUS_MODE) {
            String macroName = getCommandFromSpeech(speechInput);
            Macro userMacro = SQLiteDbFacade.getInstance().loadMacro(macroName);
            if(userMacro != null) {
                if(confirmationMode && !manualMode) {
                    setAndClearDisplayText(macroName + "?", controller);
                    // if user says no, leave without performing command
                    if(!runConfirmationMode(controller)){
                        return;
                    }
                }
                setAndClearDisplayText(speechInput, controller);
                currentState = ACTIVE_STATE.RUNNING_MACRO;
                unrecognizedCount = 0;
                Platform.runLater(() -> {
                    if(ViewLoader.listStageOpen) {
                        ViewLoader.hideCustomCommandList();
                    }
                    // if system commands are visible, hide the dialog
                    ViewLoader.hideSystemCommands();

                    ViewLoader.hidePrimaryStage();
                    EventPerformer.performMacro(userMacro);
                    currentState = ACTIVE_STATE.CONTINUOUS_MODE;
                    ViewLoader.showPrimaryStage();
                    controller.displaySpeechOnPlatThread("Macro complete");
                    // redisplay commands list if user had it open
                    ViewLoader.showCustomCommandList();
                    // give user time to read message
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    controller.displaySpeechOnPlatThread(READY_PHRASE);
                });
            }
            else {
                checkFailSafe(controller);
                setDisplayText(READY_PHRASE, controller);
            }
        }

    }

    /**
     * confirms with the user before running any macros
     *
     * if the user's microphone does not handle background noise well, the
     * program will pick up false positives and run unwanted macros, to help
     * minimize this risk, the confirmation mode has PAMM first confirm with
     * the user that the macro name was heard correctly
     *
     * @param controller
     * @return true is user says yes, false if no
     */
    private static boolean runConfirmationMode(AssistantModeController controller){
        interpreter.resumeListening();
        String response = "";
        do {
            setDisplayText("yes or no", controller);
            response = interpreter.getTextFromSpeech();
            if(response.equals(DENY_PHRASE)) {
                interpreter.pauseListening();
                setAndClearDisplayText(response, controller);
                return false;
            }
            else if(response.equals(CONFIRM_PHRASE)) {
                interpreter.pauseListening();
                setAndClearDisplayText(response, controller);
                return true;
            }
        } while(!response.equals(CONFIRM_PHRASE) && !response.equals(DENY_PHRASE));

        return false;
    }


    /**
     * Provides the user with a fail safe option for running macros in assistant mode
     *
     * if the program cannot recognize a command a certain number of times
     * in a row, this command checks to see if the command list needs to
     * be displayed so the user can select a command manually.
     *
     * @param controller - the controller we pass to the command list so it can process the command
     */
    private static void checkFailSafe(AssistantModeController controller) {
        setAndClearDisplayText(UNKNOWNREPSONSE, controller);
        unrecognizedCount++;
        System.out.println("checking fail safe, error count is " + unrecognizedCount);
        if(unrecognizedCount >= MAX_ERROR_FAIL_SAFE){
            unrecognizedCount = 0;
            Platform.runLater(() -> ViewLoader.displayCommandList(controller));
        }
    }

    public static void stopAssistantMode() {
        runningAssistantMode = false;
        currentState = ACTIVE_STATE.STOPPED;
    }


    private static void setDisplayText(String message, AssistantModeController controller) {

        controller.displaySpeech(message);
    }

    private static void setAndClearDisplayText(String speechInput, AssistantModeController controller){
        controller.displaySpeech(speechInput);
        // wait 2 seconds then clear screen
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        controller.clearViewText();

    }

    /**
     * This method runs whenever the user is recording new macros
     *
     * @param controller - the controller that is used to create the recorded macros
     */
    public static void runCreateMode(MacroSetterController controller) {
        runningCreateMode = true;
        interpreter.startListening();
        System.out.println("starting macro create mode");
        while(runningCreateMode) {
            String speechInput = interpreter.getTextFromSpeech();
            interpreter.pauseListening();

            if(speechInput != null) {
                System.out.println("result: " + speechInput);
                handleCreateCommand(speechInput, controller);
                if(!runningCreateMode) {
                    System.out.println("Stopping create mode");
                    return;
                }
            }

            interpreter.resumeListening();
        }

        System.out.println("Stopping create mode");
        interpreter.pauseListening();
    }

    public static void handleCreateCommand(String command, MacroSetterController controller) {
        switch (command) {
            case STOP_RECORDING_PHRASE:
                if (!startedVariableStep) {
                    controller.finishRecording();
                    runningCreateMode = false;
                }
                break;
            case START_VAR_STEP_PHRASE:
                if (!startedVariableStep) {
                    MediaPlayerUtil.playActivationSound();
                    EventRecorder.ignoreInput();
                    startedVariableStep = true;
                }
                break;
            case FINISH_VAR_STEP_PHRASE:
                if (startedVariableStep) {
                    startedVariableStep = false;
                    controller.getVariableStepName();
                }
                break;
        }
    }

    /**
     * determines whether PAMM will ask for confirmation before running macros
     *
     * @param selected - the state of the checkbox in the assistant mode view
     */
    public static void setConfirmationMode(boolean selected) {
        confirmationMode = selected;
    }


    public enum ACTIVE_STATE {
        IDLE,
        ACTIVATED,
        CONTINUOUS_MODE,
        RUNNING_MACRO,
        STOPPED
    }

    static String getCommandFromSpeech(String speechInput) {
        String command = "";
        if(speechInput.contains(COMMANDPHRASE)) {
            // the macro name starts after the substr "run command "
            command = speechInput.substring(speechInput.indexOf(COMMANDPHRASE) + COMMANDPHRASE.length() + 1);
            return command;
        }
        else if(speechInput.contains(POLITEPHRASE)) {
            // the macro name starts after the substr "please "
            command = speechInput.substring(speechInput.indexOf(POLITEPHRASE) + POLITEPHRASE.length() + 1);
            return command;
        }
        else
            // command phrase not included, return entire speech input
            return speechInput;
    }

    /**
     * rewrites the grammar file with updated macro names
     *
     * @throws IOException
     */
    private static void updateGrammar() throws IOException {
        List<String> commandNames = SQLiteDbFacade.getMacroNames();
        //create reader for grammar file
        BufferedReader grammarReader = new BufferedReader(new InputStreamReader(SpeechCommandHandler.class
                .getClassLoader().getResourceAsStream("grammars/PAMM.gram")));
        List<String> lines = new ArrayList<>();
        StringBuilder newCommandGrammarList = new StringBuilder(COMMANDLINE);
        if(commandNames.isEmpty()){
            newCommandGrammarList.append("<VOID> );");
        }
        else {
            for (int i = 0; i < commandNames.size(); i++) {
                // each command name is surrounded with parentheses to decrease false positives
                if (i != commandNames.size() - 1) {
                    newCommandGrammarList.append("(" + commandNames.get(i).toLowerCase()).append(") | ");
                } else {
                    newCommandGrammarList.append("(" + commandNames.get(i).toLowerCase()).append("));");
                }
            }
        }
        // read through the file until we get to the command line
        int position = 0;
        while(grammarReader.ready()) {
            String line = grammarReader.readLine();
            if(line.contains(COMMANDPHRASE)){
                System.out.println("found command phrase");
                lines.add(line);
                break;
            }
            lines.add(line);
            position++;
        }
        // overwrite commands with updated list
        lines.set(position, newCommandGrammarList.toString());
        // update the grammar file
        System.out.println(URLDecoder.decode(GRAMMAR_PATH, "UTF-8"));
        PrintWriter out = new PrintWriter(URLDecoder.decode(GRAMMAR_PATH, "UTF-8"));
        for (String line: lines){
            out.println(line);
        }
        out.close();

        grammarUpdated = true;

    }

    /**
     * this method is called whenever the user makes a change to the list of created macros
     *
     * The interpreter is reconfigured with the new grammar file so it will be
     * able to recognize new command phrases.
     */
    public static void updateSpeechRecognition(){
        updated = new CompletableFuture<Boolean>();

        CompletableFuture updating = CompletableFuture.runAsync(() -> {
            try {
                SpeechCommandHandler.updateGrammar();
            } catch (IOException e) {
                e.printStackTrace();
            }
            SpeechCommandHandler.initialize();
            updated.complete(true);
        });

    }

    /**
     * makes sure speech recogniion is up to date and ready to be used.
     *
     * Program will hang until the completableFuture is finished
     *
     * @return true once the update is complete
     */
    public static boolean isUpdated(){
        System.out.println("checking if updating is complete");
        try {
            return updated.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This function is used to verify that the speech recognition is not being used
     * for assistant mode.
     *
     * Because the microphone cannot be interrupted when in use, there is a possibility that
     * the assistant mode can be closed with the microphone running. If this happens the user
     * must clear the microphone by making an utterance before using the microphone on another
     * thread.
     * @return
     */
    public static boolean runningAssistantMode() {
        // if user runs create mode before assistant mode
        if(assistantModeFinished == null){
            return false;
        }

        return !assistantModeFinished.isDone();
    }

    public static void runMicrophoneTest(){
        // wait until microphone is cleared
        microphoneTestStage.showAndWait();
    }

}
