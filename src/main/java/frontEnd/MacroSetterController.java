package frontEnd;

import db.SQLiteDbFacade;
import eventHandling.EventRecorder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import macro.Macro;
import macro.MacroSettings;
import macro.Step;
import speechHandling.SpeechCommandHandler;

import java.util.concurrent.CompletableFuture;


public class MacroSetterController {

    public Label warningLabel;
    public TextField macroName;
    public Slider secondSlider;
    public CheckBox mouseVisibleBox;

    private SQLiteDbFacade dbFacade = SQLiteDbFacade.getInstance();

    public void recordUserEvents(Stage stage) {
        EventRecorder recorder = EventRecorder.getInstance();
        SpeechCommandHandler speechCommandHandler = SpeechCommandHandler.getInstance();
        // set up speech command handling on separate thread
        // TODO uncomment after completing event handling implementation
//        CompletableFuture recordingCommands = CompletableFuture.runAsync(() -> {
//            speechCommandHandler.runCreateMode();
//        });
        MacroSettings.currentMacro = recorder.recordUserMacro();
        System.out.println("Finished recording macro with steps: ");
        for(Step step: MacroSettings.currentMacro.getSteps()) {
            System.out.println(step.getType());
        }
        stage.show();
        stage.toFront();
    }


    @FXML
    public void checkMacroName(ActionEvent actionEvent) throws Exception{
        if(dbFacade.uniqueMacroName(macroName.getText())) {
            System.out.println("Name is unique: " + macroName.getText());
            MacroSettings.setMacroName(macroName.getText());
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
            System.out.println("Saved macro " + userMacro.getName() + " with " + secondDelay + " second delay and visible set to " + !checked);
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
}
