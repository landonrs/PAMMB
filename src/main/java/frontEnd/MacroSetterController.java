package frontEnd;

import db.SQLiteDbFacade;
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
import java.util.concurrent.TimeUnit;


public class MacroSetterController {

    public Label warningLabel;
    public TextField macroName;
    public Slider secondSlider;
    public TextField varStepNameField;
    public CheckBox mouseVisibleBox;
    public Button varStepNameCancel;

    private SQLiteDbFacade dbFacade = SQLiteDbFacade.getInstance();

    public void recordUserEvents(Stage stage) {
        SpeechCommandHandler speechCommandHandler = SpeechCommandHandler.getInstance();
        // set up speech command handling on separate thread
        // TODO uncomment after completing event handling implementation
//        CompletableFuture recordingCommands = CompletableFuture.runAsync(() -> {
//            speechCommandHandler.runCreateMode();
//        });
        // this prevents the JavaFX platform thread from terminating
        Platform.setImplicitExit( false );
        EventRecorder.startRecordingUserMacro(this);

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

    public void getVariableStep() {
        EventRecorder.pauseRecording();
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
        if(dbFacade.uniqueMacroName(macroName.getText())) {
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
        int secondDelay = (int) secondSlider.getValue();
        MacroSettings.setSecondDelay(secondDelay);
        boolean checked = mouseVisibleBox.isSelected();
        MacroSettings.setMouseIsVisible(!checked);
        // for testing on Ubuntu
        //MacroSettings.currentMacro = new Macro();
        Macro userMacro = MacroSettings.configureMacroSettings();
        boolean saved = dbFacade.saveMacro(userMacro);
        if(saved) {
            System.out.println("Saved macro " + userMacro.getName() + " with " +
                    secondDelay + " second delay and visible set to " + !checked);
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
}
