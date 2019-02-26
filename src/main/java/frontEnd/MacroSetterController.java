package frontEnd;

import db.SQLiteDbFacade;
import eventHandling.EventPerformer;
import eventHandling.EventRecorder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import macro.Macro;
import macro.MacroSettings;
import macro.Step;
import speechHandling.SpeechCommandHandler;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


public class MacroSetterController {

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


    public void recordUserEvents(Stage stage) {
        // set up speech command handling on separate thread
        CompletableFuture recordingCommands = CompletableFuture.runAsync(() -> {
            SpeechCommandHandler.runCreateMode(this);
        });
        Stage instructionsStage = ViewLoader.generateDialog("recordingInstructionsView.fxml");
        // set button event handler to close stage
        Button recordingButton = (Button) instructionsStage.getScene().lookup("#recordingButton");
        recordingButton.setOnAction(event -> instructionsStage.hide());

        instructionsStage.showAndWait();
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
        String standardizedMacroName = getStandardizedMacroName(macroName.getText(), true);
        if(standardizedMacroName != null) {
            System.out.println("Name is unique: " + standardizedMacroName);
            // trim off any extra whitespace from name
            MacroSettings.setMacroName(standardizedMacroName);
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
            SpeechCommandHandler.initialize();
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

        String macroNameInput = getStandardizedMacroName(editNameField.getText(), false);
        // if name is valid or is same delete old macro and save new one
        if(macroNameInput != null) {

            String oldMacroName = MacroSettings.getMacroName();
            MacroSettings.setMacroName(macroNameInput);
            Macro updatedMacro = createMacroWithCurrentSettings();
            boolean saved = dbFacade.saveMacro(updatedMacro);
            if(saved) {
                if(!oldMacroName.equals(macroNameInput)) {
                    // erase old macro
                    SQLiteDbFacade.deleteMacro(oldMacroName);
                }
                MacroSettings.resetValues();
                // update the grammar file with the new command
                SpeechCommandHandler.updateGrammar();
                SpeechCommandHandler.initialize();
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

    /**
     * runs the macro name input from user through several checks to verify
     * it is valid
     * @param macroName - user created name for macro they recorded
     *        newMacro - specifies if macro is being created or edited
     * @return standardized name if checks pass, otherwise null
     */
    private String getStandardizedMacroName(String macroName, boolean newMacro){
        //set warning label to invisible so message is not shown prematurely
        warningLabel.setVisible(false);

        // trim off whitespace and set to lower for consistency
        macroName = macroName.trim().toLowerCase();

        // name must not be empty
        if(macroName.equals("")) {
            warningLabel.setText("Text field cannot be empty");
            return null;
        }

        // names should only have letters and spaces
        char[] chars = macroName.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c) && !Character.isSpaceChar(c)) {
                warningLabel.setText("Macro names cannot have any numbers or special characters");
                return null;
            }
        }

        // if creating a new macro, the name must not already exist in db
        if(newMacro && !dbFacade.uniqueMacroName(macroName)) {
            warningLabel.setText("That name is already being used, please enter another");
            return null;
        }

        // passed checks
        return macroName;
    }
}
