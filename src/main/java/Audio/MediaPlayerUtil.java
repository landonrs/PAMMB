package Audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URISyntaxException;

public class MediaPlayerUtil {
    private static Media recordSound;

    public static void initializeMediaFiles(){
        System.out.println("initializing media player");
        try {
            recordSound = new Media(MediaPlayerUtil.class.getClassLoader()
                    .getResource("Robot_blip-Marianne_Gagnon-120342607.mp3").toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public static void playSound(){
        MediaPlayer mediaPlayer = new MediaPlayer(recordSound);
        mediaPlayer.play();
    }
}
