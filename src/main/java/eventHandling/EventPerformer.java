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

package eventHandling;

import frontEnd.ViewLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import macro.Macro;
import macro.Step;
import speechHandling.SpeechCommandHandler;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

/**
 * Performs the steps of a macro using Java's Robot class
 */

public class EventPerformer {

    private static final int KEYPRESS_DELAY = 50;
    private static final int KEY_MODIFIER_DELAY = 200;
    private static final int CLICK_MODIFIER_DELAY = 100;
    // delay used every time a key typing event occurs with modifiers to give system time to process
    private static final int COMBO_TYPE_DELAY = 250;

    // determines how quickly the mouse moves from one point to the next
    private static final double MOUSE_MOVE_STEPS = 1000;
    private static Point previousPoint;

    //used to return mouse to user's starting location after performing macro
    private static Point startingPoint;
    // determines if we show the mouse moving when performing macro
    private static boolean macroMouseVisible;
    // sets delay between clicking events
    private static int macroSecondDelay;
    // used to prevent delay for first clicking action
    private static boolean firstClickStep;
    // set to true if user cancels macro while it is running
    private static volatile boolean macroCancelled = false;

    private static Robot robot;
    private static int SYSTEM_META_KEY;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        if(runningWindows()) {
            SYSTEM_META_KEY = KeyEvent.VK_WINDOWS;
        }
        else {
            SYSTEM_META_KEY = KeyEvent.VK_META;
        }

    }

    public static void performMacro(Macro userMacro) {
        macroMouseVisible = userMacro.isMouseIsVisible();
        macroSecondDelay = userMacro.getSecondDelay();

        if(userMacro.getSteps().get(0).getType().contains("CLICK")) {
            // set this to true so first click step is not affected by delay
            firstClickStep = true;
        }

        //if the macro has any variable steps, get the values for those steps
        if (userMacro.getVarStep()) {
            setVarStepValues(userMacro);
        }
        //set starting point so we can return after macro is completed
        startingPoint = new Point(MouseInfo.getPointerInfo().getLocation());
        //start previousPoint at current location for mouse gliding animation
        previousPoint = new Point(MouseInfo.getPointerInfo().getLocation());

        for(Step macroStep: userMacro.getSteps()) {
            // check to see if user has cancelled macro
            if(macroCancelled) {
                // reset value and exit
                macroCancelled = false;
                return;
            }
            switch (macroStep.getType()) {

                case EventTypes.LEFT_CLICK:
                    leftClick(macroStep.getClickX(), macroStep.getClickY());
                    break;

                case EventTypes.RIGHT_CLICK:
                    rightClick(macroStep.getClickX(), macroStep.getClickY());
                    break;

                case EventTypes.CTRL_LEFT_CLICK:
                    leftClick(macroStep.getClickX(), macroStep.getClickY(), KeyEvent.VK_CONTROL);
                    break;

                case EventTypes.SHIFT_LEFT_CLICK:
                    leftClick(macroStep.getClickX(), macroStep.getClickY(), KeyEvent.VK_SHIFT);
                    break;

                case EventTypes.CTRL_SHIFT_LEFT_CLICK:
                    leftClick(macroStep.getClickX(), macroStep.getClickY(), KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT);
                    break;

                case EventTypes.TYPE:
                    typeKeyCommand(macroStep.getKeyCode());
                    break;

                case EventTypes.SHIFT_TYPE:
                    typeKeyWithModifiers(macroStep.getKeyCode(), KeyEvent.VK_SHIFT);
                    break;

                case EventTypes.CTRL_TYPE:
                    typeKeyWithModifiers(macroStep.getKeyCode(), KeyEvent.VK_CONTROL);
                    break;

                case EventTypes.ALT_TYPE:
                    typeKeyWithModifiers(macroStep.getKeyCode(), KeyEvent.VK_ALT);
                    break;

                case EventTypes.META_TYPE:
                    typeKeyWithModifiers(macroStep.getKeyCode(), SYSTEM_META_KEY);
                    break;

                case EventTypes.CTRL_SHIFT_TYPE:
                    typeKeyWithModifiers(macroStep.getKeyCode(), KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT);
                    break;

                case EventTypes.CTRL_ALT_TYPE:
                    typeKeyWithModifiers(macroStep.getKeyCode(), KeyEvent.VK_CONTROL, KeyEvent.VK_ALT);
                    break;

                case EventTypes.CTRL_ALT_LEFT_CLICK:
                    leftClick(macroStep.getClickX(), macroStep.getClickY(), KeyEvent.VK_CONTROL, KeyEvent.VK_ALT);
                    break;

                case EventTypes.CTRL_RIGHT_CLICK:
                    rightClick(macroStep.getClickX(), macroStep.getClickY(), KeyEvent.VK_CONTROL);
                    break;

                case EventTypes.SHIFT_RIGHT_CLICK:
                    rightClick(macroStep.getClickX(), macroStep.getClickY(), KeyEvent.VK_SHIFT);
                    break;

                case EventTypes.CTRL_SHIFT_RIGHT_CLICK:
                    rightClick(macroStep.getClickX(), macroStep.getClickY(), KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT);
                    break;

                case EventTypes.CTRL_ALT_RIGHT_CLICK:
                    rightClick(macroStep.getClickX(), macroStep.getClickY(), KeyEvent.VK_CONTROL, KeyEvent.VK_ALT);
                    break;

                case EventTypes.CTRL_META_TYPE:
                    typeKeyWithModifiers(macroStep.getKeyCode(), KeyEvent.VK_CONTROL, SYSTEM_META_KEY);
                    break;

                case EventTypes.SHIFT_ALT_TYPE:
                    typeKeyWithModifiers(macroStep.getKeyCode(), KeyEvent.VK_SHIFT, KeyEvent.VK_ALT);
                    break;

                case EventTypes.SHIFT_META_TYPE:
                    typeKeyWithModifiers(macroStep.getKeyCode(), KeyEvent.VK_SHIFT, SYSTEM_META_KEY);
                    break;

                case EventTypes.VAR_STEP:
                    insertVarStepValue(macroStep.getVariableStepValue());
                    break;

                case EventTypes.DRAG_START:
                    startDraggingEvent(macroStep.getClickX(), macroStep.getClickY());
                    break;

                case EventTypes.DRAG_FINISH:
                    completeDraggingEvent(macroStep.getClickX(), macroStep.getClickY());
                    break;

            }
        }

        // return mouse to user's starting point
        robot.mouseMove((int) startingPoint.getX(), (int) startingPoint.getY());

    }

    private static boolean runningWindows() {
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }

    /**
     * uses the system clipboard to insert user's variable step value into the macro
     *
     * @param variableStepValue
     */
    private static void insertVarStepValue(String variableStepValue) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable previousContents = clipboard.getContents(null);

        StringSelection variableStepSelection = new StringSelection(variableStepValue);

        clipboard.setContents(variableStepSelection, variableStepSelection);

        robot.delay(100);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.delay(100);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        // let contents be pasted before resetting clipboard contents
        robot.delay(200);

        StringSelection stringSelection = new StringSelection(variableStepValue);
        // now reset clipboard contents to previous value
        clipboard.setContents(previousContents, stringSelection);
    }

    /**
     * gets the values for each variable step in the macro from the user
     *
     * @param userMacro
     */
    private static void setVarStepValues(Macro userMacro) {
        for (Step userStep: userMacro.getSteps()) {
            if (userStep.getType().equals(EventTypes.VAR_STEP)) {
                userStep.setVariableStepValue(ViewLoader.displayVarStepValueView(userStep.getVariableStepName()));
                System.out.println("set value to " + userStep.getVariableStepValue());
                // check if user cancelled macro
                if(macroCancelled) {
                    // exit so calling function will also exit without performing macro
                    return;
                }
            }
        }
    }

    private static void typeKeyWithModifiers(int keyCode, int... modifiers) {
        int modCount = 0;
        // first try pressing all the mod keys
        try {
            for (int modifier : modifiers) {
                robot.keyPress(modifier);
                modCount++;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            //release any valid modifier keys that were pressed
            for(int i = 0; i < modCount; i++) {
                robot.keyRelease(modifiers[i]);
            }
            showMacroErrorMessage();
        }
        // try pressing base key
        try {
            robot.delay(KEY_MODIFIER_DELAY);
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
            robot.delay(KEY_MODIFIER_DELAY);
            for(int modifier: modifiers) {
                robot.keyRelease(modifier);
            }

            // give system time to process command
            robot.delay(COMBO_TYPE_DELAY);
        }
        catch (Exception e){
            e.printStackTrace();
            for(int modifier: modifiers) {
                robot.keyRelease(modifier);
            }
            showMacroErrorMessage();
        }


    }


    private static void typeKeyCommand(int keyCode) {
        try {
            robot.keyPress(keyCode);
            robot.delay(KEYPRESS_DELAY);
            robot.keyRelease(keyCode);
            robot.delay(KEYPRESS_DELAY);
        }
        catch (Exception e){
            e.printStackTrace();
            showMacroErrorMessage();
        }
        finally {
            robot.keyRelease(keyCode);
        }

    }

    private static void showMacroErrorMessage() {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR,
                "An error occured while running this macro and was unable to finish", ButtonType.OK);
        macroCancelled = true;
        errorAlert.showAndWait();
        //in case the error occurred while user was running macro mode, we need to stop the speech recognition thread
        SpeechCommandHandler.stopAssistantMode();
        ViewLoader.showPrimaryStage();
    }


    private static void leftClick(int clickX, int clickY, int... modifiers) {
       moveMouseToPosition(clickX, clickY);
        for(int modifier: modifiers) {
            robot.keyPress(modifier);
        }
        robot.delay(CLICK_MODIFIER_DELAY);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.delay(CLICK_MODIFIER_DELAY);
        for(int modifier: modifiers) {
            robot.keyRelease(modifier);
        }
    }

    private static void rightClick(int clickX, int clickY, int... modifiers) {
        moveMouseToPosition(clickX, clickY);
        for(int modifier: modifiers) {
            robot.keyPress(modifier);
        }
        robot.delay(CLICK_MODIFIER_DELAY);
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        robot.delay(CLICK_MODIFIER_DELAY);
        for(int modifier: modifiers) {
            robot.keyRelease(modifier);
        }
    }

    /**
     * smoothly moves the mouse cursor from one position to another
     *
     * @param clickX the x coordinate the mouse is moving to
     * @param clickY the y coordinate the mouse is moving to
     */
    private static void glideMouse(int clickX, int clickY) {
        double dx = (clickX - previousPoint.getX()) / MOUSE_MOVE_STEPS;
        double dy = (clickY - previousPoint.getY()) / MOUSE_MOVE_STEPS;
        // smoothly move the mouse from current position to position where click event happens
        for (int step = 1; step <= MOUSE_MOVE_STEPS; step++) {
            robot.mouseMove((int) (previousPoint.getX() + dx * step),
                    (int) (previousPoint.getY() + dy * step));
        }

        // now set previousPoint to current location
        previousPoint = new Point(clickX, clickY);
    }

    private static void startDraggingEvent(int clickX, int clickY) {
        moveMouseToPosition(clickX, clickY);
        robot.mousePress(InputEvent.BUTTON1_MASK);
    }

    private static void completeDraggingEvent(int xCoord, int yCoord) {
        dragMouseToPosition(xCoord, yCoord);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    private static void dragMouseToPosition(int clickX, int clickY) {
        glideMouse(clickX, clickY);
        robot.mouseMove(clickX, clickY);
    }

    private static void moveMouseToPosition(int clickX, int clickY) {
        if(!firstClickStep){
            try {
                TimeUnit.SECONDS.sleep(macroSecondDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // do not delay with first click step
        else {
            firstClickStep = false;
        }
        //if mouse is visible, first glide the mouse over to the position
        if(macroMouseVisible){
            glideMouse(clickX, clickY);
        }
        robot.mouseMove(clickX, clickY);
    }

    public static void stopMacro() {
        macroCancelled = true;
    }
}
