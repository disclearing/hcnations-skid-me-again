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

package technology.brk.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GenericUtils {
    private GenericUtils() {
    }

    public static <E> List<E> createList(Object object, Class<E> type) {
        ArrayList<E> output = new ArrayList<>();
        if (object != null && object instanceof List) {
            List input = (List)object;
            for (Object value : input) {
                if (value == null || value.getClass() == null) continue;
                if (type.isAssignableFrom(value.getClass())) {
                    E e = type.cast(value);
                    output.add(e);
                    continue;
                }
                String simpleName = type.getSimpleName();
                throw new AssertionError("Cannot cast to list! Key " + value + " is not a " + simpleName);
            }
        }
        return output;
    }

    public static <E> Set<E> castSet(Object object, Class<E> type) {
        HashSet<E> output = new HashSet<>();
        if (object != null && object instanceof List) {
            List input = (List)object;
            for (Object value : input) {
                if (value == null || value.getClass() == null) continue;
                if (type.isAssignableFrom(value.getClass())) {
                    E e = type.cast(value);
                    output.add(e);
                    continue;
                }
                String simpleName = type.getSimpleName();
                throw new AssertionError("Cannot cast to list! Key " + value + " is not a " + simpleName);
            }
        }
        return output;
    }

    public static <K, V> Map<K, V> castMap(Object object, Class<K> keyClass, Class<V> valueClass) {
        HashMap<K, V> output = new HashMap<>();
        if (object != null && object instanceof Map) {
            Map input = (Map)object;
            String keyClassName = keyClass.getSimpleName();
            String valueClassName = valueClass.getSimpleName();
            for (Object key : input.keySet().toArray()) {
                if (key != null && !keyClass.isAssignableFrom(key.getClass())) {
                    throw new AssertionError("Cannot cast to HashMap: " + keyClassName + ", " + keyClassName + ". Value " + valueClassName + " is not a " + keyClassName);
                }
                Object value = input.get(key);
                if (value != null && !valueClass.isAssignableFrom(value.getClass())) {
                    throw new AssertionError("Cannot cast to HashMap: " + valueClassName + ", " + valueClassName + ". Key " + key + " is not a " + valueClassName);
                }
                output.put(keyClass.cast(key), valueClass.cast(value));
            }
        }
        return output;
    }
}

