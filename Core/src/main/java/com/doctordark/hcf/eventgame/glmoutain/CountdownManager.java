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

package com.doctordark.hcf.eventgame.glmoutain;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

class CountdownManager extends BukkitRunnable{
    private final MountainHandler mountainHandler;
    private final Map<Integer, String> broadcasts;

    private final int resetInterval;
    private int remainingSeconds;

    CountdownManager(MountainHandler mountainHandler, int resetInterval, Map<Integer, String> broadcasts){
        this.broadcasts = broadcasts;
        this.mountainHandler = mountainHandler;
        this.resetInterval = resetInterval;
        remainingSeconds = resetInterval;
    }

    @Override
    public void run(){
        String broadcast = broadcasts.get(remainingSeconds);

        if(broadcast != null){
            Bukkit.broadcastMessage(broadcast);
        }

        if(remainingSeconds == 0){
            mountainHandler.resetAllMountains();
            remainingSeconds = resetInterval;
            return;
        }

        remainingSeconds--;
    }
}
