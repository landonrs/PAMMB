package eventHandling;

import macro.Macro;
import macro.Step;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Performs the steps of a macro using Java's Robot class
 */

public class EventPerformer {

    private static final int KEYPRESS_DELAY = 50;
    private static final int KEY_MODIFIER_DELAY = 150;
    private static final int CLICK_MODIFIER_DELAY = 100;

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
                    typeKeyWithModifiers(macroStep.getKeyCode(), KeyEvent.VK_META);
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
                    typeKeyWithModifiers(macroStep.getKeyCode(), KeyEvent.VK_CONTROL, KeyEvent.VK_META);
                    break;

                case EventTypes.SHIFT_ALT_TYPE:
                    typeKeyWithModifiers(macroStep.getKeyCode(), KeyEvent.VK_SHIFT, KeyEvent.VK_ALT);
                    break;

                case EventTypes.SHIFT_META_TYPE:
                    typeKeyWithModifiers(macroStep.getKeyCode(), KeyEvent.VK_SHIFT, KeyEvent.VK_META);
                    break;

            }
        }

        return true;

    }

    private static void typeKeyWithModifiers(int keyCode, int... modifiers) {
        for(int modifier: modifiers) {
            robot.keyPress(modifier);
        }
        robot.delay(KEY_MODIFIER_DELAY);
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
        robot.delay(KEY_MODIFIER_DELAY);
        for(int modifier: modifiers) {
            robot.keyRelease(modifier);
        }

    }

    private static void typeKeyCommand(int keyCode) {
        robot.keyPress(keyCode);
        robot.delay(KEYPRESS_DELAY);
        robot.keyRelease(keyCode);
        robot.delay(KEYPRESS_DELAY);
    }


    private static void leftClick(int clickX, int clickY, int... modifiers) {
        robot.mouseMove(clickX, clickY);
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
        robot.mouseMove(clickX, clickY);
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

}
