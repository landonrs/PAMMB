package eventHandling;

import macro.Macro;
import macro.Step;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Performs the steps of a macro using Java's Robot class
 */

public class EventPerformer {

    private static final int KEYPRESS_DELAY = 50;

    private static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static boolean performMacro(Macro userMacro) {

        for(Step macroStep: userMacro.getSteps()) {
            switch (macroStep.getType()) {
                case EventTypes.LEFT_CLICK:
                    leftClick(macroStep.getClickX(), macroStep.getClickY());
                    break;
                case EventTypes.RIGHT_CLICK:
                    rightClick(macroStep.getClickX(), macroStep.getClickY());
                    break;
                case EventTypes.TYPE:
                    typeKey(macroStep.getKeyCode());
                    break;

            }
        }

        return true;

    }

    private static void typeKey(int keyCode) {
        robot.keyPress(keyCode);
        robot.delay(KEYPRESS_DELAY);
        robot.keyRelease(keyCode);
        robot.delay(KEYPRESS_DELAY);
    }


    private static void leftClick(int clickX, int clickY) {
        robot.mouseMove(clickX, clickY);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    private static void rightClick(int clickX, int clickY) {
        robot.mouseMove(clickX, clickY);
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }

}
