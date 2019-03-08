/* PAMM: Personal Assistant Macro Maker.
 * Copyright (C) 2019 Landon Shumway.
 * https://github.com/landonrs/PAMMB
 *
 * PAMM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PAMM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import Audio.MediaPlayerUtil;
import db.SQLiteDbFacade;
import eventHandling.EventRecorder;
import frontEnd.ViewLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import speechHandling.SpeechCommandHandler;
import speechHandling.SphinxInterpreter;

/**
 * Starts the application
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        //initialize db
        SQLiteDbFacade.getInstance();
        //initialize EventRecorder
        EventRecorder.getInstance();
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