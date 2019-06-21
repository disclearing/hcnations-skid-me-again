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

package technology.brk.util.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import technology.brk.util.collect.Reversed;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

//TODO: All the todos, builder class etc
public class Board {
    private final static int MAX_SIDEBAR_ENTRIES = 15;

    private final Map<Class<? extends Provider>, Provider> providers = new ConcurrentHashMap<>();
    private final WeakReference<Player> player;

    private final Scoreboard scoreboard;
    @Getter
    private final Objective objective;

    final Multimap<Integer, Entry> entryPriorityMap = Multimaps.newListMultimap(new TreeMap<>(Collections.reverseOrder()), Lists::newArrayList);
    private final List<String> currentLines = new ArrayList<>();

    private Entry header;
    private Entry footer;

    public Board(Plugin plugin, Player player, boolean hook, @Nullable String title, Provider... providers){
        this.player = new WeakReference<>(player);

        if(hook && !plugin.getServer().getScoreboardManager().getMainScoreboard().equals(player.getScoreboard())){
            scoreboard = player.getScoreboard();
        }else{
            scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }

        Objective localObjective;
        if((localObjective = scoreboard.getObjective("CarbonSB")) != null){
            localObjective.unregister();
        }

        localObjective = scoreboard.registerNewObjective("CarbonSB", "dummy");
        localObjective.setDisplayName((title == null || title.length() == 0) ? "CarbonSB" : title);
        localObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        objective = localObjective;
        for(Provider provider : providers){
            registerProvider(provider);
            provider.update();
        }
        build();
    }

    public void setHeader(String headerStr){
        if(header != null){
            header.setValue(headerStr);
        }else{
            header = new Entry(this, headerStr);
            build();
        }
    }

    public void setFooter(String footerStr){
        if(footer != null){
            footer.setValue(footerStr);
        }else{
            footer = new Entry(this, footerStr);
            build();
        }
    }

    public void removeHeader(){
        if(header != null){
            header = null;
            build();
        }
    }

    public void removeFooter(){
        if(footer != null){
            footer = null;
            build();
        }
    }

    public Provider getProvider(Class<? extends Provider> providerClass){
        return providers.get(providerClass);
    }

    /**
     * Registers a new provider, throws illegal argument exception if the provider is anonymous
     * @param provider to provide
     * @return false if already registered, true if registers
     */
    public boolean registerProvider(Provider provider){
        if(provider.getClass() == Provider.class){
            throw new IllegalArgumentException("Class cannot be anonymous");
        }

        if(providers.containsKey(provider.getClass())){
            return false;
        }

        providers.put(provider.getClass(), provider);
        provider.update();
        return true;
    }

    public Scoreboard getHandle(){
        return scoreboard;
    }

    public Player getPlayer(){
        if(player.isEnqueued()){
            throw new RuntimeException("Player is offline.");
        }

        return player.get();
    }

