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

package com.doctordark.hcf.eventgame.sotw;

import com.doctordark.hcf.HCF;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import technology.brk.util.DurationFormatter;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SOTWManager{

    private final HCF plugin;

    private final ArrayList<String> runningEntries = new ArrayList<>();
    private final Object runningLock = new Object();

    private final ArrayList<String> pausedEntries = new ArrayList<>();
    private final Object pausedLock = new Object();

    private SOTWRunnable updater;

    @Getter private boolean paused;
    @Getter private long duration;
    @Getter private long endTime;

    public void setPaused(boolean pause){
        if(isRunning() && pause){
            if(updater != null){
                updater.cancel();
                updater = null;
            }

            duration = endTime - System.currentTimeMillis();
            updatePaused();
        }

        if(!isRunning() && !pause){
            if(updater == null){
                updater = new SOTWRunnable(plugin);
                updater.runTaskTimerAsynchronously(plugin, 20L, 2L);
                endTime = System.currentTimeMillis() + duration;
            }
        }

        this.paused = pause;
    }

    public boolean isRunning(){
        return updater != null;
    }

    public boolean isActive(){
        return paused || updater != null;
    }

    public void setDuration(long duration){
        this.duration = duration;

        if(!isActive()){
            setPaused(true);
            updatePaused();
            return;
        }

        if(isPaused()){
            updatePaused();
        }else{
            endTime = System.currentTimeMillis() + duration;
            updateRunning();
        }
    }

    public void end(boolean broadcast){
        if(!isActive()){
            return;
        }

        if(updater != null){
            updater.cancel();
            updater = null;
        }

        if(paused){
            paused = false;
            endTime = 0;
            duration = 0;

            synchronized(pausedLock){
                pausedEntries.clear();
            }
        }

        synchronized(runningLock){
            runningEntries.clear();
        }

        if(broadcast){
            plugin.getServer().broadcastMessage(plugin.getMessages().getString("Broadcast.SOTW-End"));
        }
    }

    void updateRunning(){
        synchronized(runningLock){
            runningEntries.clear();
            runningEntries.add(plugin.getMessages().getString("scoreboard.sotw.active").replace("{remaining}", DurationFormatter.getRemaining(endTime - System.currentTimeMillis(), true)));
        }
    }

    private void updatePaused(){
        synchronized(pausedLock){
            pausedEntries.clear();
            String formatted = DurationFormatter.getRemaining(duration, true);
            for(String line : plugin.getMessages().getString("scoreboard.sotw.paused").split("\n")){
                pausedEntries.add(line.replace("{time}", formatted));
            }
        }
    }

    public void provideScoreboard(List<String> lines){
        lines.addAll(isPaused() ? pausedEntries : runningEntries);
    }
}
