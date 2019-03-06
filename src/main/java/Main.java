import Audio.MediaPlayerUtil;
import db.SQLiteDbFacade;
import frontEnd.ViewLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import speechHandling.SpeechCommandHandler;
import speechHandling.SphinxInterpreter;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        //initialize db
        SQLiteDbFacade.getInstance();
        // initialize speech recognition instance and update grammar file for speech recognition
        SpeechCommandHandler.updateSpeechRecognition();
        //generate dictionary used to check for valid macro names
        SphinxInterpreter.generateDictionaryHashMap();
        //initialize mediaPlayers
        MediaPlayerUtil.initializeMediaFiles();
        // set stage for Viewloader to load pages
        ViewLoader.setPrimaryStage(primaryStage);
        primaryStage.setTitle("PAMM");
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/HomeView.fxml"));
        ViewLoader.loadPage(loader);
        ViewLoader.setHomeCoordinates();
        ViewLoader.setInitialized(true);
    }


    public static void main(String[] args) {
        launch(args);
    }
}