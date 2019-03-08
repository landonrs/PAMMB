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

/**
 * This class is used to track the user settings for a macro between controllers
 * whenever the user creates a new macro
 */
public class MacroSettings {

    // the delay in seconds between clicking actions
    private static int secondDelay = 1;
    // determines whether the mouse will be visible when the macro is being performed
    private static boolean mouseIsVisible = true;
    // the name of the macro being created
    private static String macroName = "";

    public static Macro currentMacro = null;

    public static int getSecondDelay() {
        return secondDelay;
    }

    public static void setSecondDelay(int secondDelay) {
        MacroSettings.secondDelay = secondDelay;
    }

    public static boolean isMouseIsVisible() {
        return mouseIsVisible;
    }

    public static void setMouseIsVisible(boolean mouseIsVisible) {
        MacroSettings.mouseIsVisible = mouseIsVisible;
    }

    public static String getMacroName() {
        return macroName;
    }

    public static void setMacroName(String macroName) {
        MacroSettings.macroName = macroName;
    }

    public static Macro configureMacroSettings(){
        currentMacro.setSecondDelay(secondDelay);
        currentMacro.setMouseIsVisible(mouseIsVisible);
        currentMacro.setName(macroName);
        return currentMacro;
    }

    /**
     * set values to default for next time macro is created
     */
    public static void resetValues(){
        secondDelay = 1;
        mouseIsVisible = true;
        macroName = "";
    }
}
