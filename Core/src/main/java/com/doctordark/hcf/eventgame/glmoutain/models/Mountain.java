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

import org.bukkit.Material;

public class Mountain{
    private final CuboidRegion region;
    private final Material material;

    private int remainingBlocks;

    public Mountain(CuboidRegion region, Material material){
        this.region = region;
        this.material = material;
    }

    public void reset(){
        region.getBlocks().forEach(block -> block.setType(material));
        remainingBlocks = region.getBlocks().size();
    }

    public CuboidRegion getRegion(){
        return region;
    }

    public int getRemainingBlocks(){
        return remainingBlocks;
    }

    public void handleBlockBreak(){
        remainingBlocks--;
    }
}
