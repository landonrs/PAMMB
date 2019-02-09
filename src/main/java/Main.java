import db.SQLiteDbFacade;
import frontEnd.ViewLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import speechHandling.SpeechCommandHandler;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        //initialize db
        SQLiteDbFacade dbFacade = SQLiteDbFacade.getInstance();
        // update grammar file for speech recognition
        SpeechCommandHandler speechCommandHandler = SpeechCommandHandler.getInstance();
        SpeechCommandHandler.updateGrammar();
        // set stage for Viewloader to load pages
        ViewLoader.setPrimaryStage(primaryStage);
        primaryStage.setTitle("PAMM");
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("HomeView.fxml"));
        ViewLoader.loadPage(loader);
        ViewLoader.setHomeCoordinates();
        ViewLoader.setInitialized(true);
    }


    public static void main(String[] args) {
        launch(args);
    }
}