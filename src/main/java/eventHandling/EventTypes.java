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

/**
 * Holds constants for all types of events that can be saved into a macro
 */
class EventTypes {

    // Clicking events
    static final String LEFT_CLICK = "LEFT_CLICK";
    static final String CTRL_LEFT_CLICK = "CTRL_LEFT_CLICK";
    static final String SHIFT_LEFT_CLICK = "SHIFT_LEFT_CLICK";
    static final String CTRL_SHIFT_LEFT_CLICK = "CTRL_SHIFT_LEFT_CLICK";
    static final String CTRL_ALT_LEFT_CLICK = "CTRL_ALT_LEFT_CLICK";
    static final String RIGHT_CLICK = "RIGHT_CLICK";
    static final String CTRL_RIGHT_CLICK = "CTRL_RIGHT_CLICK";
    static final String SHIFT_RIGHT_CLICK = "SHIFT_RIGHT_CLICK";
    static final String CTRL_SHIFT_RIGHT_CLICK = "CTRL_SHIFT_RIGHT_CLICK";
    static final String CTRL_ALT_RIGHT_CLICK = "CTRL_ALT_RIGHT_CLICK";
    // Dragging Events
    static final String DRAG_START = "DRAG_START";
    static final String DRAG_FINISH = "DRAG_FINISH";
    // Typing events
    static final String CTRL_ALT_TYPE = "CTRL_ALT_TYPE";
    static final String CTRL_SHIFT_TYPE = "CTRL_SHIFT_TYPE";
    static final String CTRL_META_TYPE = "CTRL_META_TYPE";
    static final String SHIFT_ALT_TYPE = "SHIFT_ALT_TYPE";
    static final String SHIFT_META_TYPE = "SHIFT_META_TYPE";
    static final String CTRL_TYPE = "CTRL_TYPE";
    static final String SHIFT_TYPE = "SHIFT_TYPE";
    static final String ALT_TYPE = "ALT_TYPE";
    static final String META_TYPE = "META_TYPE";
    static final String TYPE = "TYPE";
    //Variable Step
    static final String VAR_STEP = "VAR_STEP";
}
