package frontEnd;

import eventHandling.EventPerformer;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class VarStepController {


    public TextField varStepValueField;
    public static String varStepValue;
    public Label varStepName;

    public void setValue(ActionEvent actionEvent) {
        varStepValue = "";
        Stage stage = (Stage) varStepValueField.getScene().getWindow();
        stage.hide();
        varStepValue = varStepValueField.getText();
        System.out.println("Value is: " + varStepValue);
    }

    public void cancelCommand(ActionEvent actionEvent) {
        varStepValue = "";
        EventPerformer.stopMacro();
        Stage stage = (Stage) varStepValueField.getScene().getWindow();
        stage.hide();
    }
}
