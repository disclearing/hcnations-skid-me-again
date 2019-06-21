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

package technology.brk.staff.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import technology.brk.staff.Staff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaffMember {

    @Getter
    private final Map<Long, String> commandsLog = new HashMap<>();

    @Getter private final UUID userUUID;

    private File userFolder;

    private File userSettingsFile;
    private File commandsLogFile;

    private Staff plugin;

    StaffMember(Player player, Staff plugin){
        this.plugin = plugin;
        userUUID = player.getUniqueId();
        //load settings from file
    }

    private void createFolder(){
        if(userFolder == null){
            userFolder = new File(plugin.getDataFolder(), "players" + File.separator + userUUID.toString());
        }

        if(userFolder.exists()){
            return;
        }

        userFolder.mkdirs();
    }

    private void createFiles(boolean settings, boolean log){
        if(settings){
            if(userSettingsFile == null){
                userSettingsFile = new File(plugin.getDataFolder(), "players" + File.separator + userUUID.toString() + File.separator + "settings.yml");
            }

            if(userSettingsFile.exists()){
                return;
            }

            try{
                plugin.getLogger().info("Creating new file.. " + userSettingsFile.getAbsolutePath());
                userSettingsFile.createNewFile();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        if(log){
            if(commandsLogFile == null){
                commandsLogFile = new File(plugin.getDataFolder(), "players" + File.separator + userUUID.toString() + File.separator + "command.log");
            }

            if(commandsLogFile.exists()){
                return;
            }

            try{
                plugin.getLogger().info("Creating new file.. " + commandsLogFile.getAbsolutePath());
                commandsLogFile.createNewFile();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    public void logCommand(String command){
        commandsLog.put(System.currentTimeMillis(), command);
    }

    public void close(boolean async){
        if(async){
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::writeCommandsToFile);
            return;
        }

        writeCommandsToFile();
    }

    private void writeCommandsToFile(){
        if(!(commandsLog.isEmpty())){
            createFolder();
            createFiles(false, true);

            PrintWriter writer = null;

            try{
                writer = new PrintWriter(new FileWriter(commandsLogFile, true));
            } catch (IOException e){
                e.printStackTrace();
            }

            if(writer == null){
                return;
            }

            for(long i : commandsLog.keySet()){
                writer.println("[" + DateFormat.getDateTimeInstance().format(new Date(i)) + "] " + commandsLog.get(i));
            }

            writer.close();
            commandsLog.clear();
        }
    }
}
