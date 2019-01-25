package speechHandling;

import java.util.concurrent.TimeUnit;

public class SpeechCommandHandler {

    SpeechInterpreter interpreter;
    private ACTIVE_STATE currentState;
    private static volatile boolean runningAssistantMode;

    public SpeechCommandHandler(SpeechInterpreter interpreter) {
        this.interpreter = interpreter;
        this.currentState = ACTIVE_STATE.IDLE;
        runningAssistantMode = false;
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
                handleCommand(speechInput);
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

    public void handleCommand(String speechInput) {
        // activate PAMM
        if(currentState == ACTIVE_STATE.IDLE && speechInput == "hey pam") {
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
