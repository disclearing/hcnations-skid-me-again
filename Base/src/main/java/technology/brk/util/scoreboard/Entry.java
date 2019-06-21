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

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

class Entry {

    @Getter(AccessLevel.PACKAGE)
    private Value value;

    @Getter(AccessLevel.PACKAGE)
    private String originalValue;

    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private int currentScore = Integer.MIN_VALUE;

    private Team currentTeam;
    private Board board;

    Entry(Board scoreboard, String value){
        this.board = scoreboard;
        originalValue = value;
        this.value = Value.split(value);
    }

    void setValue(String strValue){
        if(currentScore == Integer.MIN_VALUE){
            if(value == null){
                originalValue = strValue;
                value = Value.split(strValue);
                board.build();
            }
            return;
        }

        if(originalValue.equals(strValue)){
            return; //No need to do those checks / update if it's the same value
        }

        Value newValue = Value.split(strValue);
        originalValue = strValue;

        if(newValue.getName().equals(value.getName()) && currentTeam != null){
            if(value.hasPrefix() && !newValue.hasPrefix()){
                currentTeam.setPrefix("");
            }else if(newValue.hasPrefix()){
                currentTeam.setPrefix(newValue.getPrefix());
            }

            if(value.hasSuffix() && !newValue.hasSuffix()){
                currentTeam.setSuffix("");
            }else if(newValue.hasSuffix()){
                currentTeam.setSuffix(newValue.getSuffix());
            }

            value = newValue;
            return;
        }

        //Shit.. Name changed! Time to recreate and re-add/remove
        Team newTeam = board.getHandle().getOrRegisterNewTeam(newValue.getName());

        if(value.hasPrefix() && !newValue.hasPrefix()){
            newTeam.setPrefix("");
        }else if(newValue.hasPrefix()){
            newTeam.setPrefix(newValue.getPrefix());
        }

        if(value.hasSuffix() && !newValue.hasSuffix()){
            newTeam.setSuffix("");
        }else if(newValue.hasSuffix()){
            newTeam.setSuffix(newValue.getSuffix());
        }

        if(!newTeam.getEntries().contains(newValue.getName())){
            newTeam.addEntry(newValue.getName());
        }

        board.getHandle().resetScores(value.getName());
        board.getObjective().getScore(newValue.getName()).setScore(currentScore);
        //TODO: Possibly team cleanup here? O.o
        currentTeam = newTeam;
        value = newValue;
    }

    @Override
    public boolean equals(Object object){
        if(!(object instanceof Entry)){
            return false;
        }

        Entry other = (Entry) object;

        if(other.getValue() == null){
            return value == null;

        }

        return other.getValue().getName().equals(value.getName());

    }

    @Override
    public String toString(){
        return value == null ? "NoValue" : value.toString();
    }

    //Class cannot be private
    @Data
    @EqualsAndHashCode
    static class Value{

        private String name;
        private String prefix = "";
        private String suffix = "";

        Value(String name){
            Objects.requireNonNull(name, "Name cannot be null.");
            this.name = name;
        }

        Value(String prefix, String name){
            this(name);

            Objects.requireNonNull(prefix, "Prefix cannot be null.");
            this.prefix = prefix;
        }

        Value(String prefix, String name, String suffix){
            this(prefix, name);

            Objects.requireNonNull(suffix, "Suffix cannot be null.");
            this.suffix = suffix;
        }

        //TODO: If equal to or less then 32, only split into 2 segments and use unique key for name like in Glaedr
        static Value split(@NonNull String display){
            if(display.length() < 17){
                return new Value(display);
            }

            if(display.length() > 48){
                throw new IllegalArgumentException("Display string too long! Must be less than "+16*3+" chars!");
            }

            String[] result = new String[3];
            int resultIndex = 0;
            StringBuilder currentBuilder = new StringBuilder();
            for(char c : display.toCharArray()) {
                if (currentBuilder.length() > ((c == ChatColor.COLOR_CHAR) ? 14 : 15)) { //Chat colors must be kept together.
                    result[resultIndex++] = currentBuilder.toString();
                    currentBuilder = new StringBuilder();
                }
                currentBuilder.append(c);
            }

            result[resultIndex] = currentBuilder.toString();
            if(result[2] == null) result[2] = "";//TODO: Implement, push as much as possible into the suffix from the name. If name length == suffix length && suffix is empty then put suffix as name and name as unique key
            return new Value(result[0], result[1], result[2]);
        }

        boolean hasPrefix(){
            return prefix != null && prefix.length() > 0;
        }

        boolean hasSuffix(){
            return suffix != null && suffix.length() > 0;
        }

        @Override
        public String toString(){
            return "{prefix=" + prefix + ", name=" + name + ", suffix=" + suffix + "}";
        }
    }
}
