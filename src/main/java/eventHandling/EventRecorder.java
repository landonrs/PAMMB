package eventHandling;

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
    private static EventRecorder instance = null;

    private EventRecorder(){
        recordingMacro = false;
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
                System.out.println("getting var step");
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
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
        System.out.println("MOUSE LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());

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
