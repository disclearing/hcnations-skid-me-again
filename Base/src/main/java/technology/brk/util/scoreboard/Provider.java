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

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class Provider {

    @Getter
    private final Board board;//TODO: Ditch getting scoreboard from constructor

    //Force LinkedList
    ArrayList<Entry> entries = new ArrayList<>();

    public Provider(Board scoreboard){
        this.board = scoreboard;
    }

    public abstract ArrayList<String> provide(Player player);

    public abstract int getPriority();

    public void update(){//This will, update existing entries, remove/add to the arraylist but NOT to the scoreboard.
        if(!isApplicable(board.getPlayer())){
            remove();
            return;
        }

        ImmutableList<String> lines = ImmutableList.copyOf(provide(board.getPlayer()));
        boolean needsBuild = false;

        int current = 0;
        Entry lastEntry;

        for(String line : lines){
            if (!(current >= entries.size())){
                lastEntry = entries.get(current);
                if(!board.entryPriorityMap.containsValue(lastEntry)) {
                    needsBuild = true;
                }

                lastEntry.setValue(line);
                current++;
                continue;
            }

            needsBuild = true;
            entries.add(new Entry(board, line));
            current++;
        }

        //TODO Err: Account for line going down a level so ordering!
        //TODO Make more efficient via looping thru the entries, and then the left over lines

        Iterator<Entry> entryIterator = entries.iterator();
        while(entryIterator.hasNext()){
            Entry next = entryIterator.next();

            if(!lines.contains(next.getOriginalValue())){
                entryIterator.remove();
                needsBuild = true;
            }
        }

        if(needsBuild){
            board.build();
        }
    }

    /**
     * This method will ONLY clear the entries, it will not unregister this as a provider.
     */
    public void remove(){
        if(!entries.isEmpty()){
            entries.clear();
            board.build();
        }
    }

    public abstract boolean isApplicable(Player player);

}
