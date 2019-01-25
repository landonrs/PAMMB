package frontEnd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import speechHandling.SpeechCommandHandler;
import speechHandling.SphinxInterpreter;

import java.util.concurrent.CompletableFuture;

public class Router {

    public Button menu, assistantMode;
    SpeechCommandHandler speechCommandHandler = new SpeechCommandHandler(new SphinxInterpreter());
    CompletableFuture audioCommands = null;

    @FXML
    private void handleButtonAction (ActionEvent event) throws Exception {
        Stage stage = null;
        Parent root = null;

        if(event.getSource() == menu){
            stage = (Stage) menu.getScene().getWindow();
            root = FXMLLoader.load(getClass().getClassLoader().getResource("HomeView.fxml"));
            System.out.println("turning off assistant mode");
            speechCommandHandler.stopAssistantMode();

        }
        else if(event.getSource() == assistantMode){
            stage = (Stage) assistantMode.getScene().getWindow();
            root = FXMLLoader.load(getClass().getClassLoader().getResource("AssistantModeView.fxml"));
            audioCommands = CompletableFuture.runAsync(() -> {
                speechCommandHandler.runAssistantMode();
            });

        }
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("PammStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
