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

import Audio.MediaPlayerUtil;
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


/**
 * Records user events using the JNativeHook library and stores them as steps in macros
 *
 * see JNativeHook library for more information about registering native events in Java:
 * https://github.com/kwhat/jnativehook
 */
public class EventRecorder extends GlobalScreen implements NativeKeyListener, NativeMouseInputListener {

    private static volatile boolean recordingMacro;
    // this is set to true whenever the user makes a command that uses the meta or alt key with another action
    private static boolean usingCombinationCommand;
    // used to track when user is dragging mouse
    private static boolean dragging;
    // used to play sound when event is recognized by program
    private static boolean eventRegistered;
    private static boolean playingEventSound;

    private static EventRecorder instance = null;
    // this is where we store the user actions each time they record a new macro
    private static Macro currentUserMacro = null;

    private static JavaKeyCodeAdapter keyCodeAdapter;


    private EventRecorder(){
        recordingMacro = false;
        usingCombinationCommand = false;
        dragging = false;
        eventRegistered = false;
        addNativeKeyListener(this);
        addNativeMouseListener(this);
        addNativeMouseMotionListener(this);

        keyCodeAdapter = new JavaKeyCodeAdapter();

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

    public static void startRecordingUserMacro() {
        recordingMacro = true;
        currentUserMacro = new Macro();

        if(!isNativeHookRegistered()) {
            try {
                registerNativeHook();
            } catch (NativeHookException e) {
                e.printStackTrace();
            }
        }
        MediaPlayerUtil.playActivationSound();

    }

    public static Macro finishRecordingUserMacro(){

            try {
                unregisterNativeHook();
            } catch (NativeHookException e) {
                e.printStackTrace();
            }

        playingEventSound = false;

        return currentUserMacro;
    }

    public static void resumeRecording() {
        recordingMacro = true;
    }


    public static void createVariableStep(String varStepName) {
        System.out.println("Creating variable step: " + varStepName);
        Step userStep = new Step(varStepName);
        currentUserMacro.getSteps().add(userStep);
        // the macro has at least one var step
        currentUserMacro.setVarStep(true);
    }

    /**
     * called when user is creating variable step, we ignore
     * user input until they finish creating the step
     */
    public static void ignoreInput(){
        recordingMacro = false;
    }

    /**
     * if set to true, the program will emit a beep sound each time an event is
     * registered and recorded
     * @param selected the state of the checkbox in the recordingInstructionsView
     */
    public static void setEventSound(boolean selected) {
        playingEventSound = selected;
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {

        if(recordingMacro) {
            // ctrl + shift command
            if (((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) &&
                    ((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0)) {
                System.out.println("Ctrl + Shift + __" + NativeKeyEvent.getKeyText(e.getKeyCode()) + "__");
                Step userStep = new Step(EventTypes.CTRL_SHIFT_TYPE, keyCodeAdapter.getJavaKeyCode(e));
                currentUserMacro.getSteps().add(userStep);
                usingCombinationCommand = true;
                eventRegistered = true;
            }
            // ctrl + alt command
            else if (((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) &&
                    ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0)) {
                System.out.println("Ctrl + Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
                Step userStep = new Step(EventTypes.CTRL_ALT_TYPE, keyCodeAdapter.getJavaKeyCode(e));
                currentUserMacro.getSteps().add(userStep);
                usingCombinationCommand = true;
                eventRegistered = true;

            }
            // ctrl + meta command
            else if (((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) &&
                    ((e.getModifiers() & NativeKeyEvent.META_MASK) != 0)) {
                System.out.println("Ctrl + Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
                Step userStep = new Step(EventTypes.CTRL_META_TYPE, keyCodeAdapter.getJavaKeyCode(e));
                currentUserMacro.getSteps().add(userStep);
                usingCombinationCommand = true;
                eventRegistered = true;

            }
            // shift + alt command
            else if (((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) &&
                    ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0)) {
                System.out.println("Shift + Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
                Step userStep = new Step(EventTypes.SHIFT_ALT_TYPE, keyCodeAdapter.getJavaKeyCode(e));
                currentUserMacro.getSteps().add(userStep);
                usingCombinationCommand = true;
                eventRegistered = true;

            }
            // shift + meta command
            else if (((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) &&
                    ((e.getModifiers() & NativeKeyEvent.META_MASK) != 0)) {
                System.out.println("Shift + Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
                Step userStep = new Step(EventTypes.SHIFT_META_TYPE, keyCodeAdapter.getJavaKeyCode(e));
                currentUserMacro.getSteps().add(userStep);
                usingCombinationCommand = true;
                eventRegistered = true;

            }
            // ctrl + key
            else if (((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) && e.getKeyCode() != NativeKeyEvent.VC_SHIFT
                    && e.getKeyCode() != NativeKeyEvent.VC_ALT && e.getKeyCode() != NativeKeyEvent.VC_META) {
                System.out.println("Ctrl + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
                Step userStep = new Step(EventTypes.CTRL_TYPE, keyCodeAdapter.getJavaKeyCode(e));
                currentUserMacro.getSteps().add(userStep);
                eventRegistered = true;
            }
            // shift + key
            else if (((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) && e.getKeyCode() != NativeKeyEvent.VC_ALT
                    && e.getKeyCode() != NativeKeyEvent.VC_META && e.getKeyCode() != NativeKeyEvent.VC_CONTROL) {
                System.out.println("Shift + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
                Step userStep = new Step(EventTypes.SHIFT_TYPE, keyCodeAdapter.getJavaKeyCode(e));
                currentUserMacro.getSteps().add(userStep);
                eventRegistered = true;
            }
            // alt + key
            else if (((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) &&
                    e.getKeyCode() != NativeKeyEvent.VC_CONTROL && e.getKeyCode() != NativeKeyEvent.VC_SHIFT) {
                System.out.println("Alt + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
                Step userStep = new Step(EventTypes.ALT_TYPE, keyCodeAdapter.getJavaKeyCode(e));
                currentUserMacro.getSteps().add(userStep);
                usingCombinationCommand = true;
                eventRegistered = true;
            }
            // meta + key
            else if ((((e.getModifiers() & NativeKeyEvent.META_MASK) != 0))) {
                System.out.println("Meta + " + NativeKeyEvent.getKeyText(e.getKeyCode()));
                Step userStep = new Step(EventTypes.META_TYPE, keyCodeAdapter.getJavaKeyCode(e));
                currentUserMacro.getSteps().add(userStep);
                usingCombinationCommand = true;
                eventRegistered = true;
            }
            // key typed without modifiers
            else if (e.getKeyCode() != NativeKeyEvent.VC_CONTROL && e.getKeyCode() != NativeKeyEvent.VC_SHIFT
                    && e.getKeyCode() != 0xe36) {
                // if the user has just pressed a combination of keys(ex meta + r) we don't want the meta or alt
                // key firing as a separate event, so we first verify that the user has not just performed a combo
                // command
                if (usingCombinationCommand) {
                    usingCombinationCommand = false;
                } else {
                    System.out.println("User typed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
                    Step userStep = new Step(EventTypes.TYPE, keyCodeAdapter.getJavaKeyCode(e));
                    currentUserMacro.getSteps().add(userStep);
                    eventRegistered = true;
                }

            }
            if(eventRegistered && playingEventSound) {
                MediaPlayerUtil.playEventRegisterSound();
                eventRegistered = false;
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
        if(recordingMacro) {
            // ctrl + shift left click
            if ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 && (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0 &&
                    (e.getButton() == NativeMouseEvent.BUTTON1)) {
                System.out.println("CTRL + SHIFT + LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
                Step userStep = new Step(EventTypes.CTRL_SHIFT_LEFT_CLICK, e.getX(), e.getY());
                currentUserMacro.getSteps().add(userStep);
                eventRegistered = true;
            }
            // ctrl + alt left click
            else if ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 &&
                    ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) && (e.getButton() == NativeMouseEvent.BUTTON1)) {
                System.out.println("CTRL + ALT + LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
                Step userStep = new Step(EventTypes.CTRL_ALT_LEFT_CLICK, e.getX(), e.getY());
                currentUserMacro.getSteps().add(userStep);
                usingCombinationCommand = true;
                eventRegistered = true;
            }
            // ctrl + shift right click
            else if ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 && (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0 &&
                    (e.getButton() == NativeMouseEvent.BUTTON3 || e.getButton() == NativeMouseEvent.BUTTON2)) {
                System.out.println("CTRL + SHIFT + RIGHT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
                Step userStep = new Step(EventTypes.CTRL_SHIFT_RIGHT_CLICK, e.getX(), e.getY());
                currentUserMacro.getSteps().add(userStep);
                eventRegistered = true;
            }
            // ctrl + alt right click
            else if ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 &&
                    ((e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0)
                    && (e.getButton() == NativeMouseEvent.BUTTON3 || e.getButton() == NativeMouseEvent.BUTTON2)) {
                System.out.println("CTRL + ALT + RIGHT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
                Step userStep = new Step(EventTypes.CTRL_ALT_RIGHT_CLICK, e.getX(), e.getY());
                currentUserMacro.getSteps().add(userStep);
                usingCombinationCommand = true;
                eventRegistered = true;
            }
            // ctrl + left click
            else if ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 && (e.getButton() == NativeMouseEvent.BUTTON1)) {
                System.out.println("CTRL + LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
                Step userStep = new Step(EventTypes.CTRL_LEFT_CLICK, e.getX(), e.getY());
                currentUserMacro.getSteps().add(userStep);
                eventRegistered = true;
            }
            // shift + left click
            else if ((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0 && (e.getButton() == NativeMouseEvent.BUTTON1)) {
                System.out.println("SHIFT + LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
                Step userStep = new Step(EventTypes.SHIFT_LEFT_CLICK, e.getX(), e.getY());
                currentUserMacro.getSteps().add(userStep);
                eventRegistered = true;
            }
            // ctrl + right click
            else if ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0
                    && (e.getButton() == NativeMouseEvent.BUTTON3 || e.getButton() == NativeMouseEvent.BUTTON2)) {
                System.out.println("CTRL + RIGHT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
                Step userStep = new Step(EventTypes.CTRL_RIGHT_CLICK, e.getX(), e.getY());
                currentUserMacro.getSteps().add(userStep);
                eventRegistered = true;
            }
            // shift + right click
            else if ((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0
                    && (e.getButton() == NativeMouseEvent.BUTTON3 || e.getButton() == NativeMouseEvent.BUTTON2)) {
                System.out.println("SHIFT + RIGHT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
                Step userStep = new Step(EventTypes.SHIFT_RIGHT_CLICK, e.getX(), e.getY());
                currentUserMacro.getSteps().add(userStep);
                eventRegistered = true;
            }
            // left click
            else if (e.getButton() == NativeMouseEvent.BUTTON1) {
                System.out.println("MOUSE LEFT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
                Step userStep = new Step(EventTypes.LEFT_CLICK, e.getX(), e.getY());
                currentUserMacro.getSteps().add(userStep);
                eventRegistered = true;

            }
            // right click
            else if (e.getButton() == NativeMouseEvent.BUTTON3 || e.getButton() == NativeMouseEvent.BUTTON2) {
                System.out.println("MOUSE RIGHT CLICKED AT COORDINATES: X" + e.getX() + " Y" + e.getY());
                Step userStep = new Step(EventTypes.RIGHT_CLICK, e.getX(), e.getY());
                currentUserMacro.getSteps().add(userStep);
                eventRegistered = true;
            }

            if(eventRegistered && playingEventSound) {
                MediaPlayerUtil.playEventRegisterSound();
                eventRegistered = false;
            }
        }


    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        if(dragging) {
            System.out.println("MOUSE FINISHED DRAGGING AT COORDINATES: X" + e.getX() + " Y" + e.getY());
            Step userStep = new Step(EventTypes.DRAG_FINISH, e.getX(), e.getY());
            currentUserMacro.getSteps().add(userStep);
            dragging = false;
        }

    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {
        if(recordingMacro){
            // add new step when user first starts dragging
            if (!dragging) {
                System.out.println("MOUSE STARTED DRAGGING AT COORDINATES: X" + e.getX() + " Y" + e.getY());
                Step userStep = new Step(EventTypes.DRAG_START, e.getX(), e.getY());
                currentUserMacro.getSteps().add(userStep);
                dragging = true;
                if(playingEventSound) {
                    MediaPlayerUtil.playEventRegisterSound();
                }
            }
        }

    }
}
