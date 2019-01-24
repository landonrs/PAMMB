package speechHandling;

public class SpeechCommandHandler {

    SpeechInterpreter interpreter;
    private ACTIVE_STATE currentState;
    boolean runningAssistantMode;

    public SpeechCommandHandler(SpeechInterpreter interpreter) {
        this.interpreter = interpreter;
        this.currentState = ACTIVE_STATE.IDLE;
        this.runningAssistantMode = false;
    }



    public void runAssistantMode() {
        runningAssistantMode = true;
        interpreter.startListening();
        while(runningAssistantMode) {
            String speechInput = interpreter.getTextFromSpeech();


            if(speechInput != null) {
                handleCommand(speechInput);
            }
        }

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


    public enum ACTIVE_STATE {
        IDLE,
        ACTIVATED,
        RUNNING_MACRO
    }

    public ACTIVE_STATE getCurrentState() {
        return currentState;
    }
}
