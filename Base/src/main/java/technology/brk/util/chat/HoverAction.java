/*
 *   COPYRIGHT NOTICE
 *
 *   Copyright (C) 2016, SystemUpdate, <admin@systemupdate.io>.
 *
 *   All rights reserved.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF THIRD PARTY RIGHTS. IN
 *   NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *   DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 *   OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 *   OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *   Except as contained in this notice, the name of a copyright holder shall not
 *   be used in advertising or otherwise to promote the sale, use or other dealings
 *   in this Software without prior written authorization of the copyright holder.
 */

package technology.brk.util.chat;

import net.minecraft.server.v1_7_R4.EnumHoverAction;

@Deprecated
public enum HoverAction {
    SHOW_TEXT(EnumHoverAction.SHOW_TEXT),
    SHOW_ITEM(EnumHoverAction.SHOW_ITEM),
    SHOW_ACHIEVEMENT(EnumHoverAction.SHOW_ACHIEVEMENT);
    
    private final EnumHoverAction hoverAction;

    private HoverAction(EnumHoverAction hoverAction) {
        this.hoverAction = hoverAction;
    }

    public EnumHoverAction getNMS() {
        return this.hoverAction;
    }
}

