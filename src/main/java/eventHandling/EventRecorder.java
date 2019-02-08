package eventHandling;

import frontEnd.HomeMenuController;
import macro.Macro;
import macro.Step;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import java.util.logging.Level;
import java.util.logging.Logger;


public class EventRecorder extends GlobalScreen implements NativeKeyListener, NativeMouseInputListener {

    private static volatile boolean recordingMacro;
    private static volatile boolean gettingVariableStep;
    // this is set to true whenever the user makes a command that uses the meta or alt key with another action
    private static boolean usingCombinationCommand;
    private static EventRecorder instance = null;
    // this is where we store the user actions each time they record a new macro
    private static Macro currentUserMacro = null;

    private EventRecorder(){
        recordingMacro = false;
        gettingVariableStep = false;
        usingCombinationCommand = false;
        addNativeKeyListener(this);
        addNativeMouseListener(this);

        // Get the logger for "org.jnativehook" and set the level to warning.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.SEVERE);
        // disable the parent handlers.
        logger.setUseParentHandlers(false);
    }

    public static EventRecorder getInstance(){
        if (instance == null) {
            instance = new EventRecorder();
            return instance;
        }
        else
            return instance;
    }

    public Macro recordUserMacro() {
        recordingMacro = true;
        currentUserMacro = new Macro();
        try {
            registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }


        while(recordingMacro) {
            if(gettingVariableStep){
                String varStepName = HomeMenuController.getVariableStepValue();
                System.out.println("getting var step: " + varStepName);
                gettingVariableStep = false;
            }
        }

        try {
            unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }

        return currentUserMacro;

    }

    public static void stopRecording() {
        System.out.println("Stopping recording");
        recordingMacro = false;
    }

