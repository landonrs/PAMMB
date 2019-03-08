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

import edu.cmu.sphinx.api.*;
import edu.cmu.sphinx.frontend.util.StreamDataSource;

import java.io.IOException;

public class CustomSpeechRecognizer extends AbstractSpeechRecognizer {
    private static Microphone microphone = null;

    /**
     * This recognizer is identical to the Sphinx LiveRecognizer class except the microphone
     * is static and only initialized once. This allows the program to update the configuration whenever
     * the grammar file is updated at runtime without running into the Windows bug that occurs when more than one
     * microphone is created.
     *
     * @param configuration common configuration
     * @throws IOException if model IO went wrong
     */
    public CustomSpeechRecognizer(Configuration configuration) throws IOException
    {
        super(configuration);
        if(microphone == null) {
            microphone = new Microphone(16000, 16, true, false);
        }
        context.getInstance(StreamDataSource.class)
                .setInputStream(microphone.getStream());
    }

    /**
     * Starts recognition process.
     *
     * @param clear clear cached microphone data
     * @see         LiveSpeechRecognizer#stopRecognition()
     */
    public void startRecognition(boolean clear) {
        recognizer.allocate();
        microphone.startRecording();
    }

    /**
     * Stops recognition process.
     *
     * Recognition process is paused until the next call to startRecognition.
     *
     * @see LiveSpeechRecognizer#startRecognition(boolean)
     */
    public void stopRecognition() {
        microphone.stopRecording();
        recognizer.deallocate();
    }
}
