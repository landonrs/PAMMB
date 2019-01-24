package speechHandling;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

import java.io.IOException;

public class SphinxInterpreter implements SpeechInterpreter {


    Configuration configuration;
    LiveSpeechRecognizer recognizer;

    public SphinxInterpreter() {
        configuration = new Configuration();

        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        try {
            recognizer = new LiveSpeechRecognizer(configuration);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public String getTextFromSpeech() {
        SpeechResult result = recognizer.getResult();
        String text = result.getHypothesis();
        return text;
    }

    public void startListening() {
        recognizer.startRecognition(true);

    }

    public void pauseListening() {
        recognizer.stopRecognition();
    }

    public void resumeListening() {
        recognizer.startRecognition(false);
    }
}