    public static void createVariableStep() {
        System.out.println("Creating variable step");
        gettingVariableStep = true;
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        //System.out.println("User typed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // ctrl + shift command
        if(((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) &&
                ((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0)) {
            System.out.println("Ctrl + Shift + __" + NativeKeyEvent.getKeyText(e.getKeyCode()) + "__");
            Step userStep = new Step(EventTypes.CTRL_SHIFT_TYPE, e.getKeyCode());
            currentUserMacro.getSteps().add(userStep);
            usingCombinationCommand = true;
        }
        // ctrl + alt command
        else if(((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) &&
                ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0)) {
            System.out.println("Ctrl + Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            Step userStep = new Step(EventTypes.CTRL_ALT_TYPE, e.getKeyCode());
            currentUserMacro.getSteps().add(userStep);
            usingCombinationCommand = true;

        }
        // ctrl + meta command
        else if(((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) &&
                ((e.getModifiers() & NativeKeyEvent.META_MASK) != 0)) {
            System.out.println("Ctrl + Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            Step userStep = new Step(EventTypes.CTRL_META_TYPE, e.getKeyCode());
            currentUserMacro.getSteps().add(userStep);
            usingCombinationCommand = true;

        }
        // shift + alt command
        else if(((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) &&
                ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0)) {
            System.out.println("Shift + Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            Step userStep = new Step(EventTypes.SHIFT_ALT_TYPE, e.getKeyCode());
            currentUserMacro.getSteps().add(userStep);
            usingCombinationCommand = true;

        }
        // shift + meta command
        else if(((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) &&
                ((e.getModifiers() & NativeKeyEvent.META_MASK) != 0)) {
            System.out.println("Shift + Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            Step userStep = new Step(EventTypes.SHIFT_META_TYPE, e.getKeyCode());
            currentUserMacro.getSteps().add(userStep);
            usingCombinationCommand = true;

        }
        // ctrl + key
        else if (((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) && e.getKeyCode() != NativeKeyEvent.VC_SHIFT
                && e.getKeyCode() != NativeKeyEvent.VC_ALT && e.getKeyCode() != NativeKeyEvent.VC_META) {
            System.out.println("Ctrl + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            Step userStep = new Step(EventTypes.CTRL_TYPE, e.getKeyCode());
            currentUserMacro.getSteps().add(userStep);
        }
        // shift + key
        else if(((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) && e.getKeyCode() != NativeKeyEvent.VC_ALT
                && e.getKeyCode() != NativeKeyEvent.VC_META && e.getKeyCode() != NativeKeyEvent.VC_CONTROL) {
            System.out.println("Shift + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            Step userStep = new Step(EventTypes.SHIFT_TYPE, e.getKeyCode());
            currentUserMacro.getSteps().add(userStep);
        }
        // alt + key
        else if(((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) &&
                e.getKeyCode() != NativeKeyEvent.VC_CONTROL && e.getKeyCode() != NativeKeyEvent.VC_SHIFT) {
            System.out.println("Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            Step userStep = new Step(EventTypes.ALT_TYPE, e.getKeyCode());
            currentUserMacro.getSteps().add(userStep);
            usingCombinationCommand = true;
        }
        // meta + key
        else if((((e.getModifiers() & NativeKeyEvent.META_MASK) != 0))) {
            System.out.println("Meta + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            Step userStep = new Step(EventTypes.META_TYPE, e.getKeyCode());
            currentUserMacro.getSteps().add(userStep);
            usingCombinationCommand = true;
        }
        // key typed without modifiers
        else if (e.getKeyCode() != NativeKeyEvent.VC_CONTROL && e.getKeyCode() != NativeKeyEvent.VC_SHIFT
                && e.getKeyCode() != 0xe36) {
            // if the user has just pressed a combination of keys(ex meta + r) we don't want the meta or alt key firing
            // as a separate event, so we first verify that the user has not just performed a combo command
            if(usingCombinationCommand){
                usingCombinationCommand = false;
            }
            else{
                // TODO remove following line after testing
                if(NativeKeyEvent.getKeyText(e.getKeyCode()) == "9") {
                    System.out.println("pressed 9, stopping recording");
                    stopRecording();
                    return;
                }
                System.out.println("User typed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
                Step userStep = new Step(EventTypes.TYPE, e.getKeyCode());
                currentUserMacro.getSteps().add(userStep);
            }

        }

    }

    /**
     * Records user click events and stores them in macro. Currently, BOTH MIDDLE BUTTON CLICKS AND RIGHT BUTTON CLICKS
     * REGISTER AS RIGHT BUTTON CLICKS to avoid issues with different systems mapping right mouse buttons with
     * different masks
     * @param e - the event that was fired
     */
    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
        // ctrl + shift left click
        if((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 && (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0 &&
                (e.getButton() == NativeMouseEvent.BUTTON1)) {
            System.out.println("CTRL + SHIFT + LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
            Step userStep = new Step(EventTypes.CTRL_SHIFT_LEFT_CLICK, e.getX(), e.getY());
            currentUserMacro.getSteps().add(userStep);
        }
        // ctrl + alt left click
        else if((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 &&
                ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) && (e.getButton() == NativeMouseEvent.BUTTON1)) {
            System.out.println("CTRL + ALT + LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
            Step userStep = new Step(EventTypes.CTRL_ALT_LEFT_CLICK, e.getX(), e.getY());
            currentUserMacro.getSteps().add(userStep);
            usingCombinationCommand = true;
        }
        // ctrl + shift right click
        else if((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 && (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0 &&
                (e.getButton() == NativeMouseEvent.BUTTON3 || e.getButton() == NativeMouseEvent.BUTTON2)) {
            System.out.println("CTRL + SHIFT + RIGHT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
            Step userStep = new Step(EventTypes.CTRL_SHIFT_RIGHT_CLICK, e.getX(), e.getY());
            currentUserMacro.getSteps().add(userStep);
        }
        // ctrl + alt right click
        else if((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 &&
                ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0)
                && (e.getButton() == NativeMouseEvent.BUTTON3 || e.getButton() == NativeMouseEvent.BUTTON2)) {
            System.out.println("CTRL + ALT + RIGHT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
            Step userStep = new Step(EventTypes.CTRL_ALT_RIGHT_CLICK, e.getX(), e.getY());
            currentUserMacro.getSteps().add(userStep);
            usingCombinationCommand = true;
        }
        // ctrl + left click
        else if((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 && (e.getButton() == NativeMouseEvent.BUTTON1)) {
            System.out.println("CTRL + LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
            Step userStep = new Step(EventTypes.CTRL_LEFT_CLICK, e.getX(), e.getY());
            currentUserMacro.getSteps().add(userStep);
        }
        // shift + left click
        else if((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0 && (e.getButton() == NativeMouseEvent.BUTTON1)) {
            System.out.println("SHIFT + LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
            Step userStep = new Step(EventTypes.SHIFT_LEFT_CLICK, e.getX(), e.getY());
            currentUserMacro.getSteps().add(userStep);
        }
        // ctrl + right click
        else if((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0
                && (e.getButton() == NativeMouseEvent.BUTTON3 || e.getButton() == NativeMouseEvent.BUTTON2)) {
            System.out.println("CTRL + RIGHT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
            Step userStep = new Step(EventTypes.CTRL_RIGHT_CLICK, e.getX(), e.getY());
            currentUserMacro.getSteps().add(userStep);
        }
        // shift + right click
        else if((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0
                && (e.getButton() == NativeMouseEvent.BUTTON3 || e.getButton() == NativeMouseEvent.BUTTON2)) {
            System.out.println("SHIFT + RIGHT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
            Step userStep = new Step(EventTypes.SHIFT_RIGHT_CLICK, e.getX(), e.getY());
            currentUserMacro.getSteps().add(userStep);
        }
        // left click
        else if(e.getButton() == NativeMouseEvent.BUTTON1) {
            System.out.println("MOUSE LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
            Step userStep = new Step(EventTypes.LEFT_CLICK, e.getX(), e.getY());
            currentUserMacro.getSteps().add(userStep);

        }
        // right click
        else if(e.getButton() == NativeMouseEvent.BUTTON3 || e.getButton() == NativeMouseEvent.BUTTON2) {
            System.out.println("MOUSE RIGHT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
            Step userStep = new Step(EventTypes.RIGHT_CLICK, e.getX(), e.getY());
            currentUserMacro.getSteps().add(userStep);
        }


    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {

    }
}
