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

import net.minecraft.server.v1_7_R4.ChatClickable;
import net.minecraft.server.v1_7_R4.ChatComponentText;
import net.minecraft.server.v1_7_R4.ChatHoverable;
import net.minecraft.server.v1_7_R4.EnumChatFormat;
import net.minecraft.server.v1_7_R4.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class Text
extends ChatComponentText {
    public Text() {
        super("");
    }

    public Text(String string) {
        super(string);
    }

    public Text(Object object) {
        super(String.valueOf(object));
    }

    public Text append(Object object) {
        return this.append(String.valueOf(object));
    }

    public Text append(String text) {
        return (Text)this.a(text);
    }

    public Text append(IChatBaseComponent node) {
        return (Text)this.addSibling(node);
    }

    public  Text append(IChatBaseComponent ... nodes) {
        for (IChatBaseComponent node : nodes) {
            this.addSibling(node);
        }
        return this;
    }

    public static Trans fromItemStack(ItemStack stack) {
        return ChatUtil.fromItemStack(stack);
    }

    public Text localText(ItemStack stack) {
        return this.append(ChatUtil.localFromItem(stack));
    }

    public Text appendItem(ItemStack stack) {
        return this.append(ChatUtil.fromItemStack(stack));
    }

    public Text setBold(boolean bold) {
        this.getChatModifier().setBold(bold);
        return this;
    }

    public Text setItalic(boolean italic) {
        this.getChatModifier().setItalic(italic);
        return this;
    }

    public Text setUnderline(boolean underline) {
        this.getChatModifier().setUnderline(underline);
        return this;
    }

    public Text setRandom(boolean random) {
        this.getChatModifier().setRandom(random);
        return this;
    }

    public Text setStrikethrough(boolean strikethrough) {
        this.getChatModifier().setStrikethrough(strikethrough);
        return this;
    }

    public Text setColor(ChatColor color) {
        this.getChatModifier().setColor(EnumChatFormat.valueOf(color.name()));
        return this;
    }

    public Text setClick(ClickAction action, String value) {
        this.getChatModifier().setChatClickable(new ChatClickable(action.getNMS(), value));
        return this;
    }

    public Text setHover(HoverAction action, IChatBaseComponent value) {
        this.getChatModifier().a(new ChatHoverable(action.getNMS(), value));
        return this;
    }

    public Text setHoverText(String text) {
        return this.setHover(HoverAction.SHOW_TEXT, new Text(text));
    }

    public Text reset() {
        ChatUtil.reset(this);
        return this;
    }

    public IChatBaseComponent f() {
        return this.h();
    }

    public String toRawText() {
        return this.c();
    }

    public void send(CommandSender sender) {
        ChatUtil.send(sender, this);
    }

    public void broadcast() {
        this.broadcast(null);
    }

    public void broadcast(String permission) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (permission != null && !player.hasPermission(permission)) continue;
            this.send(player);
        }
        this.send(Bukkit.getConsoleSender());
    }
}

