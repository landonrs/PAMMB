package frontEnd;

import eventHandling.EventRecorder;
import macro.Macro;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import speechHandling.SpeechCommandHandler;

import java.util.concurrent.CompletableFuture;

public class HomeMenuController {

    public Button createMacro, assistantMode;
    SpeechCommandHandler speechCommandHandler = SpeechCommandHandler.getInstance();
    EventRecorder recorder = EventRecorder.getInstance();

    @FXML
    private void handleButtonAction (ActionEvent event) throws Exception {
        Stage stage = null;
        Parent root = null;

        if(event.getSource() == createMacro) {
            stage = (Stage) createMacro.getScene().getWindow();
            recordUserEvents(stage);
        }
        if(event.getSource() == assistantMode){
            stage = (Stage) assistantMode.getScene().getWindow();
            root = FXMLLoader.load(getClass().getClassLoader().getResource("AssistantModeView.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getClassLoader().getResource("PammStyle.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        }

    }

    private void recordUserEvents(Stage stage) {
        stage.hide();
        // set up speech command handling on separate thread
        CompletableFuture recordingCommands = CompletableFuture.runAsync(() -> {
            speechCommandHandler.runCreateMode();
        });
        Macro createdMacro = recorder.recordUserMacro();
        System.out.println("Finished recording macro");
        //stage.setIconified(false);
        stage.show();
        stage.toFront();
    }

    public static String getVariableStepValue(){

        return "testValue";

    }
}
