package frontEnd;

import db.SQLiteDbFacade;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * util class for loading the different views of the app
 */
public class ViewLoader {
    // used for main views of application
    private static Stage primaryStage;
    // command list window displayed in assistant mode
    private static Stage listStage;
    public static boolean listStageOpen = false;
    private static final int ASSISTANT_MODE_WIDTH = 200;
    private static final int ASSISTANT_MODE_HEIGHT = 300;
    private static double HOME_MENU_X;
    private static double HOME_MENU_Y;
    private static boolean initialized = false;

    public static void setPrimaryStage(Stage stage) {

        ViewLoader.primaryStage = stage;
        // this prevents the JavaFX platform thread from terminating when primaryStage is hidden
        Platform.setImplicitExit(false);
        // explicitly end program when user closes primary primaryStage
        ViewLoader.primaryStage.setOnCloseRequest(event -> Platform.exit());
    }

    public static void loadPage(FXMLLoader loader) throws Exception{

        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ViewLoader.class.getClassLoader().getResource("PammStyle.css").toExternalForm());
        primaryStage.setScene(scene);
        //System.out.println(loader.getController().getClass());
        if((loader.getController() instanceof HomeMenuController) && initialized) {
            primaryStage.setX(HOME_MENU_X);
            primaryStage.setY(HOME_MENU_Y);
        }

        primaryStage.show();
    }

    public static void loadAssistantMode(FXMLLoader loader) throws Exception{
        setHomeCoordinates();

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        //set Stage boundaries to the lower right corner of the visible bounds of the main screen
        primaryStage.setX(primaryScreenBounds.getMinX() + primaryScreenBounds.getMaxX() - ASSISTANT_MODE_WIDTH);
        primaryStage.setY(primaryScreenBounds.getMinY() + primaryScreenBounds.getMaxY() - ASSISTANT_MODE_HEIGHT);
        primaryStage.setAlwaysOnTop(true);

        loadPage(loader);
    }

    public static void setInitialized(boolean initialized) {
        ViewLoader.initialized = initialized;
    }

    public static void setHomeCoordinates(){
        //store previous coordinates of primaryStage so we can revert back to it
        HOME_MENU_X = primaryStage.getX();
        HOME_MENU_Y = primaryStage.getY();
    }

    public static void showPrimaryStage() {
        primaryStage.show();
        primaryStage.toFront();
    }

    public static void hidePrimaryStage() {
        primaryStage.toBack();
        primaryStage.hide();
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

    public static void displayCommandList() {

        if(!listStageOpen) {
            //load list view
            FXMLLoader listLoader = new FXMLLoader(ViewLoader.class.getClassLoader().getResource("showCommandsView.fxml"));
            Parent root = null;
            try {
                root = listLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            listStage = new Stage();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(ViewLoader.class.getClassLoader().getResource("PammStyle.css").toExternalForm());
            listStage.setScene(scene);
            //get commands from db
            List commands = SQLiteDbFacade.getMacroNames();
            // populate list with commands
            ListView commandList = (ListView) scene.lookup("#showCommandListView");
            commandList.getItems().addAll(commands);
            // prevent user from being able to click on it
            commandList.setMouseTransparent(true);
            commandList.setFocusTraversable(false);

            listStage.setOnCloseRequest(event -> listStageOpen = false);
            listStage.show();
            listStageOpen = true;
        }

    }

    public static void hideCommandList(){
        if(listStageOpen) {
            listStage.hide();
            listStageOpen = false;
        }
    }
}
