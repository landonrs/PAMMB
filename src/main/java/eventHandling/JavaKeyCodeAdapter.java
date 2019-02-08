package eventHandling;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.SwingKeyAdapter;

/**
 * This class converts the JNativeHook native events into Java Key Events for
 * getting the needed key code.
 */
public class JavaKeyCodeAdapter extends SwingKeyAdapter {

    public JavaKeyCodeAdapter() {
        super();
    }

    public int getJavaKeyCode(NativeKeyEvent e) {
        return getJavaKeyEvent(e).getKeyCode();
    }
}
