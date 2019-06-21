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

package com.doctordark.hcf.scoreboard.api;

import lombok.Getter;
import org.bukkit.ChatColor;

@Getter
class SidebarEntry{

    private final String prefix;
    private final String name;
    private final String suffix;

    SidebarEntry(String value){
        if(value.length() < 17){
            prefix = value;
            name = null;
            suffix = null;
            return;
        }

        StringBuilder currentBuilder = new StringBuilder();
        String[] result = new String[3];
        int resultIndex = 0;

        for(char c : value.toCharArray()) {
            if(currentBuilder.length() > ((c == ChatColor.COLOR_CHAR) ? 14 : 15)){ //Chat colors must be kept together.
                result[resultIndex++] = currentBuilder.toString();
                currentBuilder = new StringBuilder();
            }

            currentBuilder.append(c);
        }

        result[resultIndex] = currentBuilder.toString();
        prefix = result[0];

        if(result[2] == null){
            name = null;
            suffix = result[1];
        }else{
            name = result[1];
            suffix = result[2];
        }
    }

}
