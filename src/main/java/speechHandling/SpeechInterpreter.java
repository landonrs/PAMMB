package speechHandling;

public interface SpeechInterpreter {

    String getTextFromSpeech();

    void startListening();

    void pauseListening();

    void resumeListening();
}
