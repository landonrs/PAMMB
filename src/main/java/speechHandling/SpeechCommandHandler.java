package speechHandling;

import eventHandling.EventRecorder;

import java.util.concurrent.TimeUnit;

public class SpeechCommandHandler {

    private static SpeechInterpreter interpreter;
    private static ACTIVE_STATE currentState;
    private static volatile boolean runningAssistantMode;
    private static volatile boolean runningCreateMode;
    //singleton instance to ensure that only one microphone is intitialized
    private static SpeechCommandHandler instance = null;

    private SpeechCommandHandler(SpeechInterpreter interpreter) {
        interpreter = interpreter;
        currentState = ACTIVE_STATE.IDLE;
        runningAssistantMode = false;
        runningCreateMode = false;
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

    public void runAssistantMode() {
        runningAssistantMode = true;
        interpreter.startListening();
        System.out.println("starting assistant mode");
        while(runningAssistantMode) {
            String speechInput = interpreter.getTextFromSpeech();
            interpreter.pauseListening();


            if(speechInput != null) {
                System.out.println("result: " + speechInput + " running assistant " + runningAssistantMode);
                handleAssistantCommand(speechInput);
            }

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            interpreter.resumeListening();
        }

        System.out.println("Stopping assistant mode");
        interpreter.pauseListening();

    }

    public void handleAssistantCommand(String speechInput) {
        // activate PAMM
        if(currentState == ACTIVE_STATE.IDLE && speechInput == "listen up pam") {
            currentState = ACTIVE_STATE.ACTIVATED;
        }

        else if(currentState == ACTIVE_STATE.ACTIVATED && speechInput == "stop listening") {
            currentState = ACTIVE_STATE.IDLE;
        }

        else if(currentState == ACTIVE_STATE.ACTIVATED) {
            //checkCommandInDB(speechInput);
        }

    }

    public void stopAssistantMode() {
        runningAssistantMode = false;
    }

    public void runCreateMode() {
        runningCreateMode = true;
        interpreter.startListening();
        System.out.println("starting macro create mode");
        while(runningCreateMode) {
            String speechInput = interpreter.getTextFromSpeech();
            interpreter.pauseListening();

            if(speechInput != null) {
                System.out.println("result: " + speechInput);
                handleCreateCommand(speechInput);
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

    private void handleCreateCommand(String command) {
        if(command.equals("stop recording") ){
            EventRecorder.stopRecording();
            runningCreateMode = false;
        }
        else if(command.equals("create variable step")) {
            EventRecorder.createVariableStep();
        }
    }


    public enum ACTIVE_STATE {
        IDLE,
        ACTIVATED,
        RUNNING_MACRO
    }

    public ACTIVE_STATE getCurrentState() {
        return currentState;
    }

    public void setRunningAssistantMode(boolean runningAssistantMode) {
        SpeechCommandHandler.runningAssistantMode = runningAssistantMode;
    }
}
