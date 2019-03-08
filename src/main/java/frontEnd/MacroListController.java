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

import db.SQLiteDbFacade;
import eventHandling.EventPerformer;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import macro.Macro;
import macro.MacroSettings;
import speechHandling.SpeechCommandHandler;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * This controller handles events that take place in the macroListView
 */
public class MacroListController implements Initializable {

    private List macroNames = SQLiteDbFacade.getMacroNames();
    public ListView macroList;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        macroList.getItems().addAll(macroNames);
    }

    public void runSelectedMacro(ActionEvent actionEvent) {
        String selectedMacro = (String) macroList.getSelectionModel().getSelectedItem();
        // if user hasn't selected anything, return
        if(selectedMacro == null) {
            return;
        }
        System.out.println("Running macro " + selectedMacro);
        Macro userMacro = SQLiteDbFacade.getInstance().loadMacro(selectedMacro);
        // hide view while performing macro
        ViewLoader.hidePrimaryStage();
        EventPerformer.performMacro(userMacro);
        ViewLoader.showPrimaryStage();

    }

    public void deleteSelectedMacro(ActionEvent actionEvent) {
        String selectedMacro = (String) macroList.getSelectionModel().getSelectedItem();
        // if user hasn't selected anything, return
        if(selectedMacro == null) {
            return;
        }
        System.out.println("Deleting " + selectedMacro);
        SQLiteDbFacade.getInstance().deleteMacro(selectedMacro);
        updateList();
        // remove the command from the grammar file for speech recognition
        SpeechCommandHandler.updateSpeechRecognition();

    }

    public void displayHomeMenu(ActionEvent actionEvent) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/HomeView.fxml"));
        ViewLoader.loadPage(loader);
    }

    private void updateList(){
        macroNames = SQLiteDbFacade.getMacroNames();
        macroList.getItems().clear();
        macroList.getItems().addAll(macroNames);
    }

    public void editSelectedMacro(ActionEvent actionEvent) {
        // first load the macro so we can set the controls to the current macro settings
        String selectedName = (String) macroList.getSelectionModel().getSelectedItem();
        // if user hasn't selected anything, return
        if(selectedName == null) {
            return;
        }
        Macro selectedMacro = SQLiteDbFacade.getInstance().loadMacro(selectedName);
        // store selected macro in MacroSettings so steps will be preserved
        MacroSettings.currentMacro = selectedMacro;
        MacroSettings.setMacroName(selectedName);
        // create edit dialog window
        Stage editDialog = ViewLoader.generateDialog("views/editMacroView.fxml");
        //set controls to current settings
        TextField nameField = (TextField) editDialog.getScene().lookup("#editNameField");
        nameField.setText(selectedMacro.getName());
        Slider delaySlider = (Slider) editDialog.getScene().lookup("#secondSlider");
        delaySlider.setValue(selectedMacro.getSecondDelay());
        CheckBox mouseBox = (CheckBox) editDialog.getScene().lookup("#mouseVisibleBox");
        mouseBox.setSelected(!selectedMacro.isMouseIsVisible());
        // once dialog is closed, update list
        editDialog.setOnHidden(event -> {
            updateList();
        });
        ViewLoader.hidePrimaryStage();
        editDialog.show();
        editDialog.toFront();

    }
}
