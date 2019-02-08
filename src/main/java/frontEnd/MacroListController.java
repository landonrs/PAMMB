package frontEnd;

import db.SQLiteDbFacade;
import eventHandling.EventPerformer;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import macro.Macro;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MacroListController implements Initializable {

    private List macroNames = SQLiteDbFacade.getMacroNames();
    public ListView macroList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        macroList.getItems().addAll(macroNames);
    }

    public void runSelectedMacro(ActionEvent actionEvent) {
        String selectedMacro = (String) macroList.getSelectionModel().getSelectedItem();
        System.out.println("Running macro " + selectedMacro);
        Macro userMacro = SQLiteDbFacade.getInstance().loadMacro(selectedMacro);

            EventPerformer.performMacro(userMacro);
    }

    public void deleteSelectedMacro(ActionEvent actionEvent) {
        String selectedMacro = (String) macroList.getSelectionModel().getSelectedItem();
        // if user hasn't selected anything, return
        if(selectedMacro == null) {
            return;
        }
        System.out.println("Deleting " + selectedMacro);
        SQLiteDbFacade.deleteMacro(selectedMacro);
        updateList();

    }

    public void displayHomeMenu(ActionEvent actionEvent) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("HomeView.fxml"));
        ViewLoader.loadPage(loader);
    }

    public void updateList(){
        macroNames = SQLiteDbFacade.getMacroNames();
        macroList.getItems().clear();
        macroList.getItems().addAll(macroNames);
    }
}
