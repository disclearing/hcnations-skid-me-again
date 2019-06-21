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

package org.hcgames.kmextra.util;

import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.hcgames.kmextra.KMExtra;

import java.util.Set;

@RequiredArgsConstructor
public class CommandWrapper implements CommandSender{

    private final KMExtra plugin;

    @Override
    public void sendMessage(String s){}

    @Override
    public void sendMessage(String[] strings) {

    }

    @Override
    public Server getServer() {
        return plugin.getServer();
    }

    @Override
    public String getName() {
        return plugin.getDescription().getName();
    }

    @Override
    public boolean isPermissionSet(String s){
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission permission){
        return true;
    }

    @Override
    public boolean hasPermission(String s){
        return true;
    }

    @Override
    public boolean hasPermission(Permission permission){
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b){
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin){
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i){
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i){
        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment permissionAttachment){}

    @Override
    public void recalculatePermissions(){}

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return null;
    }

    @Override
    public boolean isOp(){
        return true;
    }

    @Override
    public void setOp(boolean b){}
}
