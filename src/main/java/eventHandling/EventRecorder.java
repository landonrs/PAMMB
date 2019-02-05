package eventHandling;

import frontEnd.HomeMenuController;
import macro.Macro;
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
    private static boolean usingCombinationCommand;
    private static EventRecorder instance = null;

    private EventRecorder(){
        recordingMacro = false;
        gettingVariableStep = false;
        usingCombinationCommand = false;
        addNativeKeyListener(this);
        addNativeMouseListener(this);

        // Get the logger for "org.jnativehook" and set the level to warning.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.SEVERE);
        // Don't forget to disable the parent handlers.
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
        Macro userMacro = null;
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

        return userMacro;

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
            usingCombinationCommand = true;
        }
        // ctrl + alt command
        else if(((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) &&
                ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0)) {
            System.out.println("Ctrl + Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            usingCombinationCommand = true;

        }
        // ctrl + meta command
        else if(((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) &&
                ((e.getModifiers() & NativeKeyEvent.META_MASK) != 0)) {
            System.out.println("Ctrl + Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            usingCombinationCommand = true;

        }
        // shift + alt command
        else if(((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) &&
                ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0)) {
            System.out.println("Shift + Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            usingCombinationCommand = true;

        }
        // shift + meta command
        else if(((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) &&
                ((e.getModifiers() & NativeKeyEvent.META_MASK) != 0)) {
            System.out.println("Shift + Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            usingCombinationCommand = true;

        }
        // ctrl + key
        else if (((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) && e.getKeyCode() != NativeKeyEvent.VC_SHIFT
                && e.getKeyCode() != NativeKeyEvent.VC_ALT && e.getKeyCode() != NativeKeyEvent.VC_META) {
            System.out.println("Ctrl + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        }
        // shift + key
        else if(((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) && e.getKeyCode() != NativeKeyEvent.VC_ALT
                && e.getKeyCode() != NativeKeyEvent.VC_META) {
            System.out.println("Shift + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        }
        // alt + key
        else if(((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) &&
                e.getKeyCode() != NativeKeyEvent.VC_CONTROL && e.getKeyCode() != NativeKeyEvent.VC_SHIFT) {
            System.out.println("Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            usingCombinationCommand = true;
        }
        // meta + key
        else if((((e.getModifiers() & NativeKeyEvent.META_MASK) != 0))) {
            System.out.println("Meta + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
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
                System.out.println("User typed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            }

        }

    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
        // ctrl + shift left click
        if((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 && (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0 &&
                (e.getButton() == NativeMouseEvent.BUTTON1)) {
            System.out.println("CTRL + SHIFT + LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
        }
        // ctrl + alt left click
        else if((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 &&
                ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) && (e.getButton() == NativeMouseEvent.BUTTON1)) {
            System.out.println("CTRL + ALT + LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
            usingCombinationCommand = true;
        }
        // ctrl + left click
        else if((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 && (e.getButton() == NativeMouseEvent.BUTTON1)) {
            System.out.println("CTRL + LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
        }
        // shift + left click
        else if((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0 && (e.getButton() == NativeMouseEvent.BUTTON1)) {
            System.out.println("SHIFT + LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
        }
        // ctrl + right click
        else if((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 && (e.getButton() == NativeMouseEvent.BUTTON3)) {
            System.out.println("CTRL + RIGHT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
        }
        // shift + right click
        else if((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0 && (e.getButton() == NativeMouseEvent.BUTTON3)) {
            System.out.println("SHIFT + RIGHT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
        }
        // left click
        else if(e.getButton() == NativeMouseEvent.BUTTON1) {
            System.out.println("MOUSE LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
        }
        // right click
        else if(e.getButton() == NativeMouseEvent.BUTTON3) {
            System.out.println("MOUSE RIGHT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
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
