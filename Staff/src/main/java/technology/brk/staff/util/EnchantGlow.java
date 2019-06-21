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

package technology.brk.staff.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class EnchantGlow extends EnchantmentWrapper{

    private static Enchantment glow;

    private EnchantGlow(int id){
        super(id);
    }

    @Override
    public boolean canEnchantItem(ItemStack item){
        return true;
    }

    @Override
    public boolean conflictsWith(Enchantment other){
        return false;
    }

    @Override
    public EnchantmentTarget getItemTarget(){
        return null;
    }

    @Override
    public int getMaxLevel(){
        return 10;
    }

    @Override
    public String getName(){
        return "Glow";
    }

    @Override
    public int getStartLevel(){
        return 1;
    }

    public static Enchantment getGlow(){
        if(glow != null){
            return glow;
        }

        try{
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null , true);
        }catch (Exception e){
            e.printStackTrace();
        }

        glow = new EnchantGlow(255);
        Enchantment.registerEnchantment(glow);

        return glow;
    }

    public static void addGlow(ItemStack item){
        item.addEnchantment(getGlow(), 1);
    }

    public static void removeGlow(ItemStack item){
        item.removeEnchantment(getGlow());
    }
}
