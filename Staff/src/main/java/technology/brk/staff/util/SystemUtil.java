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

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Deprecated //TODO: Move to Base
public class SystemUtil {

    //TODO: inventory#getcontents#>0
    public static int getInventoryContentsAmount(Inventory inventory){
        Validate.notNull(inventory, "Inventory cannot be null.");
        int amount = 0;

        for(ItemStack i : inventory.getContents()){
            if(i == null || i.getType().equals(Material.AIR)){
                continue;
            }

            amount++;
        }

        return amount;
    }

    public static Boolean parseState(String arg){
        switch(arg.toLowerCase()){
            case "on":
            case "enable:":
            case "true":
                return true;
            case "off":
            case "disable":
            case "false":
                return false;
        }

        return null;
    }
}
