package com.doctordark.hcf.eventgame.crate;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class EventKeyInventoryHolder implements InventoryHolder {

    private Inventory inventory;

    private EventKeyInventoryHolder(int rows) {
        this.inventory = Bukkit.createInventory(this, rows * 9, "Event Prize");
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Creates an {@link Inventory} held by an instance of this class. The output inventory will
     * be either a single chest or a double chest depending on the requested size provided through the
     * method parameters.
     *
     * @param slots Requested size of inventory
     * @return Single or double chest inventory
     */
    public static Inventory createInventory(int slots) {
        int rows = slots > 36 ? 6 : 3;
        EventKeyInventoryHolder holder = new EventKeyInventoryHolder(rows);
        return holder.getInventory();
    }
}
