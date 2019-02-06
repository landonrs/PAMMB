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
