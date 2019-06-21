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

package org.hcgames.hcfactions.faction.system;

import org.bson.Document;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.faction.Faction;

import java.util.Map;
import java.util.UUID;

public class WarzoneFaction extends Faction implements SystemFaction {

    private final static UUID FACTION_UUID = UUID.fromString("f067e071-86d0-41c7-8c4b-f1a1cf15867e");

    public WarzoneFaction() {
        super("Warzone", FACTION_UUID);
    }

    public WarzoneFaction(Map<String, Object> map) {
        super(map);
    }

    public WarzoneFaction(Document document){
        super(document);
    }

    @Override
    public String getFormattedName(CommandSender sender) {
        return JavaPlugin.getPlugin(HCFactions.class).getConfiguration().getRelationColourWarzone() + getName();
    }

    public static UUID getUUID() {
        return FACTION_UUID;
    }
}
