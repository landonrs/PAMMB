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
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import speechHandling.SpeechCommandHandler;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AssistantModeController implements Initializable {

    public Circle pammCircle;
    public Label userSpeechDisplay;
    public Region systemCommandHelp;
    public ToggleButton confirmModeButton;
    private FillTransition ACTIVATED_TRANSITION;
    // for circle animation
    private final String ACTIVATED_COLOR = "#44a4ff";
    private final String IDLE_COLOR = "#003261";
    CompletableFuture audioCommands;


    public void playActiviationAnimation(){
        ACTIVATED_TRANSITION = new FillTransition(Duration.millis(1000), pammCircle,
                Color.valueOf(IDLE_COLOR), Color.valueOf(ACTIVATED_COLOR));
        ACTIVATED_TRANSITION.setCycleCount(Animation.INDEFINITE);
        ACTIVATED_TRANSITION.setAutoReverse(true);
        ACTIVATED_TRANSITION.play();
    }

    public void lightUpCircle(){
        if(ACTIVATED_TRANSITION != null) {
            ACTIVATED_TRANSITION.stop();
            ACTIVATED_TRANSITION = null;
        }
        FillTransition LIGHT_TRANSITION = new FillTransition(Duration.millis(250), pammCircle,
                Color.valueOf(IDLE_COLOR), Color.valueOf(ACTIVATED_COLOR));
        LIGHT_TRANSITION.play();
    }

    public void dimCircle(){
        if(ACTIVATED_TRANSITION != null) {
            ACTIVATED_TRANSITION.stop();
            ACTIVATED_TRANSITION = null;
        }
        FillTransition IDLE_TRANSITION = new FillTransition(Duration.millis(1000), pammCircle,
                Color.valueOf(ACTIVATED_COLOR), Color.valueOf(IDLE_COLOR));
        IDLE_TRANSITION.play();
    }

    public void displaySpeech(String speechInput) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                userSpeechDisplay.setText(speechInput);
            }
        });
    }

    public void clearViewText() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                userSpeechDisplay.setText("");
            }
        });
    }

    // for use when called from within Platform thread
    public void displaySpeechOnPlatThread(String message) {
        userSpeechDisplay.setText(message);
    }

    public void clearViewTextOnPlatThread() {
        userSpeechDisplay.setText("");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //set up tooltip and click event handler on system command help region
        Tooltip systemToolTip = new Tooltip("Click to view list of commands");
        Tooltip.install(systemCommandHelp, systemToolTip);
        systemCommandHelp.setOnMouseClicked(event -> ViewLoader.showSystemCommands());
        // set up toggle button handler
        confirmModeButton.setOnAction(event -> setConfirmMode());
        audioCommands = CompletableFuture.runAsync(() -> {
            SpeechCommandHandler.runAssistantMode(this);
        });
    }

    public void loadHomeView() {
        System.out.println("turning off assistant mode");
        displaySpeech("Closing assistant mode");
        // give user time to see message
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/HomeView.fxml"));
                try {
                    ViewLoader.loadPage(loader);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setConfirmMode() {
        String style = "-fx-background-color: #003261";

        if (confirmModeButton.isSelected()) {
            style = "-fx-background-color: #44a4ff";
        }

        confirmModeButton.setStyle(style);
        SpeechCommandHandler.setConfirmationMode(confirmModeButton.isSelected());
    }


}
