public class VariableStep extends Step {

    // the name the user enters to remember what the var step does
    private String variableStepName;
    // this holds the variable text value the user enters each time they run the macro
    private String variableStepText;

    public VariableStep(String variableStepName) {
        this.type = "VARIABLE";
        this.variableStepName = variableStepName;
        // this is not set until the user runs the command
        this.variableStepText = "";
    }

    public String getVariableStepName() {
        return variableStepName;
    }

    public String getVariableStepText() {
        return variableStepText;
    }

    public void setVariableStepText(String variableStepText) {
        this.variableStepText = variableStepText;
    }
}
