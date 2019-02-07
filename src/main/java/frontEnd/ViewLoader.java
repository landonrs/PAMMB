package frontEnd;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * util class for loading the different views of the app
 */
public class ViewLoader {
    private static Stage stage;

    public static void setPrimaryStage(Stage stage) {
        ViewLoader.stage = stage;
    }

    public static void loadPage(FXMLLoader loader) throws Exception{
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ViewLoader.class.getClassLoader().getResource("PammStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}
