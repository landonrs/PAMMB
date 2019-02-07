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
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("HomeView.fxml"));
            ViewLoader.loadPage(loader);
        }

    }
}
