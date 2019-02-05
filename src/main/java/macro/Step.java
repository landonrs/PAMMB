package macro;

import javax.persistence.*;

@Entity
@Table(name = "steps")
public class Step {


    @Id
    @Column(name="ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected int id;

    protected String type;

    protected int keyCode;

    protected int clickX;

    protected int clickY;

    protected String variableStepName;

    protected String variableStepValue;

    public Step(){}

    // constructor for key typing events
    public Step (String type, int keyCode){
        this.type = type;
        this.keyCode = keyCode;

        // key steps do not use click coordinates
        this.clickX = 0;
        this.clickY = 0;
        this.variableStepName = "";
        this.variableStepValue = "";
    }

    // constructor for clicking events
    public Step(String type, int clickX, int clickY) {
        this.type = type;
        this.clickX = clickX;
        this.clickY = clickY;

        // click steps do not have a key code
        this.keyCode = 0;
        this.variableStepName = "";
        this.variableStepValue = "";
    }

    // constructor for variable steps
    public Step(String variableStepName) {
        this.type = "VAR_STEP";
        this.variableStepName = variableStepName;
        // this will be set to a different value each time the user runs the macro
        this.variableStepValue = "";

        // var step do not use coordinates or key codes
        this.clickX = 0;
        this.clickY = 0;
        this.keyCode = 0;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getClickX() {
        return clickX;
    }

    public void setClickX(int clickX) {
        this.clickX = clickX;
    }

    public int getClickY() {
        return clickY;
    }

    public void setClickY(int clickY) {
        this.clickY = clickY;
    }

    public String getVariableStepName() {
        return variableStepName;
    }

    public void setVariableStepName(String variableStepName) {
        this.variableStepName = variableStepName;
    }

    public String getVariableStepValue() {
        return variableStepValue;
    }

    public void setVariableStepValue(String variableStepValue) {
        this.variableStepValue = variableStepValue;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
