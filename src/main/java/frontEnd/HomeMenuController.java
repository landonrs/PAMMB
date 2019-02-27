package frontEnd;

import eventHandling.EventRecorder;
import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import speechHandling.SpeechCommandHandler;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeMenuController implements Initializable {

    public Button createMacro, viewList, assistantMode;
    EventRecorder recorder = EventRecorder.getInstance();
    public Circle pammCircle;

    private final String ACTIVATED_COLOR = "#44a4ff";
    private final String IDLE_COLOR = "#003261";

    @FXML
    private void handleButtonAction (ActionEvent event) throws Exception {
        Stage stage = null;
        Parent root = null;

        if(event.getSource() == createMacro) {
            stage = (Stage) createMacro.getScene().getWindow();
            stage.toBack();
            stage.hide();
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("macroNameSetterView.fxml"));
            root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getClassLoader().getResource("PammStyle.css").toExternalForm());
            stage.setScene(scene);
            MacroSetterController macroSetterController = loader.getController();
            macroSetterController.recordUserEvents(stage);
        }
        if(event.getSource() == viewList){
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("macroListView.fxml"));
            ViewLoader.loadPage(loader);
        }
        if(event.getSource() == assistantMode){
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("AssistantModeView.fxml"));
            Stage assistantInstructions = ViewLoader.generateDialog("AssistantInstructions.fxml");
            ViewLoader.hidePrimaryStage();
            Button startButton = (Button) assistantInstructions.getScene().lookup("#startButton");
            startButton.setOnAction(actionEvent -> assistantInstructions.hide());
            assistantInstructions.showAndWait();
            if(SpeechCommandHandler.isUpdated()) {
                ViewLoader.loadAssistantMode(loader);
            }
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playActiviationAnimation();

    }

    public void playActiviationAnimation()  {
        FillTransition ACTIVATED_TRANSITION = new FillTransition(Duration.millis(2000), pammCircle,
                Color.valueOf(IDLE_COLOR), Color.valueOf(ACTIVATED_COLOR));
        ACTIVATED_TRANSITION.setCycleCount(Animation.INDEFINITE);
        ACTIVATED_TRANSITION.setAutoReverse(true);
        ACTIVATED_TRANSITION.play();
    }
}
