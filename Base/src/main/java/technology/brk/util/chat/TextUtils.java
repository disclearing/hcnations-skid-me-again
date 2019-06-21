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

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

@Deprecated
public class TextUtils {
    public static Text join(Collection<Text> textCollection, String delimiter) {
        Text result = new Text();
        Text prefix = new Text();
        for (Text text : textCollection) {
            result.append(prefix).append(text);
            prefix = new Text(", ");
        }
        return result;
    }

    public static Text joinItemList(Collection<ItemStack> collection, String delimiter, boolean showQuantity) {
        Text text = new Text();
        for (ItemStack stack : collection) {
            if (stack == null) continue;
            text.append(new Text(delimiter));
            if (showQuantity) {
                text.append(new Text("[").setColor(ChatColor.YELLOW));
            }
            text.appendItem(stack);
            if (!showQuantity) continue;
            text.append(new Text(" x" + stack.getAmount()).setColor(ChatColor.YELLOW)).append(new Text("]").setColor(ChatColor.YELLOW));
        }
        return text;
    }
}

