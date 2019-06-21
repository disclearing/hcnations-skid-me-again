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

package technology.brk.util;

import com.google.common.base.Preconditions;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import technology.brk.util.mongo.Mongoable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PersistableLocation implements ConfigurationSerializable, Cloneable, Mongoable{
    private Location location;
    private World world;
    private String worldName;
    private UUID worldUID;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public PersistableLocation(Location location) {
        Preconditions.checkNotNull(location, "Location cannot be null");
        Preconditions.checkNotNull(location.getWorld(), "Locations' world cannot be null");
        this.world = location.getWorld();
        this.worldName = this.world.getName();
        this.worldUID = this.world.getUID();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public PersistableLocation(World world, double x, double y, double z) {
        this.worldName = world.getName();
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0.0f;
        this.pitch = 0.0f;
    }

    public PersistableLocation(String worldName, double x, double y, double z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0.0f;
        this.pitch = 0.0f;
    }

    public PersistableLocation(Map<String, Object> map) {
        this.worldName = (String)map.get("worldName");
        this.worldUID = UUID.fromString((String)map.get("worldUID"));
        Object o = map.get("x");
        this.x = o instanceof String ? Double.parseDouble((String)o) : (Double)o;
        o = map.get("y");
        this.y = o instanceof String ? Double.parseDouble((String)o) : (Double)o;
        o = map.get("z");
        this.z = o instanceof String ? Double.parseDouble((String)o) : (Double)o;
        this.yaw = Float.parseFloat((String)map.get("yaw"));
        this.pitch = Float.parseFloat((String)map.get("pitch"));
    }

    public PersistableLocation(Document document){
        worldName = document.getString("worldName");
        worldUID = UUID.fromString(document.getString("worldUID"));
        x = document.getDouble("x");
        y = document.getDouble("y");
        z = document.getDouble("z");
        yaw = Float.valueOf(document.getString("yaw"));
        pitch = Float.valueOf(document.getString("pitch"));
    }

    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("worldName", this.worldName);
        map.put("worldUID", this.worldUID.toString());
        map.put("x", this.x);
        map.put("y", this.y);
        map.put("z", this.z);
        map.put("yaw", Float.toString(this.yaw));
        map.put("pitch", Float.toString(this.pitch));
        return map;
    }

    @Override
    public Document toDocument(){
        Document document = new Document();
        document.put("worldName", this.worldName);
        document.put("worldUID", this.worldUID.toString());
        document.put("x", this.x);
        document.put("y", this.y);
        document.put("z", this.z);
        document.put("yaw", Float.toString(yaw));
        document.put("pitch", Float.toString(pitch));
        return document;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public UUID getWorldUID() {
        return this.worldUID;
    }

    public World getWorld() {
        Preconditions.checkNotNull((Object)this.worldUID, "World UUID cannot be null");
        Preconditions.checkNotNull((Object)this.worldName, "World name cannot be null");
        if (this.world == null) {
            this.world = Bukkit.getWorld(this.worldUID);
        }
        return this.world;
    }

    public void setWorld(World world) {
        this.worldName = world.getName();
        this.worldUID = world.getUID();
        this.world = world;
    }

    private void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    private void setWorldUID(UUID worldUID) {
        this.worldUID = worldUID;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public Location getLocation() {
        if (this.location == null) {
            this.location = new Location(this.getWorld(), this.x, this.y, this.z, this.yaw, this.pitch);
        }
        return this.location;
    }

    public PersistableLocation clone() throws CloneNotSupportedException {
        try {
            return (PersistableLocation)super.clone();
        }
        catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }
    }

    public String toString() {
        return "PersistableLocation [worldName=" + this.worldName + ", worldUID=" + this.worldUID + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", yaw=" + this.yaw + ", pitch=" + this.pitch + ']';
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersistableLocation)) {
            return false;
        }
        PersistableLocation that = (PersistableLocation)o;
        if (Double.compare(that.x, this.x) != 0) {
            return false;
        }
        if (Double.compare(that.y, this.y) != 0) {
            return false;
        }
        if (Double.compare(that.z, this.z) != 0) {
            return false;
        }
        if (Float.compare(that.yaw, this.yaw) != 0) {
            return false;
        }
        if (Float.compare(that.pitch, this.pitch) != 0) {
            return false;
        }
        if (this.world != null ? !this.world.equals(that.world) : that.world != null) {
            return false;
        }
        if (this.worldName != null ? !this.worldName.equals(that.worldName) : that.worldName != null) {
            return false;
        }
        if (this.worldUID == null) {
            return that.worldUID == null;
        }
        return this.worldUID.equals(that.worldUID);
    }

    public int hashCode() {
        int result = this.world != null ? this.world.hashCode() : 0;
        result = 31 * result + (this.worldName != null ? this.worldName.hashCode() : 0);
        result = 31 * result + (this.worldUID != null ? this.worldUID.hashCode() : 0);
        long temp = Double.doubleToLongBits(this.x);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.y);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.z);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        result = 31 * result + (this.yaw != 0.0f ? Float.floatToIntBits(this.yaw) : 0);
        result = 31 * result + (this.pitch != 0.0f ? Float.floatToIntBits(this.pitch) : 0);
        return result;
    }
}

