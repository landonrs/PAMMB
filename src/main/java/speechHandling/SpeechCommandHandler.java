package speechHandling;

import Audio.MediaPlayerUtil;
import db.SQLiteDbFacade;
import eventHandling.EventPerformer;
import eventHandling.EventRecorder;
import frontEnd.AssistantModeController;
import frontEnd.MacroSetterController;
import frontEnd.ViewLoader;
import javafx.application.Platform;
import macro.Macro;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SpeechCommandHandler {

    private static SpeechInterpreter interpreter;
    private static ACTIVE_STATE currentState;
    private static volatile boolean runningAssistantMode;
    private static volatile boolean runningCreateMode;
    private static volatile boolean startedVariableStep;
    // singleton instance to ensure that only one microphone is intitialized
    private static SpeechCommandHandler instance = null;

    private static File grammarFile;

    private static final String COMMANDLINE = "public <command> = [(please | run command)] (";
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

    // commands used when creating macros
    private static final String STOP_RECORDING_PHRASE = "finish recording";
    private static final String START_VAR_STEP_PHRASE = "start variable step";
    private static final String FINISH_VAR_STEP_PHRASE = "finish variable step";

    private SpeechCommandHandler(SpeechInterpreter someInterpreter) {
        interpreter = someInterpreter;
        currentState = ACTIVE_STATE.IDLE;
        runningAssistantMode = false;
        runningCreateMode = false;
        startedVariableStep = false;
        try {
            grammarFile = new File(getClass().getClassLoader().getResource("grammars/PAMM.gram").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    // For the Sphinx4 library on Windows OS, an error occurs if more than one
    // recognizer gets instantiated which breaks the program
    // to prevent this, we use a singleton instance of the SpeechCommandHandler
    // which ensures that only one instance will ever be initialized
    public static SpeechCommandHandler getInstance() {
        if (instance == null) {
            instance = new SpeechCommandHandler(new SphinxInterpreter());
            return instance;
        }
        else {
            return instance;
        }
    }

    public void runAssistantMode(AssistantModeController controller) {
        // reset our state from last time
        currentState = ACTIVE_STATE.IDLE;
        runningAssistantMode = true;
        interpreter.startListening();
        System.out.println("starting assistant mode");
        while(runningAssistantMode) {
            String speechInput = interpreter.getTextFromSpeech();
            interpreter.pauseListening();


            if(speechInput != null) {
                System.out.println("result: " + speechInput + " current state " + currentState);
                handleAssistantCommand(speechInput, controller);
            }

            interpreter.resumeListening();
        }

        System.out.println("Stopping assistant mode");
        interpreter.pauseListening();

    }

    public void handleAssistantCommand(String speechInput, AssistantModeController controller) {

        if(speechInput.equals(RETURN_PHRASE)){
            runningAssistantMode = false;
            controller.loadHomeView();
            return;
        }

        // activate PAMM
        if(currentState == ACTIVE_STATE.IDLE && speechInput.equals(ACTIVATE_PHRASE)) {
            currentState = ACTIVE_STATE.ACTIVATED;
            MediaPlayerUtil.playSound();
            controller.playActiviationAnimation();
            setAndClearDisplayText(speechInput, controller);

        }

        // show command list
        else if((currentState == ACTIVE_STATE.ACTIVATED || currentState == ACTIVE_STATE.CONTINUOUS_MODE)
        && speechInput.equals(SHOW_COMMANDS_PHRASE)) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ViewLoader.displayCommandList();
                }
            });
        }

        else if((currentState == ACTIVE_STATE.ACTIVATED || currentState == ACTIVE_STATE.CONTINUOUS_MODE)
                && (speechInput.equals(STOP_LISTENING) || speechInput.equals(NEVER_MIND_PHRASE))) {
            currentState = ACTIVE_STATE.IDLE;
            controller.dimCircle();
            setAndClearDisplayText(speechInput, controller);

        }

        else if(currentState == ACTIVE_STATE.ACTIVATED) {
            if(speechInput.equals(CONTINUOUS_PHRASE)) {
                currentState = ACTIVE_STATE.CONTINUOUS_MODE;
                controller.lightUpCircle();
                setAndClearDisplayText(speechInput, controller);

            }
            else {
                String macroName = getCommandFromSpeech(speechInput);
                Macro userMacro = SQLiteDbFacade.getInstance().loadMacro(macroName);
                if(userMacro != null) {
                    setAndClearDisplayText(speechInput, controller);
                    currentState = ACTIVE_STATE.RUNNING_MACRO;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            ViewLoader.hideCommandList();
                            ViewLoader.hidePrimaryStage();
                            EventPerformer.performMacro(userMacro);
                            currentState = ACTIVE_STATE.IDLE;
                            ViewLoader.showPrimaryStage();
                        }

                    });
                    // After macro has been performed, return to idle state
                    controller.dimCircle();
                }
                else
                    setAndClearDisplayText(UNKNOWNREPSONSE, controller);

            }
        }

        else if(currentState == ACTIVE_STATE.CONTINUOUS_MODE) {
            String macroName = getCommandFromSpeech(speechInput);
            Macro userMacro = SQLiteDbFacade.getInstance().loadMacro(macroName);
            if(userMacro != null) {
                setAndClearDisplayText(speechInput, controller);
                currentState = ACTIVE_STATE.RUNNING_MACRO;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ViewLoader.hideCommandList();
                        ViewLoader.hidePrimaryStage();
                        EventPerformer.performMacro(userMacro);
                        currentState = ACTIVE_STATE.CONTINUOUS_MODE;
                        ViewLoader.showPrimaryStage();
                        ViewLoader.displayCommandList();
                    }

                });
            }
            else
                setAndClearDisplayText(UNKNOWNREPSONSE, controller);
        }

    }


    private void setAndClearDisplayText(String speechInput, AssistantModeController controller){
        controller.displaySpeech(speechInput);
        // wait 2 seconds then clear screen
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        controller.clearViewText();

    }

    public void runCreateMode(MacroSetterController controller) {
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

    private void handleCreateCommand(String command, MacroSetterController controller) {
        switch (command) {
            case STOP_RECORDING_PHRASE:
                if (!startedVariableStep) {
                    controller.finishRecording();
                    runningCreateMode = false;
                }
                break;
            case START_VAR_STEP_PHRASE:
                if (!startedVariableStep) {
                    MediaPlayerUtil.playSound();
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


    public enum ACTIVE_STATE {
        IDLE,
        ACTIVATED,
        CONTINUOUS_MODE,
        RUNNING_MACRO
    }

    ACTIVE_STATE getCurrentState() {
        return currentState;
    }

    String getCommandFromSpeech(String speechInput) {
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

    public static void updateGrammar() throws IOException {
        List<String> commandNames = SQLiteDbFacade.getMacroNames();
        String newCommandGrammarList = COMMANDLINE;
        if(commandNames.isEmpty()){
            newCommandGrammarList += "<VOID> );";
        }
        else {
            for (int i = 0; i < commandNames.size(); i++) {
                if (i != commandNames.size() - 1) {
                    newCommandGrammarList += commandNames.get(i).toLowerCase() + " | ";
                } else {
                    newCommandGrammarList += commandNames.get(i).toLowerCase() + ");";
                }
            }
        }
        // read through the file until we get to the command line
        int position = 0;
        List<String> lines = Files.readAllLines(grammarFile.toPath());
        for(String line: lines){
            // System.out.println(position + " " + line);
            if(line.toLowerCase().indexOf(COMMANDPHRASE.toLowerCase()) != -1){
                break;
            }
            position++;
        }
        // overwrite commands with updated list
        lines.set(position, newCommandGrammarList);
        // update the grammar file
        Files.write(grammarFile.toPath(), lines);
    }

}
