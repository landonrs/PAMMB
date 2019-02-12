package speechHandling;

import db.SQLiteDbFacade;
import eventHandling.EventPerformer;
import eventHandling.EventRecorder;
import frontEnd.AssistantModeController;
import frontEnd.MacroSetterController;
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
    // singleton instance to ensure that only one microphone is intitialized
    private static SpeechCommandHandler instance = null;

    private static File grammarFile;

    private static final String COMMANDLINE = "public <command> = [please] (run command) (";
    private static final String COMMANDPHRASE = "run command";
    private static final String UNKNOWNREPSONSE = "Command not recognized";

    private SpeechCommandHandler(SpeechInterpreter someInterpreter) {
        interpreter = someInterpreter;
        currentState = ACTIVE_STATE.IDLE;
        runningAssistantMode = false;
        runningCreateMode = false;
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
        runningAssistantMode = true;
        interpreter.startListening();
        System.out.println("starting assistant mode");
        while(runningAssistantMode) {
            String speechInput = interpreter.getTextFromSpeech();
            interpreter.pauseListening();


            if(speechInput != null) {
                System.out.println("result: " + speechInput + " running assistant " + runningAssistantMode);
                handleAssistantCommand(speechInput, controller);
            }

//            try {
//                TimeUnit.SECONDS.sleep(2);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            interpreter.resumeListening();
        }

        System.out.println("Stopping assistant mode");
        interpreter.pauseListening();

    }

    public void handleAssistantCommand(String speechInput, AssistantModeController controller) {
        // activate PAMM
        if(currentState == ACTIVE_STATE.IDLE && speechInput.equals("listen up pam")) {
            currentState = ACTIVE_STATE.ACTIVATED;
            controller.displaySpeech(speechInput);
            controller.playActiviationAnimation();
        }

        else if(currentState == ACTIVE_STATE.ACTIVATED && speechInput.equals("stop listening")) {
            currentState = ACTIVE_STATE.IDLE;
            controller.displaySpeech(speechInput);
            controller.dimCircle();
        }

        else if(currentState == ACTIVE_STATE.ACTIVATED) {
            if(speechInput.equals("run continuous mode")) {
                currentState = ACTIVE_STATE.CONTINUOUS_MODE;
                controller.displaySpeech(speechInput);
                controller.lightUpCircle();
            }
            else {
                String macroName = getCommandFromSpeech(speechInput);
                Macro userMacro = SQLiteDbFacade.getInstance().loadMacro(macroName);
                if(userMacro != null) {
                    controller.displaySpeech(speechInput);
                    EventPerformer.performMacro(userMacro);
                    // After macro has been performed, return to idle state
                    currentState = ACTIVE_STATE.IDLE;
                    controller.dimCircle();
                }
                else
                    controller.displaySpeech(UNKNOWNREPSONSE);

            }
        }

        else if(currentState == ACTIVE_STATE.CONTINUOUS_MODE && speechInput.equals("stop listening")) {
            currentState = ACTIVE_STATE.IDLE;
            controller.displaySpeech(speechInput);
            controller.dimCircle();
        }

        else if(currentState == ACTIVE_STATE.CONTINUOUS_MODE) {
            String macroName = getCommandFromSpeech(speechInput);
            Macro userMacro = SQLiteDbFacade.getInstance().loadMacro(macroName);
            if(userMacro != null) {
                controller.displaySpeech(speechInput);
                EventPerformer.performMacro(userMacro);
            }
            else
                controller.displaySpeech(UNKNOWNREPSONSE);
        }

    }

    public void stopAssistantMode() {
        runningAssistantMode = false;
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
        if(command.equals("stop recording") ){
            EventRecorder.stopRecording();
            runningCreateMode = false;
        }
        else if(command.equals("create variable step")) {
            controller.getVariableStep();
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
        if(speechInput.indexOf(COMMANDPHRASE) != -1) {
            // the macro name starts after the substr "run command "
            command = speechInput.substring(speechInput.indexOf(COMMANDPHRASE) + COMMANDPHRASE.length() + 1);
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
