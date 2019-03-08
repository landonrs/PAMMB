/* PAMM: Personal Assistant Macro Maker.
 * Copyright (C) 2019 Landon Shumway.
 * https://github.com/landonrs/PAMMB
 *
 * PAMM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PAMM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package macro;

import javax.persistence.*;
import java.util.ArrayList;
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
    private List<Step> steps;

    private int secondDelay;
    private boolean mouseIsVisible;

    public Macro() {
        steps = new ArrayList<>();

    }

    public Macro(String name, List<Step> steps, boolean varStep) {
        this.name = name;
        this.steps = steps;
        this.varStep = varStep;

        // default values
        this.secondDelay = 1;
        this.mouseIsVisible = true;
    }

    @Id
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(cascade = {CascadeType.ALL, CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinColumn(name = "name")
    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public boolean getVarStep() {
        return varStep;
    }

    public void setVarStep(boolean varStep) {
        this.varStep = varStep;
    }

    public int getSecondDelay() {
        return secondDelay;
    }

    public void setSecondDelay(int secondDelay) {
        this.secondDelay = secondDelay;
    }

    public boolean isMouseIsVisible() {
        return mouseIsVisible;
    }

    public void setMouseIsVisible(boolean mouseIsVisible) {
        this.mouseIsVisible = mouseIsVisible;
    }
}