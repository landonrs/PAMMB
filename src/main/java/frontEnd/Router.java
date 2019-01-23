package frontEnd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class Router {

    public Button menu, assistantMode;

    @FXML
    private void handleButtonAction (ActionEvent event) throws Exception {
        Stage stage;
        Parent root;

        if(event.getSource()== menu){
            stage = (Stage) menu.getScene().getWindow();
            root = FXMLLoader.load(getClass().getClassLoader().getResource("HomeView.fxml"));
        }
        else{
            stage = (Stage) assistantMode.getScene().getWindow();
            root = FXMLLoader.load(getClass().getClassLoader().getResource("AssistantModeView.fxml"));
        }
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("PammStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
