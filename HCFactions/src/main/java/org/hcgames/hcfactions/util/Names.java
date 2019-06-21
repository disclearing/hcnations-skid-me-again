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

package org.hcgames.hcfactions.util;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.World;

public class Names {

    //TODO: Move into Base or make configurable
    private static final ImmutableMap<World.Environment, String> ENVIRONMENT_MAPPINGS = /*TODO:Maps.immutableEnumMap*/(ImmutableMap.of(
            World.Environment.NETHER, "Nether",
            World.Environment.NORMAL, "Overworld",
            World.Environment.THE_END, "The End"
    ));

    public static String getEnvironmentName(World.Environment environment){
        return ENVIRONMENT_MAPPINGS.containsKey(environment) ? ENVIRONMENT_MAPPINGS.get(environment) : StringUtils.capitalize(environment.toString());
    }
}
