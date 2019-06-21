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

import net.minecraft.server.v1_7_R4.ChatComponentText;
import net.minecraft.server.v1_7_R4.ChatHoverable;
import net.minecraft.server.v1_7_R4.ChatModifier;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.EnumChatFormat;
import net.minecraft.server.v1_7_R4.IChatBaseComponent;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.PacketPlayOutChat;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

@Deprecated
public class ChatUtil {
    public static String getName(net.minecraft.server.v1_7_R4.ItemStack stack) {
        NBTTagCompound nbttagcompound;
        if (stack.tag != null && stack.tag.hasKeyOfType("display", 10) && (nbttagcompound = stack.tag.getCompound("display")).hasKeyOfType("Name", 8)) {
            return nbttagcompound.getString("Name");
        }
        return stack.getItem().a(stack) + ".name";
    }

    public static Trans localFromItem(ItemStack stack) {
        PotionType type;
        Potion potion;
        if (stack.getType() == Material.POTION && stack.getData().getData() == 0 && (potion = Potion.fromItemStack(stack)) != null && (type = potion.getType()) != null && type != PotionType.WATER) {
            String effectName = (potion.isSplash() ? "Splash " : "") + WordUtils.capitalizeFully(type.name().replace('_', ' ')) + " L" + potion.getLevel();
            return ChatUtil.fromItemStack(stack).append(" of " + effectName);
        }
        return ChatUtil.fromItemStack(stack);
    }

    public static Trans fromItemStack(ItemStack stack) {
        net.minecraft.server.v1_7_R4.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        NBTTagCompound tag = new NBTTagCompound();
        nms.save(tag);
        return new Trans(ChatUtil.getName(nms), new Object[0]).setColor(ChatColor.getByChar(nms.w().e.getChar())).setHover(HoverAction.SHOW_ITEM, new ChatComponentText(tag.toString()));
    }

    public static void reset(IChatBaseComponent text) {
        ChatModifier modifier = text.getChatModifier();
        modifier.a((ChatHoverable)null);
        modifier.setChatClickable(null);
        modifier.setBold(Boolean.FALSE);
        modifier.setColor(EnumChatFormat.RESET);
        modifier.setItalic(Boolean.FALSE);
        modifier.setRandom(Boolean.FALSE);
        modifier.setStrikethrough(Boolean.FALSE);
        modifier.setUnderline(Boolean.FALSE);
    }

    public static void send(CommandSender sender, IChatBaseComponent text) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            PacketPlayOutChat packet = new PacketPlayOutChat(text, true);
            EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
            entityPlayer.playerConnection.sendPacket(packet);
        } else {
            sender.sendMessage(text.c());
        }
    }
}

