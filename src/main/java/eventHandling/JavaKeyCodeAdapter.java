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
