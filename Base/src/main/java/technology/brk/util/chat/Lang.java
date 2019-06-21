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

package technology.brk.util.chat;

import net.minecraft.server.v1_7_R4.Item;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_7_R4.potion.CraftPotionEffectType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import technology.brk.base.GuavaCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class Lang {
    private static final Pattern PAT = Pattern.compile("^\\s*([\\w\\d\\.]+)\\s*=\\s*(.*)\\s*$");
    private static Map<String, String> translations;
    private static String language;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void initialize(String lang) throws IOException {
        translations = new HashMap<>();
        if (lang == null) {
            lang = "en_US";
        }
        if (!lang.equals(language)) {
            InputStream stream = null;
            BufferedReader reader = null;
            try {
                String line;
                language = lang;
                String resourcePath = "/assets/minecraft/lang/" + language + ".lang";
                stream = Item.class.getResourceAsStream(resourcePath);
                reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                while ((line = reader.readLine()) != null) {
                    Matcher matcher;
                    if (!(line = line.trim()).contains("=") || !(matcher = PAT.matcher(line)).matches()) continue;
                    translations.put(matcher.group(1), matcher.group(2));
                }
            }
            finally {
                if (stream != null) {
                    stream.close();
                }
                if (reader != null) {
                    reader.close();
                }
            }
        }
    }

    public static String getLanguage() {
        return language;
    }

    public static String translatableFromStack(ItemStack stack) {
        net.minecraft.server.v1_7_R4.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        Item item = nms.getItem();
        return item.a(nms) + ".name";
    }

    public static String fromStack(ItemStack stack) {
        String node = Lang.translatableFromStack(stack);
        return GuavaCompat.firstNonNull(translations.get(node), node);
    }

    public static String translatableFromEnchantment(Enchantment ench) {
        net.minecraft.server.v1_7_R4.Enchantment nms = net.minecraft.server.v1_7_R4.Enchantment.byId[ench.getId()];
        return nms == null ? ench.getName() : nms.a();
    }

    public static String fromEnchantment(Enchantment ench) {
        String node = Lang.translatableFromEnchantment(ench);
        return GuavaCompat.firstNonNull(translations.get(node), node);
    }

    public static String translatableFromPotionEffectType(PotionEffectType effectType) {
        CraftPotionEffectType craftType = (CraftPotionEffectType)PotionEffectType.getById(effectType.getId());
        return craftType.getHandle().a();
    }

    public static String fromPotionEffectType(PotionEffectType effectType) {
        String node = Lang.translatableFromPotionEffectType(effectType);
        String val = translations.get(node);
        if (val == null) {
            return node;
        }
        return val;
    }

    public static String translate(String key, Object ... args) {
        return String.format(translations.get(key), args);
    }

    static {
        language = null;
    }
}

