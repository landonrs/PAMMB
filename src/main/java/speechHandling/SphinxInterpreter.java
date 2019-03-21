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

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;

/**
 * Uses the Sphinx4 speech recognition library to convert speech to text
 */
public class SphinxInterpreter implements SpeechInterpreter {


    Configuration configuration;
    CustomSpeechRecognizer recognizer;
    // holds all words in program dictionary for checking against macro names
    private static HashSet<String> dictionary = new HashSet<>();

    public SphinxInterpreter() {
        configuration = new Configuration();

        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        //configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        try {
            configuration.setGrammarPath(URLDecoder.decode("file:" + SpeechCommandHandler.GRAMMAR_DIR,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        configuration.setGrammarName("SPEECH_RULES");
        configuration.setUseGrammar(true);

        try {
            recognizer = new CustomSpeechRecognizer(configuration);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public String getTextFromSpeech() {
        SpeechResult result = recognizer.getResult();
        return result.getHypothesis();
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

    public static void generateDictionaryHashMap() throws IOException {
        BufferedReader dictionaryReader = new BufferedReader(new InputStreamReader(SpeechCommandHandler.class
                .getClassLoader().getResourceAsStream("edu/cmu/sphinx/models/en-us/cmudict-en-us.dict")));
        while(dictionaryReader.ready()) {
            String line = dictionaryReader.readLine();
            // extract word from line and add to dictionary
            String word = line.substring(0,line.indexOf(" "));
            //System.out.println(word);
            dictionary.add(word);
        }

    }

    public static boolean isInDictionary(String word) {
        return dictionary.contains(word);
    }
}
