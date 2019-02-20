package frontEnd;

import Audio.MediaPlayerUtil;
import db.SQLiteDbFacade;
import eventHandling.EventPerformer;
import eventHandling.EventRecorder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import macro.Macro;
import macro.MacroSettings;
import macro.Step;
import speechHandling.SpeechCommandHandler;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


public class MacroSetterController implements Initializable {

    // controls for nameSetter View
    public Label warningLabel;
    public TextField macroName;
    //controls for settings view
    public CheckBox mouseVisibleBox;
    public Slider secondSlider;
    // controls for varStepNameSetter view
    public TextField varStepNameField;
    public Button varStepNameCancel;
    // controls for editMacro view
    public TextField editNameField;


    private SQLiteDbFacade dbFacade = SQLiteDbFacade.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MediaPlayerUtil.playSound();
    }

    public void recordUserEvents(Stage stage) {
        SpeechCommandHandler speechCommandHandler = SpeechCommandHandler.getInstance();
        // set up speech command handling on separate thread
        CompletableFuture recordingCommands = CompletableFuture.runAsync(() -> {
            speechCommandHandler.runCreateMode(this);
        });
        EventRecorder.startRecordingUserMacro();

    }

    public void finishRecording() {
        MacroSettings.currentMacro = EventRecorder.finishRecordingUserMacro();
        System.out.println("Finished recording macro with steps: ");
        for(Step step: MacroSettings.currentMacro.getSteps()) {
            System.out.println(step.getType());
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ViewLoader.showPrimaryStage();
            }
        });

    }

    public void getVariableStepName() {
        // EventRecorder.stopRecording();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Stage stage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("VarStepNameView.fxml"));
                Parent root = null;
                try {
                    root = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Scene scene = new Scene(root);
                scene.getStylesheets().add(ViewLoader.class.getClassLoader().getResource("PammStyle.css").toExternalForm());
                stage.setScene(scene);
                stage.show();
                stage.toFront();
            }
        });
    }


    @FXML
    public void checkMacroName(ActionEvent actionEvent) throws Exception{
        if(dbFacade.uniqueMacroName(macroName.getText().trim())) {
            System.out.println("Name is unique: " + macroName.getText());
            // trim off any extra whitespace from name
            MacroSettings.setMacroName(macroName.getText().trim());
            Stage stage = (Stage) macroName.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("macroSettingView.fxml"));
            ViewLoader.loadPage(loader);
        }
        else{
            warningLabel.setVisible(true);
        }

    }

    @FXML
    public void saveMacro(ActionEvent actionEvent) throws Exception{
        Macro userMacro = createMacroWithCurrentSettings();
        boolean saved = dbFacade.saveMacro(userMacro);
        if(saved) {
            MacroSettings.resetValues();
            // update the grammar file with the new command
            SpeechCommandHandler.updateGrammar();
        }
        displayHomeView();

    }

    @FXML
    public void cancelSave(ActionEvent actionEvent) throws Exception{
        displayHomeView();

    }

    private void displayHomeView() throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("HomeView.fxml"));
        ViewLoader.loadPage(loader);
    }

    @FXML
    public void setVarStepName(ActionEvent actionEvent) throws InterruptedException {
        String varStepName = varStepNameField.getText();
        Stage stage = (Stage) varStepNameCancel.getScene().getWindow();
        stage.close();
        TimeUnit.MILLISECONDS.sleep(250);
        EventRecorder.createVariableStep(varStepName);
        EventRecorder.resumeRecording();
    }


    @FXML
    public void cancelVarStep(ActionEvent actionEvent) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(250);
        Stage stage = (Stage) varStepNameCancel.getScene().getWindow();
        stage.hide();
        EventRecorder.resumeRecording();
    }

    @FXML
    public void testMacro(ActionEvent actionEvent) {
        Macro testMacro = createMacroWithCurrentSettings();
        ViewLoader.hidePrimaryStage();
        EventPerformer.performMacro(testMacro);
        ViewLoader.showPrimaryStage();
    }

    private Macro createMacroWithCurrentSettings(){
        int secondDelay = (int) secondSlider.getValue();
        MacroSettings.setSecondDelay(secondDelay);
        boolean checked = mouseVisibleBox.isSelected();
        MacroSettings.setMouseIsVisible(!checked);
        Macro macro = MacroSettings.configureMacroSettings();
        System.out.println("created macro " + macro.getName() + " with " +
        secondDelay + " second delay and visible set to " + !checked);

        return macro;

    }


    /**
     * This is called when a user has selected to update the settings of a
     * previously created macro.
     * @param actionEvent
     */
    public void updateMacro(ActionEvent actionEvent) throws IOException {
        // if name is valid or is same delete old macro and save new one
        if(dbFacade.uniqueMacroName(editNameField.getText().trim()) ||
                editNameField.getText().trim().equals(MacroSettings.getMacroName())) {

            SQLiteDbFacade.deleteMacro(MacroSettings.getMacroName());
            MacroSettings.setMacroName(editNameField.getText().trim());
            Macro updatedMacro = createMacroWithCurrentSettings();
            boolean saved = dbFacade.saveMacro(updatedMacro);
            if(saved) {
                MacroSettings.resetValues();
                // update the grammar file with the new command
                SpeechCommandHandler.updateGrammar();
            }
            // now remove the dialog from view
            Stage stage = (Stage) editNameField.getScene().getWindow();
            stage.close();
            // show macro list view
            ViewLoader.showPrimaryStage();
        }
        else {
            warningLabel.setVisible(true);
        }

    }

    public void testEditedMacro(ActionEvent actionEvent) {
        Macro testMacro = createMacroWithCurrentSettings();
        Stage dialogStage = (Stage) editNameField.getScene().getWindow();
        dialogStage.hide();
        EventPerformer.performMacro(testMacro);
        dialogStage.show();
        dialogStage.toFront();
    }

    public void cancelEdit(ActionEvent actionEvent) {
        Stage dialogStage = (Stage) editNameField.getScene().getWindow();
        dialogStage.close();
        ViewLoader.showPrimaryStage();
    }
}