    synchronized void build(){
        //This method will only be called when adding NEW entries or removing current ones.

        /*
        --MY LONG TYPING LEL--

        Section below was taken from Hawk so lets be real..

        - update method will check if it exists, if it does then it will
          - Generate a new sidebar entry
            - If its a new entry, call the build method
            - If only the prefix / suffix changes, then update those
            - else generate the team, remove and then re-add the text/score
            (Maybe look into buffered objective for this bit)

        - Add a buffer objective
          - As seen at https://bukkit.org/threads/update-a-scoreboard-every-second-without-flashing.288265/
          - Cache: http://imgur.com/P4MZuX1, http://pastebin.com/YwKfH1X5


        Uh yes, team clean up.. from hawk:
        Team clean up also needs to be added, as a lot of teams will be un-used and just sit their
        taking up space and memory which can be dedicated else where.

        Also need to look into ensuring that duplicate scores can still be displayed, such as cutting the end
        string and adding a colour to ensure they are different. Something that is in consideration is adding an #isVisible
        method however we do have isApplicable but that doesn't allow the developer to hide it on demand unless they add
        that in manually.

        Possibly also add a way to tick providers (TickingProvider) so they can more easily and consistently update
        the scoreboard entry for stuff such as cool downs. Also need to add a way to track variables within the responses
        so that they can be forced into the prefix / suffix. Also need to make the update previous value more efficent, shouldn't
        create a new Value everytime it needs to be updated.
         */

        /*
        -- FROM HAWK --
         rawScoreboard.getEntries().forEach(rawScoreboard::resetScores);
        if(entries.isEmpty()){
            return;
        }

        Multimap<Integer, Entry> sortedMap = Multimaps.newListMultimap(new TreeMap<>(Collections.reverseOrder()), Lists::newArrayList);

        if(header != null){
            sortedMap.put(Integer.MAX_VALUE, header);
        }

        for(Entry entry : entries){
            sortedMap.put(entry.getPriority(), entry);
        }

        if(footer != null){
            sortedMap.put(Integer.MIN_VALUE, footer);
        }

        Team team;
        int currentScore = sortedMap.values().size();

        for(Entry entry : sortedMap.values()){
            team = rawScoreboard.getOrRegisterNewTeam(entry.getValue().getName());

            if(entry.getValue().hasPrefix()){
                team.setPrefix(entry.getValue().getPrefix());
            }

            if(entry.getValue().hasSuffix()){
                team.setPrefix(entry.getValue().getSuffix());
            }

            if(!team.getEntries().contains(entry.getValue().getName())){
                team.addEntry(entry.getValue().getName());
            }

            objective.getScore(entry.getValue().getName()).setScore(currentScore);
            entry.setScore(currentScore, false);

            currentScore--;
        }

        ----- BELOW IS TAKEN FROM IHCF/DD, THE CODE IS NOT IN USE ANYWHERE ----
        public void setAllLines(List<SidebarEntry> lines){
        synchronized (this.contents) {
            if (lines.size() != this.contents.size()) {
                this.contents.clear();
                if (lines.isEmpty()) {
                    this.requiresUpdate.set(true);
                    return;
                }
            }

            List<SidebarEntry> newLines = new ArrayList<>();

            if(spacerStart != null && !lines.isEmpty()) {
                newLines.add(spacerStart);
            }

            newLines.addAll(lines);

            if(spacerEnd != null && !lines.isEmpty()) {
                newLines.add(spacerEnd);
            }

            int size = Math.min(MAX_SIDEBAR_ENTRIES, newLines.size());
            int count = 0, lineNumber;

            for (SidebarEntry sidebarEntry : newLines) {
                lineNumber = size - count++;
                SidebarEntry value = this.contents.get(lineNumber);
                if (value == null || value != sidebarEntry) {
                    this.contents.put(lineNumber, sidebarEntry);
                    this.requiresUpdate.set(true);
                }
            }
        }

         public void flip() {
        if (this.requiresUpdate.getAndSet(false)) {
            Set<String> newLines = new HashSet<>(this.contents.size());
            this.contents.forEachEntry((i, sidebarEntry) -> {
                Team team = scoreboard.getOrRegisterNewTeam(sidebarEntry.name.length() > MAX_NAME_LENGTH ? sidebarEntry.name.substring(0, MAX_NAME_LENGTH) : sidebarEntry.name);

                if (sidebarEntry.prefix != null) {
                    team.setPrefix(sidebarEntry.prefix.length() > MAX_PREFIX_LENGTH ? sidebarEntry.prefix.substring(0, MAX_PREFIX_LENGTH) : sidebarEntry.prefix);
                }

                if (sidebarEntry.suffix != null) {
                    team.setSuffix(sidebarEntry.suffix.length() > MAX_SUFFIX_LENGTH ? sidebarEntry.suffix.substring(0, MAX_SUFFIX_LENGTH) : sidebarEntry.suffix);
                }

                newLines.add(sidebarEntry.name);
                if (!team.hasEntry(sidebarEntry.name)) {
                    team.addEntry(sidebarEntry.name);
                }

                current.getScore(sidebarEntry.name).setScore(i);
                return true;
            });

            // Reset the previous scores.
            this.previousLines.removeAll(newLines);
            Iterator<String> iterator = this.previousLines.iterator();
            while (iterator.hasNext()) {
                String last = iterator.next();
                Team team = this.scoreboard.getTeam(last);
                if (team != null) {
                    if(team.getEntries().contains(last)){
                        team.removeEntry(last);
                    }
                    this.scoreboard.resetScores(last);
                }

                if(iterator != null){
                    iterator.remove();
                }
            }

            this.previousLines = newLines; // flip around
            this.current.setDisplayName(this.title);
         */

        entryPriorityMap.clear();
        currentLines.clear();

        int checkSize = 0;
        if(header != null){
            entryPriorityMap.put(Integer.MAX_VALUE, header);
            checkSize++;
        }

        if(footer != null){
            entryPriorityMap.put(Integer.MIN_VALUE, footer);
            checkSize++;
        }

        for(Provider provider : providers.values()){
            //TODO: Do in a better way
            for(Entry entry : Reversed.reversed(provider.entries)){
                entryPriorityMap.put(provider.getPriority(), entry);
                if(checkSize >= MAX_SIDEBAR_ENTRIES){
                  break;
               }
            }
        }

        if(entryPriorityMap.size() > checkSize){
            int score = 1; //TODO: Fix why team is null, it should never be when it enters this stage
            for(Entry entry : entryPriorityMap.values()){
                /////////////
                Team team = scoreboard.getOrRegisterNewTeam(entry.getValue().getName());

                team.setPrefix(entry.getValue().hasPrefix() ? entry.getValue().getPrefix() : "");
                team.setSuffix(entry.getValue().hasSuffix() ? entry.getValue().getSuffix() : "");

                if(!team.getEntries().contains(entry.getValue().getName())){
                    team.addEntry(entry.getValue().getName());
                }
                //////////////

                objective.getScore(entry.getValue().getName()).setScore(score);
                entry.setCurrentScore(score);

                currentLines.add(entry.getValue().getName());
                score++;
            }
        }

        for(String entry : scoreboard.getEntries()){
            if(!currentLines.contains(entry)){
                Team team = scoreboard.getTeam(entry);

                if(team != null){
                    if(team.getEntries().contains(entry)){
                        team.removeEntry(entry);
                    }
                }

                scoreboard.resetScores(entry);
            }
        }
    }

}
