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

package org.hcgames.hcfactions.structure;


import lombok.NonNull;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.faction.Faction;
import technology.brk.base.GuavaCompat;
import technology.brk.util.mongo.Mongoable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores data about members in a {@link Faction}.
 */
public class FactionMember implements ConfigurationSerializable, Mongoable {

    private final UUID uniqueID;
    private String cachedName;

    private ChatChannel chatChannel;
    private Role role;

    public FactionMember(Player player, ChatChannel chatChannel, Role role) {
        this.uniqueID = player.getUniqueId();
        this.cachedName = player.getName();
        this.chatChannel = chatChannel;
        this.role = role;
    }

    /**
     * Constructs a new {@link FactionMember} from a map.
     *
     * @param map the map to construct from
     */
    public FactionMember(Map<String, Object> map) {
        this.uniqueID = UUID.fromString((String) map.get("uniqueID"));
        this.chatChannel = GuavaCompat.getIfPresent(ChatChannel.class, (String) map.get("chatChannel")).orElse(ChatChannel.PUBLIC);
        this.role = GuavaCompat.getIfPresent(Role.class, (String) map.get("role")).orElse(Role.MEMBER);

        if(map.containsKey("cachedName")){
            cachedName = (String) map.get("cachedName");
        }
    }

    public FactionMember(Document document){
        this.uniqueID = UUID.fromString(document.getString("uniqueID"));
        this.chatChannel = GuavaCompat.getIfPresent(ChatChannel.class, document.getString("chatChannel")).orElse(ChatChannel.PUBLIC);
        this.role = GuavaCompat.getIfPresent(Role.class, document.getString("role")).orElse(Role.MEMBER);

        if(document.containsKey("cachedName")){
            cachedName = document.getString("cachedName");
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uniqueID", uniqueID.toString());
        map.put("chatChannel", chatChannel.name());
        map.put("role", role.name());
        map.put("cachedName", cachedName);
        return map;
    }

    @Override
    public Document toDocument(){
        Document document = new Document();
        document.put("uniqueID", uniqueID.toString());
        document.put("chatChannel", chatChannel.name());
        document.put("role", role.name());
        document.put("cachedName", cachedName);
        return document;
    }

    /**
     * Gets the cached name of this {@link FactionMember}.
     * This name is updated whenever they person joins however it can be wrong if the user changes there name and it is not updated.
     *
     * @return the cached name of this {@link FactionMember}
     */
    public String getCachedName() {
        if(cachedName == null){ //TODO: Remove for next map
            cachedName = Bukkit.getOfflinePlayer(uniqueID).getName();
        }
        return cachedName;
    }

    /**
     * Gets the {@link UUID} of this {@link FactionMember}.
     *
     * @return the {@link UUID}
     */
    public UUID getUniqueId() {
        return uniqueID;
    }

    /**
     * Gets the {@link ChatChannel} of this {@link FactionMember}.
     *
     * @return the {@link ChatChannel}
     */
    public ChatChannel getChatChannel() {
        return chatChannel;
    }

    /**
     * Sets the {@link ChatChannel} of this {@link FactionMember}.
     *
     * @param chatChannel the {@link ChatChannel} to set
     */
    public void setChatChannel(@NonNull ChatChannel chatChannel){
        this.chatChannel = chatChannel;
    }

    /**
     * Gets the {@link Role} of this {@link FactionMember}.
     *
     * @return the {@link Role}
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the {@link Role} of this {@link FactionMember}.
     *
     * @param role the {@link Role} to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Converts this {@link Player} to a {@link Player}.
     *
     * @return an optional instance containing a {@link Player}
     */
    public Optional<Player> toOnlinePlayer() {
        return Optional.ofNullable(Bukkit.getServer().getPlayer(uniqueID));
    }

    public void setCachedName(@NonNull String name){
        this.cachedName = name;
    }
}
