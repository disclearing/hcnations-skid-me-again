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

package org.hcgames.hcfactions.manager;

import org.hcgames.hcfactions.faction.Faction;

/**
 * This is the result of calling a advancedSearch method from the FactionManager
 * Only one of the methods will be called when the method completes
 * However I suggest you input code into both and handle all possible outcomes
 *
 * @param <T>
 */

//TODO: A, boolean with isAsync & second, a way to forceAsync
//TODO: Add more fail reasons as there is more possibilities
public interface SearchCallback<T extends Faction> {

    void onSuccess(T t);

    void onFail(FailReason reason);

    enum FailReason{

        /**
         * When the search fails to find a faction
         */
        NOT_FOUND,

        /**
         * When the search succeeds however the faction found does not cast to the one provided.
         */
        CLASS_CAST,

        /**
         * When the cause of it failing is unknown
         */
        UNKNOWN,
    }

}
