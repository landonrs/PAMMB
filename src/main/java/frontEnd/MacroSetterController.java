package frontEnd;

import db.SQLiteDbFacade;
import eventHandling.EventPerformer;
import eventHandling.EventRecorder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import macro.Macro;
import macro.MacroSettings;
import macro.Step;
import speechHandling.SpeechCommandHandler;
import speechHandling.SphinxInterpreter;

import java.io.IOException;
import java.util.ArrayList;
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
    // used to display words that do not exist in dictionary
    private Stage dictionaryCheck = null;
    // this tracks words that do not exist in dictionary
    private ArrayList<String> missingWords = new ArrayList<>();

    //create an invisible stage that will allow the user to kill the create mode manually by closing it
    private static Stage invisibleStage = null;
    // set to true when user manually ends macro recording with button click
    private boolean trimButtonClick = false;


    private SQLiteDbFacade dbFacade = SQLiteDbFacade.getInstance();


    public void recordUserEvents(Stage stage) {

        Stage instructionsStage = ViewLoader.generateDialog("views/recordingInstructionsView.fxml");
        // set button event handler to close stage
        Button recordingButton = (Button) instructionsStage.getScene().lookup("#recordingButton");
        recordingButton.setOnAction(event -> instructionsStage.hide());
        CheckBox eventSoundCheckBox = (CheckBox) instructionsStage.getScene().lookup("#eventSoundCheckBox");
        eventSoundCheckBox.setOnMouseClicked(event -> EventRecorder.setEventSound(eventSoundCheckBox.isSelected()));
        instructionsStage.showAndWait();
        if(SpeechCommandHandler.isUpdated()) {
            // make sure microphone is not being used in assistant mode
            if(SpeechCommandHandler.runningAssistantMode()) {
                // if it is, we need the user to make a noise
                SpeechCommandHandler.runMicrophoneTest();
            }
            // set up speech command handling on separate thread
            CompletableFuture recordingCommands = CompletableFuture.runAsync(() -> {
                SpeechCommandHandler.runCreateMode(this);
            });

            setUpKillSwitch();

            EventRecorder.startRecordingUserMacro();
        }

    }

    public void finishRecording() {
        MacroSettings.currentMacro = EventRecorder.finishRecordingUserMacro();
        System.out.println("Finished recording macro with steps: ");
        for(Step step: MacroSettings.currentMacro.getSteps()) {
            System.out.println(step.getType());
        }
        //if user manually stopped recording, remove step that was recorded for clicking the button
        if(trimButtonClick){
            int buttonClickStepIndex = MacroSettings.currentMacro.getSteps().size() - 1;
            MacroSettings.currentMacro.getSteps().remove(buttonClickStepIndex);
        }


        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // remove invisible stage
                if(invisibleStage.isShowing()) {
                    invisibleStage.hide();
                }
                ViewLoader.showPrimaryStage();
            }
        });

    }

    public void getVariableStepName() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Stage stage = ViewLoader.generateDialog("views/VarStepNameView.fxml");
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
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/macroSettingView.fxml"));
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
            SpeechCommandHandler.updateSpeechRecognition();
        }
        displayHomeView();

    }

    @FXML
    public void cancelSave(ActionEvent actionEvent) throws Exception{
        displayHomeView();

    }

    private void displayHomeView() throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/HomeView.fxml"));
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
                    SQLiteDbFacade.getInstance().deleteMacro(oldMacroName);
                }
                MacroSettings.resetValues();
                // update the grammar file with the new command
                SpeechCommandHandler.updateSpeechRecognition();

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

        // each word must exist in speech recognition dictionary
        String[] words = macroName.split(" ");
        // remove words from any previous checks
        missingWords.clear();
        for(String word: words) {
            //System.out.println(word);
            if(!SphinxInterpreter.isInDictionary(word)){
                //System.out.println(" is not in the dictionary");
                missingWords.add(word);
            }
        }

        if (!missingWords.isEmpty()) {
            // display which words do not exist in dictionary to user
            if(dictionaryCheck == null) {
                dictionaryCheck = ViewLoader.generateDialog("views/dictionaryCheckView.fxml");
                dictionaryCheck.setAlwaysOnTop(true);
                dictionaryCheck.setOnCloseRequest(event -> dictionaryCheck = null);

                Button closeButton = (Button) dictionaryCheck.getScene().lookup("#dictionaryCheckButton");
                closeButton.setOnAction(event -> {
                    dictionaryCheck.hide();
                    dictionaryCheck = null;
                });
                dictionaryCheck.show();
            }
            // populate list with most recent missing words
            ListView missingWordsList = (ListView) dictionaryCheck.getScene().lookup("#missingWordsList");
            // clear any previous words
            missingWordsList.getItems().clear();
            missingWordsList.getItems().addAll(missingWords);


            warningLabel.setText("Words must exist in program dictionary");
            return null;
        }

        // passed checks
        // hide window if showing
        if(dictionaryCheck != null){
            dictionaryCheck.hide();
            dictionaryCheck = null;
        }
        return macroName;
    }

    private void setUpKillSwitch(){
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        //presents the user with manual kill switch
        invisibleStage = ViewLoader.generateDialog("views/killSwitchButton.fxml");
        invisibleStage.initStyle(StageStyle.UNDECORATED);
        Button stopButton = (Button) invisibleStage.getScene().lookup("#stopButton");
        stopButton.setOnAction(event -> {
            // this will cause the last click step to be removed from the macro
            trimButtonClick = true;
            SpeechCommandHandler.handleCreateCommand(SpeechCommandHandler.STOP_RECORDING_PHRASE, this);
        });
        // place button in left center of screen
        invisibleStage.setX(primaryScreenBounds.getMinX() + stopButton.getWidth());
        invisibleStage.setY((primaryScreenBounds.getMinY() + primaryScreenBounds.getMaxY()) / 2);
        invisibleStage.setAlwaysOnTop(true);

        invisibleStage.show();
    }


}
