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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MapSorting {
    private static final Function EXTRACT_KEY = (Function<Map.Entry<Object, Object>, Object>) input -> input == null ? null : input.getKey();
    private static final Function EXTRACT_VALUE = (Function<Map.Entry<Object, Object>, Object>) input -> input == null ? null : input.getValue();

    public static <T, V extends Comparable<V>> List<Map.Entry<T, V>> sortedValues(Map<T, V> map) {
        return MapSorting.sortedValues(map, Ordering.natural());
    }

    public static <T, V> List sortedValues(final Map<T, V> map, final Comparator<V> valueComparator) {
        return Ordering.from(valueComparator).onResultOf(extractValue()).sortedCopy((Iterable) map.entrySet());
    }

    public static <T, V> Iterable<T> keys(List<Map.Entry<T, V>> entryList) {
        return Iterables.transform(entryList, MapSorting.extractKey());
    }

    public static <T, V> Iterable<V> values(List<Map.Entry<T, V>> entryList) {
        return Iterables.transform(entryList, MapSorting.extractValue());
    }

    private static <T, V> Function<Map.Entry<T, V>, T> extractKey() {
        return EXTRACT_KEY;
    }

    private static <T, V> Function<Map.Entry<T, V>, V> extractValue() {
        return EXTRACT_VALUE;
    }

}

