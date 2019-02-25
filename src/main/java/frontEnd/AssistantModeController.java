package frontEnd;

import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import speechHandling.SpeechCommandHandler;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class AssistantModeController implements Initializable {

    public Circle pammCircle;
    public Label userSpeechDisplay;
    public Region systemCommandHelp;
    private FillTransition ACTIVATED_TRANSITION;
    private final String ACTIVATED_COLOR = "#44a4ff";
    private final String IDLE_COLOR = "#003261";
    CompletableFuture audioCommands;


    public void playActiviationAnimation(){
        ACTIVATED_TRANSITION = new FillTransition(Duration.millis(1000), pammCircle,
                Color.valueOf(IDLE_COLOR), Color.valueOf(ACTIVATED_COLOR));
        ACTIVATED_TRANSITION.setCycleCount(Animation.INDEFINITE);
        ACTIVATED_TRANSITION.setAutoReverse(true);
        ACTIVATED_TRANSITION.play();
    }

    public void lightUpCircle(){
        if(ACTIVATED_TRANSITION != null) {
            ACTIVATED_TRANSITION.stop();
            ACTIVATED_TRANSITION = null;
        }
        FillTransition LIGHT_TRANSITION = new FillTransition(Duration.millis(250), pammCircle,
                Color.valueOf(IDLE_COLOR), Color.valueOf(ACTIVATED_COLOR));
        LIGHT_TRANSITION.play();
    }

    public void dimCircle(){
        if(ACTIVATED_TRANSITION != null) {
            ACTIVATED_TRANSITION.stop();
            ACTIVATED_TRANSITION = null;
        }
        FillTransition IDLE_TRANSITION = new FillTransition(Duration.millis(1000), pammCircle,
                Color.valueOf(ACTIVATED_COLOR), Color.valueOf(IDLE_COLOR));
        IDLE_TRANSITION.play();
    }

    public void displaySpeech(String speechInput) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                userSpeechDisplay.setText(speechInput);
            }
        });
    }

    public void clearViewText() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                userSpeechDisplay.setText("");
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //set up tooltip and click event handler on system command help region
        Tooltip systemToolTip = new Tooltip("Click to view list of commands");
        Tooltip.install(systemCommandHelp, systemToolTip);
        systemCommandHelp.setOnMouseClicked(event -> ViewLoader.showSystemCommands());
        audioCommands = CompletableFuture.runAsync(() -> {
            SpeechCommandHandler.runAssistantMode(this);
        });
    }

    public void loadHomeView() {
        System.out.println("turning off assistant mode");
        displaySpeech("Closing assistant mode");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("HomeView.fxml"));
                try {
                    ViewLoader.loadPage(loader);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
