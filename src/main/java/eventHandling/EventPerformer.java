package eventHandling;

import macro.Macro;
import macro.Step;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

/**
 * Performs the steps of a macro using Java's Robot class
 */

public class EventPerformer {

    private static final int KEYPRESS_DELAY = 50;
    private static final int KEY_MODIFIER_DELAY = 150;
    private static final int CLICK_MODIFIER_DELAY = 100;
    // determines how quickly the mouse moves from one point to the next
    private static final double MOUSE_MOVE_STEPS = 1000;
    private static Point previousPoint;
    // determines if we show the mouse moving when performing macro
    private static boolean macroMouseVisible;
    // sets delay between clicking events
    private static int macroSecondDelay;

    private static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static boolean performMacro(Macro userMacro) {
        macroMouseVisible = userMacro.isMouseIsVisible();
        macroSecondDelay = userMacro.getSecondDelay();
        //start previousPoint at current location
        previousPoint = new Point(MouseInfo.getPointerInfo().getLocation());

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
       // wait for set delay time
        try {
            TimeUnit.SECONDS.sleep(macroSecondDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //if mouse is visible, first glide the mouse over to the position
        if(macroMouseVisible){
            glideMouse(clickX, clickY);
        }

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
        // wait for set delay time
        try {
            TimeUnit.SECONDS.sleep(macroSecondDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //if mouse is visible, first glide the mouse over to the position
        if(macroMouseVisible){
            glideMouse(clickX, clickY);
        }

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

    private static void glideMouse(int clickX, int clickY) {
        double dx = (clickX - previousPoint.getX()) / MOUSE_MOVE_STEPS;
        double dy = (clickY - previousPoint.getY()) / MOUSE_MOVE_STEPS;
        // smoothly move the mouse from current position to position where click event happens
        for (int step = 1; step <= MOUSE_MOVE_STEPS; step++) {
            robot.mouseMove((int) (previousPoint.getX() + dx * step),
                    (int) (previousPoint.getY() + dy * step));
        }

        // now set previousPoint to currentLocation
        previousPoint = new Point(clickX, clickY);
    }

}
