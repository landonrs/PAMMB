package frontEnd;

import db.SQLiteDbFacade;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MacroListController implements Initializable {

    List<String> macroNames = SQLiteDbFacade.getMacroNames();
    public ListView macroList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        macroList.getItems().addAll(macroNames);
    }

    public void runSelectedMacro(ActionEvent actionEvent) {

    }

    public void deleteSelectedMacro(ActionEvent actionEvent) {

    }

    public void displayHomeMenu(ActionEvent actionEvent) {

    }
}
