package Audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URISyntaxException;

public class MediaPlayerUtil {
    private static File recordSoundFile;
    private static Media recordSound;
    private static MediaPlayer mediaPlayer;

    public static void initializeMediaFiles(){
        System.out.println("initializing media player");
        try {
            File soundFile = new File(MediaPlayerUtil.class.getClassLoader()
                    .getResource("Robot_blip-Marianne_Gagnon-120342607.mp3").toURI());
            recordSound = new Media(new File(MediaPlayerUtil.class.getClassLoader()
                    .getResource("Robot_blip-Marianne_Gagnon-120342607.mp3").toURI()).toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public static void playSound(){
        MediaPlayer mediaPlayer = new MediaPlayer(recordSound);
        mediaPlayer.play();
    }
}
