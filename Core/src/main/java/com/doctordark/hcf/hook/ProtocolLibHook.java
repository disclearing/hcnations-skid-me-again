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

package com.doctordark.hcf.hook;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.doctordark.hcf.HCF;
import com.doctordark.hcf.visualise.VisualBlock;
import com.doctordark.hcf.visualise.VisualBlockData;
import com.doctordark.util.NmsUtils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Reference http://wiki.vg/Protocol
 */
public final class ProtocolLibHook{

    private static final ImmutableSet<String> DISALLOWED_TAB_COMPLETES = ImmutableSet.of("ver", "version", "?", "about", "help"); //Empty string is required to check for "/"

    private static final int STARTED_DIGGING = 0;
    private static final int FINISHED_DIGGING = 2;

    private ProtocolLibHook(){
    }

    /**
     * Hooks ProtocolLibrary into a {@link JavaPlugin}.
     *
     * @param hcf the plugin to hook into
     */
    public static void hook(HCF hcf){
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(hcf, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_PLACE){
            @Override
            public void onPacketReceiving(PacketEvent event){
                StructureModifier<Integer> modifier = event.getPacket().getIntegers();
                Player player = event.getPlayer();

                try{
                    int face = modifier.read(3);
                    if(face == 255){
                        return;
                    }

                    Location clickedBlock = new Location(player.getWorld(), modifier.read(0), modifier.read(1), modifier.read(2));
                    if(hcf.getVisualiseHandler().getVisualBlockAt(player, clickedBlock) != null){
                        Location placedLocation = clickedBlock.clone();
                        switch(face){
                            case 2:
                                placedLocation.add(0, 0, -1);
                                break;
                            case 3:
                                placedLocation.add(0, 0, 1);
                                break;
                            case 4:
                                placedLocation.add(-1, 0, 0);
                                break;
                            case 5:
                                placedLocation.add(1, 0, 0);
                                break;
                            default:
                                return;
                        }

                        if(hcf.getVisualiseHandler().getVisualBlockAt(player, placedLocation) == null){
                            event.setCancelled(true);
                            player.sendBlockChange(placedLocation, Material.AIR, (byte) 0);
                            NmsUtils.resendHeldItemPacket(player);
                        }
                    }
                }catch(FieldAccessException ex){
                    ex.printStackTrace();
                }
            }
        });

        protocolManager.addPacketListener(new PacketAdapter(hcf, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG){
            @Override
            public void onPacketReceiving(PacketEvent event){
                StructureModifier<Integer> modifier = event.getPacket().getIntegers();

                try{
                    int status = modifier.read(4);
                    if(status == STARTED_DIGGING || status == FINISHED_DIGGING){
                        Player player = event.getPlayer();
                        int x = modifier.read(0), y = modifier.read(1), z = modifier.read(2);
                        Location location = new Location(player.getWorld(), x, y, z);
                        VisualBlock visualBlock = hcf.getVisualiseHandler().getVisualBlockAt(player, location);
                        if(visualBlock != null){
                            event.setCancelled(true);
                            VisualBlockData data = visualBlock.getBlockData();
                            if(status == FINISHED_DIGGING){
                                player.sendBlockChange(location, data.getBlockType(), data.getData());
                            }else if(status == STARTED_DIGGING){ // we check this because Blocks that broke pretty much straight away do not send a FINISHED for some weird reason.
                                EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                                if(player.getGameMode() == GameMode.CREATIVE || entityPlayer.world.getType(x, y, z).getDamage(entityPlayer, entityPlayer.world, x, y, z) >= 1.0F){
                                    player.sendBlockChange(location, data.getBlockType(), data.getData());
                                }
                            }
                        }
                    }
                }catch(FieldAccessException ex){
                    ex.printStackTrace();
                }
            }
        });

        protocolManager.addPacketListener(new PacketAdapter(hcf, PacketType.Play.Client.TAB_COMPLETE){
            @Override
            public void onPacketReceiving(PacketEvent event){
                if(event.getPlayer().hasPermission("hcf.bypassanticommandtab")){
                    return;
                }

                String message = event.getPacket().getSpecificModifier(String.class).read(0).toLowerCase();

                if(message.startsWith("/") && !message.contains(" ")){ //Blocks /<tab>
                    event.setCancelled(true);
                    return;
                }


                for(String i : DISALLOWED_TAB_COMPLETES){ //Blocks /<command> <tab>
                    if(message.startsWith("/" + i) && !message.contains("  ")){
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        });

    }

}
