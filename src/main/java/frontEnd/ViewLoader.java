package frontEnd;

import db.SQLiteDbFacade;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import speechHandling.SpeechCommandHandler;

import java.io.IOException;
import java.util.List;

/**
 * util class for loading the different views of the app
 */
public class ViewLoader {
    // used for main views of application
    private static Stage primaryStage;
    // command list window displayed in assistant mode
    private static Stage customListStage;
    public static boolean listStageOpen = false;
    // system command list
    private static Stage systemListStage;

    private static final int ASSISTANT_MODE_WIDTH = 200;
    private static final int ASSISTANT_MODE_HEIGHT = 300;
    private static final Image ICON = new Image(ViewLoader.class
            .getClassLoader().getResourceAsStream("images/PAMM.png"));
    private static final String STYLESHEET = "PammStyle.css";
    private static double HOME_MENU_X;
    private static double HOME_MENU_Y;
    private static boolean initialized = false;

    // used to keep track of which scene we are currently in
    private static FXMLLoader currentLoader;

    public static void setPrimaryStage(Stage stage) {

        ViewLoader.primaryStage = stage;
        // this prevents the JavaFX platform thread from terminating when primaryStage is hidden
        Platform.setImplicitExit(false);
        ViewLoader.primaryStage.getIcons().add(ICON);
        // explicitly end program when user closes primary primaryStage
        ViewLoader.primaryStage.setOnCloseRequest(event -> checkWindowBeforeExit());
    }

    public static void loadPage(FXMLLoader loader) throws Exception{

        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ViewLoader.class.getClassLoader().getResource(STYLESHEET).toExternalForm());
        primaryStage.setScene(scene);
        if((loader.getController() instanceof HomeMenuController) && initialized) {
            primaryStage.setX(HOME_MENU_X);
            primaryStage.setY(HOME_MENU_Y);
        }

        // set the current loader so we can track which scene we are on
        currentLoader = loader;

        primaryStage.show();
    }

    public static void loadAssistantMode(FXMLLoader loader) throws Exception{
        setHomeCoordinates();

        hidePrimaryStage();

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
        primaryStage.setIconified(false);
    }

    public static void minimizePrimaryStage() {
        primaryStage.setIconified(true);
    }

    public static void hidePrimaryStage() {
        primaryStage.toBack();
        primaryStage.hide();
    }

    public static String displayVarStepValueView(String varStepName) {

        Stage stage = generateDialog("views/VarStepValueView.fxml");
        // set focus on textField
        TextField varValueField = (TextField) stage.getScene().lookup("#varStepValueField");
        varValueField.requestFocus();
        // insert var step name into label
        Label varNameLabel = (Label) stage.getScene().lookup("#varStepName");
        varNameLabel.setText("Enter the value for the " + varStepName);
        stage.toFront();
        stage.showAndWait();
        System.out.println("Returned from show and wait...");
        return VarStepController.varStepValue;
    }

    /**
     * Called when the user tells the program to show the command list or when the program
     * is unable to process 3 commands in a row
     */
    public static void displayCommandList(AssistantModeController controller) {

        // only have one stage open at a time
        if(!listStageOpen) {

            customListStage = generateDialog("views/showCommandsView.fxml");

            //get commands from db
            List commands = SQLiteDbFacade.getMacroNames();
            // populate list with commands
            ListView commandList = (ListView) customListStage.getScene().lookup("#showCommandListView");
            commandList.getItems().addAll(commands);

            Label commandListLabel = (Label) customListStage.getScene().lookup("#commandListLabel");
            commandListLabel.setText("Custom Commands");

            Button commandListButton = (Button) customListStage.getScene().lookup("#commandListButton");
            commandListButton.setOnAction(event ->
                SpeechCommandHandler
                        .handleAssistantCommand((String) commandList.getSelectionModel().getSelectedItem(), controller, true));

            customListStage.setOnHidden(event -> listStageOpen = false);
            customListStage.setOnCloseRequest(event -> listStageOpen = false);
            customListStage.show();
            customListStage.toFront();
            listStageOpen = true;
        }

    }

    /**
     * generate a dialog stage window with the view loaded and returns the stage
     * @param dialogFileName - name of fxml file in resources folder
     * @return
     */
    public static Stage generateDialog(String dialogFileName) {
        Stage dialogWindow = new Stage();
        FXMLLoader loader = new FXMLLoader(ViewLoader.class.getClassLoader().getResource(dialogFileName));
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }

        scene.getStylesheets().add(ViewLoader.class.getClassLoader().getResource(STYLESHEET).toExternalForm());
        dialogWindow.setScene(scene);
        dialogWindow.getIcons().add(ICON);

        return dialogWindow;

    }

    public static void hideCustomCommandList(){
        if(customListStage.isShowing()) {
            customListStage.hide();
        }
    }

    public static void showCustomCommandList() {
        if(listStageOpen && !customListStage.isShowing()) {
            customListStage.show();
        }
    }

    /**
     * This is called whenever the close 'x' button is clicked by the user on the primary stage. If we are in assistant
     * mode we return to the home menu, otherwise the entire program is closed
     */
    private static void checkWindowBeforeExit(){
        if((currentLoader.getController() instanceof AssistantModeController)) {
                SpeechCommandHandler.stopAssistantMode();
                Platform.runLater(() ->
                {
                    if(listStageOpen){
                        hideCustomCommandList();
                    }
                    hideSystemCommands();

                    try {
                        loadPage(new FXMLLoader(ViewLoader.class.getClassLoader().getResource("views/HomeView.fxml")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        }
        else {
            Platform.exit();
        }

    }

    public static void showSystemCommands() {
        if(systemListStage == null || !systemListStage.isShowing()) {
            systemListStage = generateDialog("views/showCommandsView.fxml");

            List systemCommands = SpeechCommandHandler.getSystemCommandNames();

            Label commandListLabel = (Label) systemListStage.getScene().lookup("#commandListLabel");
            commandListLabel.setText("System Commands");

            // populate list with commands
            ListView commandList = (ListView) systemListStage.getScene().lookup("#showCommandListView");
            commandList.getItems().addAll(systemCommands);

            // user cannot run system commands from dialog, so make button invisible
            Button commandListButton = (Button) systemListStage.getScene().lookup("#commandListButton");
            commandListButton.setVisible(false);

            systemListStage.show();
            systemListStage.toFront();
        }
    }

    public static void hideSystemCommands() {
        if(systemListStage != null && systemListStage.isShowing()) {
            systemListStage.hide();
        }
    }

}
