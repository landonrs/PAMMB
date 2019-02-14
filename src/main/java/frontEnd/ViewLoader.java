package frontEnd;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * util class for loading the different views of the app
 */
public class ViewLoader {
    private static Stage stage;
    private static final int ASSISTANT_MODE_WIDTH = 200;
    private static final int ASSISTANT_MODE_HEIGHT = 300;
    private static double HOME_MENU_X;
    private static double HOME_MENU_Y;
    private static boolean initialized = false;

    public static void setPrimaryStage(Stage stage) {

        ViewLoader.stage = stage;
        Platform.setImplicitExit(false);
        ViewLoader.stage.setOnCloseRequest(event -> {
            Platform.exit();
        });
    }

    public static void loadPage(FXMLLoader loader) throws Exception{

        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ViewLoader.class.getClassLoader().getResource("PammStyle.css").toExternalForm());
        stage.setScene(scene);
        //System.out.println(loader.getController().getClass());
        if((loader.getController() instanceof HomeMenuController) && initialized) {
            stage.setX(HOME_MENU_X);
            stage.setY(HOME_MENU_Y);
        }

        stage.show();
    }

    public static void loadAssistantMode(FXMLLoader loader) throws Exception{
        setHomeCoordinates();

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        //set Stage boundaries to the lower right corner of the visible bounds of the main screen
        stage.setX(primaryScreenBounds.getMinX() + primaryScreenBounds.getMaxX() - ASSISTANT_MODE_WIDTH);
        stage.setY(primaryScreenBounds.getMinY() + primaryScreenBounds.getMaxY() - ASSISTANT_MODE_HEIGHT);
        stage.setAlwaysOnTop(true);

        loadPage(loader);
    }

    public static void setInitialized(boolean initialized) {
        ViewLoader.initialized = initialized;
    }

    public static void setHomeCoordinates(){
        //store previous coordinates of stage so we can revert back to it
        HOME_MENU_X = stage.getX();
        HOME_MENU_Y = stage.getY();
    }

    public static void showPrimaryStage() {
        stage.toFront();
        stage.show();
    }

    public static String displayVarStepValueView(String varStepName) throws IOException {

        FXMLLoader loader = new FXMLLoader(ViewLoader.class.getClassLoader().getResource("VarStepValueView.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(ViewLoader.class.getClassLoader().getResource("PammStyle.css").toExternalForm());
        stage.setScene(scene);
        // set focus on textField
        TextField varValueField = (TextField) scene.lookup("#varStepValueField");
        varValueField.requestFocus();
        // insert var step name into label
        Label varNameLabel = (Label) scene.lookup("#varStepName");
        varNameLabel.setText("Enter the value for the " + varStepName);
        stage.toFront();
        stage.showAndWait();
        System.out.println("Returned from show and wait...");
        return VarStepController.varStepValue;
    }
}
