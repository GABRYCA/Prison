/*
 *  Prison is a Minecraft plugin for the prison game mode.
 *  Copyright (C) 2017 The Prison Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tech.mcprison.prison.spigot.compat;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Spigot 1.9, 1.10, and 1.11.
 *
 * @author Faizaan A. Datoo
 */
public class Spigot19 
	extends Spigot19GUI
	implements Compatibility {

    @Override 
    public EquipmentSlot getHand(PlayerInteractEvent e) {
        if (e.getHand() == null) {
            return null;
        } else {
            return EquipmentSlot.valueOf(e.getHand().name());
        }
    }

    @Override 
    public ItemStack getItemInMainHand(PlayerInteractEvent e) {
        return e.getPlayer().getInventory().getItemInMainHand();
    }

    @Override 
    public ItemStack getItemInMainHand(Player player) {
    	return player.getInventory().getItemInMainHand();
    }
    
    @Override 
    public void playIronDoorSound(Location loc) {
        loc.getWorld().playEffect(loc, Effect.IRON_DOOR_TOGGLE, null);
    }
    

	@Override
	public void breakItemInMainHand( Player player ) {
		player.getInventory().setItemInMainHand( null );
		
		player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 0.85F); 
	}

}
