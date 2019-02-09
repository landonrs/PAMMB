package frontEnd;

import eventHandling.EventRecorder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import speechHandling.SpeechCommandHandler;

public class HomeMenuController {

    public Button createMacro, viewList, assistantMode;
    SpeechCommandHandler speechCommandHandler = SpeechCommandHandler.getInstance();
    EventRecorder recorder = EventRecorder.getInstance();

    @FXML
    private void handleButtonAction (ActionEvent event) throws Exception {
        Stage stage = null;
        Parent root = null;

        if(event.getSource() == createMacro) {
            stage = (Stage) createMacro.getScene().getWindow();
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
            ViewLoader.loadAssistantMode(loader);
        }

    }


    public static String getVariableStepValue(){

        return "testValue";

    }
}
