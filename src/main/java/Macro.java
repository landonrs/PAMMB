import javax.persistence.*;
import java.util.List;

/**
 * This macro class holds the info for a user created macro. It consists of a name
 * which the user sets and this name becomes the unique id for the macro. It also
 * holds a list of steps which the program uses to determine which actions to perform
 *
 */
@Entity
@Table(name = "macros")
public class Macro {
    // unique name for macro
    private String name;
    // set true if macro has a variable step
    private boolean varStep;
    // list of actions to perform for macro
    private List<String> steps;

    public Macro() {

    }

    public Macro(String name, List<String> steps, boolean varStep) {
        this.name = name;
        this.steps = steps;
        this.varStep = varStep;
    }

    @Id
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ElementCollection
    @CollectionTable(name="steps", joinColumns=@JoinColumn(name="name"))
    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public boolean hasVarStep() {
        return varStep;
    }

    public void setVarStep(boolean varStep) {
        this.varStep = varStep;
    }
}