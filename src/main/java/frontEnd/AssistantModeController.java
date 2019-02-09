package frontEnd;

import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import speechHandling.SpeechCommandHandler;

import java.util.concurrent.CompletableFuture;

public class AssistantModeController {

    public Button menu;
    public Circle pammCircle;
    public Label userSpeechDisplay;
    private FillTransition ACTIVATED_TRANSITION;
    private final String ACTIVATED_COLOR = "#44a4ff";
    private final String IDLE_COLOR = "#003261";
    SpeechCommandHandler speechCommandHandler = SpeechCommandHandler.getInstance();
    CompletableFuture audioCommands = CompletableFuture.runAsync(() -> {
        speechCommandHandler.runAssistantMode(this);
    });

    @FXML
    private void handleButtonAction (ActionEvent event) throws Exception {
        Stage stage = null;
        Parent root = null;

        if (event.getSource() == menu) {
            System.out.println("turning off assistant mode");
            speechCommandHandler.stopAssistantMode();
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("HomeView.fxml"));
            ViewLoader.loadPage(loader);
        }

    }

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
}
