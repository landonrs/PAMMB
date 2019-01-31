package frontEnd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import speechHandling.SpeechCommandHandler;

import java.util.concurrent.CompletableFuture;

public class AssistantModeController {

    public Button menu;
    SpeechCommandHandler speechCommandHandler = SpeechCommandHandler.getInstance();
    CompletableFuture audioCommands = CompletableFuture.runAsync(() -> {
        speechCommandHandler.runAssistantMode();
    });

    @FXML
    private void handleButtonAction (ActionEvent event) throws Exception {
        Stage stage = null;
        Parent root = null;

        if (event.getSource() == menu) {
            System.out.println("turning off assistant mode");
            speechCommandHandler.stopAssistantMode();
            setUpMenuScene();
        }

    }

    private void setUpMenuScene() throws Exception{
        Stage stage = (Stage) menu.getScene().getWindow();
        Scene scene = new Scene(FXMLLoader.load(getClass().getClassLoader().getResource("HomeView.fxml")));
        scene.getStylesheets().add(getClass().getClassLoader().getResource("PammStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
