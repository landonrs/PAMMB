package Audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URISyntaxException;

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
