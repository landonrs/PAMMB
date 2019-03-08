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

/* sound effect attribution:
*    Title: "Robot Blip"
*    Author: Marianne Gagnon
*    Link: http://soundbible.com/1682-Robot-Blip.html#Robot%20Blip%20Sound
*    Used under the Attribution 3.0 license: https://creativecommons.org/licenses/by/3.0/us/
*/

package Audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URISyntaxException;

/**
 * Util class used to play sounds during certain events
 */
public class MediaPlayerUtil {
    private static Media recordSound;
    private static Media eventRegisterSound;

    public static void initializeMediaFiles(){
        System.out.println("initializing media player");
        try {
            recordSound = new Media(MediaPlayerUtil.class.getClassLoader()
                    .getResource("Robot_blip-Marianne_Gagnon-120342607.mp3").toURI().toString());
            eventRegisterSound = new Media(MediaPlayerUtil.class.getClassLoader()
                    .getResource("event_register_sound.mp3").toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public static void playActivationSound(){
        MediaPlayer mediaPlayer = new MediaPlayer(recordSound);
        mediaPlayer.setVolume(0.75);
        mediaPlayer.play();
    }

    public static void playEventRegisterSound() {
        MediaPlayer mediaPlayer = new MediaPlayer(eventRegisterSound);
        mediaPlayer.setVolume(0.05);
        mediaPlayer.play();
    }
}
