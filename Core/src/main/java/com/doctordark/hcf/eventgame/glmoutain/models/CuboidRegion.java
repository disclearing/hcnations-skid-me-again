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

package com.doctordark.hcf.eventgame.glmoutain.models;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class CuboidRegion{
    private final World world;
    private final Vector minimumPoint;
    private final Vector maximumPoint;

    private final Set<Block> blocks = new HashSet<Block>();

    public CuboidRegion(World world, Vector minimumPoint, Vector maximumPoint){
        this.world = world;
        this.minimumPoint = minimumPoint;
        this.maximumPoint = maximumPoint;

        for(int x = minimumPoint.getBlockX(); x <= maximumPoint.getBlockX(); x++){
            for(int y = minimumPoint.getBlockY(); y <= maximumPoint.getBlockY(); y++){
                for(int z = minimumPoint.getBlockZ(); z <= maximumPoint.getBlockZ(); z++){
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }
    }

    public Set<Block> getBlocks(){
        return blocks;
    }

    public boolean isInAABB(Location location){
        return location.getWorld() == world && location.toVector().isInAABB(minimumPoint, maximumPoint);
    }
}
