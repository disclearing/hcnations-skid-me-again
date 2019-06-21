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

package org.hcgames.hcfactions.structure;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import technology.brk.util.mongo.Mongoable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor @Getter
public class FactionRelation implements Mongoable, ConfigurationSerializable{

    private final Relation relation;
    private final UUID faction;

    @Getter(AccessLevel.NONE)
    private String factionCachedName;

    public FactionRelation(Map<String, Object> map){
        relation = Relation.valueOf((String) map.get("relation"));
        faction = UUID.fromString((String) map.get("faction"));
        factionCachedName = (String) map.get("factionCachedName");
    }

    public FactionRelation(Document document){
        relation = Relation.valueOf(document.getString("relation"));
        faction = UUID.fromString(document.getString("faction"));
        factionCachedName = document.getString("factionCachedName");
    }

    @Override
    public Document toDocument(){
        Document document = new Document();
        document.put("relation", relation.name());
        document.put("faction", faction.toString());
        document.put("factionCachedName", factionCachedName);
        return document;
    }

    @Override
    public Map<String, Object> serialize(){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("relation", relation.name());
        map.put("faction", faction.toString());
        map.put("factionCachedName", factionCachedName);
        return map;
    }
}
