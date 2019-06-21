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

package com.doctordark.hcf.invrestore;

import lombok.Getter;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import technology.brk.util.BukkitUtils;
import technology.brk.util.InventorySerialisation;
import technology.brk.util.mongo.Mongoable;

import java.io.IOException;
import java.util.UUID;

@Getter
public class InventoryState implements Mongoable{

    private final UUID owner;

    private final ItemStack[] contents;
    private final ItemStack[] armour;

    private final int level;
    private final float exp;

    InventoryState(Player player){
        owner = player.getUniqueId();

        contents = BukkitUtils.deepClone(player.getInventory().getContents());
        armour = BukkitUtils.deepClone(player.getInventory().getArmorContents());

        level = player.getLevel();
        exp = player.getExp();
    }

    InventoryState(Document document){
        owner = UUID.fromString(document.getString("_id"));

        try{
            contents = InventorySerialisation.itemStackArrayFromBase64(document.getString("contents"));
            armour = InventorySerialisation.itemStackArrayFromBase64(document.getString("armor"));
        }catch(IOException e){
            throw new RuntimeException(e);
        }

        level = document.getInteger("levels");
        exp = Float.valueOf(document.getString("exp"));
    }

    @Override
    public Document toDocument(){
        Document document = new Document();
        document.put("_id", owner.toString());

        document.put("contents", InventorySerialisation.itemStackArrayToBase64(contents));
        document.put("armor", InventorySerialisation.itemStackArrayToBase64(armour));

        document.put("levels", level);
        document.put("exp", Float.toString(exp));
        return document;
    }

    void restore(Player player){
        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(armour);
        player.setLevel(level);
        player.setExp(exp);
    }
}
