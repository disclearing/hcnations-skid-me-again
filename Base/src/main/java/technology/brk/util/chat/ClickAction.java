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

import net.minecraft.server.v1_7_R4.EnumClickAction;

@Deprecated
public enum ClickAction {
    OPEN_URL(EnumClickAction.OPEN_URL),
    OPEN_FILE(EnumClickAction.OPEN_FILE),
    RUN_COMMAND(EnumClickAction.RUN_COMMAND),
    SUGGEST_COMMAND(EnumClickAction.SUGGEST_COMMAND);
    
    private final EnumClickAction clickAction;

    private ClickAction(EnumClickAction action) {
        this.clickAction = action;
    }

    public EnumClickAction getNMS() {
        return this.clickAction;
    }
}

