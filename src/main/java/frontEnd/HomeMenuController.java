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

package frontEnd;

import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import speechHandling.SpeechCommandHandler;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Handles events that take place from the home menu
 */
public class HomeMenuController implements Initializable {

    public Button createMacro, viewList, assistantMode, licenseButton;
    public Circle pammCircle;

    //stage to show license terms and conditions
    static Stage licenseNotice = null;

    private final String ACTIVATED_COLOR = "#44a4ff";
    private final String IDLE_COLOR = "#003261";
    // This link sends the user to the PAMM tutorial playlist
    private final URI TUTORIAL_URI = URI.create("https://www.youtube.com/watch?v=LVvMyLyMXeQ&list=PL84FpCusm1XN05kxA59hioVxxz2l6Tyev");

    @FXML
    private void handleButtonAction (ActionEvent event) throws Exception {
        Stage stage = null;
        Parent root = null;

        if(event.getSource() == createMacro) {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/macroNameSetterView.fxml"));
            // hide main menu
            ViewLoader.hidePrimaryStage();
            // load scene but don't display yet
            ViewLoader.loadPage(loader, false);
            MacroSetterController macroSetterController = loader.getController();
            macroSetterController.recordUserEvents();
        }
        if(event.getSource() == viewList){
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/macroListView.fxml"));
            ViewLoader.loadPage(loader);
        }
        if(event.getSource() == assistantMode){
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/AssistantModeView.fxml"));
            Stage assistantInstructions = ViewLoader.generateDialog("views/AssistantInstructions.fxml");
            ViewLoader.hidePrimaryStage();
            Button startButton = (Button) assistantInstructions.getScene().lookup("#startButton");
            startButton.setOnAction(actionEvent -> assistantInstructions.hide());
            assistantInstructions.showAndWait();
            if(SpeechCommandHandler.isUpdated()) {
                // make sure microphone is not being used from previous speech session
                if(SpeechCommandHandler.runningMicrophone()) {
                    // if it is, we need the user to make a noise
                    SpeechCommandHandler.runMicrophoneTest();
                }
                ViewLoader.loadAssistantMode(loader);
            }
        }

        if(event.getSource() == licenseButton) {
            if(licenseNotice == null || !licenseNotice.isShowing()) {
                licenseNotice = ViewLoader.generateDialog("views/licenseTermsView.fxml");
                licenseNotice.setAlwaysOnTop(true);
                licenseNotice.show();
                licenseNotice.toFront();
            }
        }

    }

    @FXML
    private void openTutorial() {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(TUTORIAL_URI);
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "PAMM could not open your web browser", ButtonType.OK);
                alert.showAndWait();
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "PAMM could not open your web browser", ButtonType.OK);
            alert.showAndWait();
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playActiviationAnimation();

    }

    private void playActiviationAnimation()  {
        FillTransition ACTIVATED_TRANSITION = new FillTransition(Duration.millis(2500), pammCircle,
                Color.valueOf(IDLE_COLOR), Color.valueOf(ACTIVATED_COLOR));
        ACTIVATED_TRANSITION.setCycleCount(Animation.INDEFINITE);
        ACTIVATED_TRANSITION.setAutoReverse(true);
        ACTIVATED_TRANSITION.play();
    }
}
