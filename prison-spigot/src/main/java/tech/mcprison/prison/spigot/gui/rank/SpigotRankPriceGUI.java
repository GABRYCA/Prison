package tech.mcprison.prison.spigot.gui.rank;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import tech.mcprison.prison.spigot.SpigotPrison;
import tech.mcprison.prison.spigot.gui.ListenersPrisonManager;
import tech.mcprison.prison.spigot.gui.SpigotGUIComponents;

import java.util.List;

/**
 * @author GABRYCA
 */
public class SpigotRankPriceGUI extends SpigotGUIComponents {

    private final Player p;
    private final String rankName;
    private final Integer val;
    private final Configuration messages = messages();

    public SpigotRankPriceGUI(Player p, Integer val, String rankname){
        this.p = p;
        this.val = val;
        this.rankName = rankname;
    }

    public void open() {

        // Check if Ranks are enabled
        if (!(checkRanks(p))){
            return;
        }

        // Create a new inventory
        int dimension = 45;
        Inventory inv = Bukkit.createInventory(null, dimension, SpigotPrison.format("&3RankManager -> RankPrice"));

        if (guiBuilder(inv)) return;

        // Open the inventory
        openGUI(p, inv);
    }

    private boolean guiBuilder(Inventory inv) {
        try {
            buttonsSetup(inv);
        } catch (NullPointerException ex){
            p.sendMessage(SpigotPrison.format("&cThere's a null value in the GuiConfig.yml [broken]"));
            ex.printStackTrace();
            return true;
        }
        return false;
    }

    private void buttonsSetup(Inventory inv) {


        // Create a new lore
        List<String> changeDecreaseValueLore = createLore(
                messages.getString("Lore.ClickToDecrease")
        );
        List<String> confirmButtonLore;
        confirmButtonLore = createLore(
                messages.getString("Lore.LeftClickToConfirm"),
                messages.getString("Lore.Price2") + val,
                messages.getString("Lore.RightClickToCancel")
        );
        List<String> changeIncreaseValueLore = createLore(
                messages.getString("Lore.ClickToIncrease")
        );

        // Decrease button
        ItemStack decreaseOf1 = createButton(Material.REDSTONE_BLOCK, 1, changeDecreaseValueLore, SpigotPrison.format("&3" + rankName + " " + val + " - 1" ));
        inv.setItem(1, decreaseOf1);
        ItemStack decreaseOf5 = createButton(Material.REDSTONE_BLOCK, 10, changeDecreaseValueLore, SpigotPrison.format("&3" + rankName + " " + val + " - 10"));
        inv.setItem(10, decreaseOf5);
        ItemStack decreaseOf10 = createButton(Material.REDSTONE_BLOCK, 1, changeDecreaseValueLore, SpigotPrison.format("&3" + rankName + " " + val + " - 100"));
        inv.setItem(19, decreaseOf10);
        ItemStack decreaseOf50 = createButton(Material.REDSTONE_BLOCK, 1, changeDecreaseValueLore, SpigotPrison.format("&3" + rankName + " " + val + " - 1000"));
        inv.setItem(28, decreaseOf50);
        ItemStack decreaseOf100 = createButton(Material.REDSTONE_BLOCK, 1, changeDecreaseValueLore, SpigotPrison.format("&3" + rankName + " " + val + " - 10000"));
        inv.setItem(37, decreaseOf100);


        // Create a button and set the position
        ItemStack confirmButton = createButton(Material.TRIPWIRE_HOOK, 1, confirmButtonLore, SpigotPrison.format("&3" + "Confirm: " + rankName + " " + val));
        inv.setItem(22, confirmButton);


        // Increase button
        ItemStack increaseOf1 = createButton(Material.EMERALD_BLOCK, 1, changeIncreaseValueLore, SpigotPrison.format("&3" + rankName + " " + val + " + 1" ));
        inv.setItem(7, increaseOf1);
        ItemStack increaseOf5 = createButton(Material.EMERALD_BLOCK, 10, changeIncreaseValueLore, SpigotPrison.format("&3" + rankName + " " + val + " + 10"));
        inv.setItem(16, increaseOf5);
        ItemStack increaseOf10 = createButton(Material.EMERALD_BLOCK, 1, changeIncreaseValueLore, SpigotPrison.format("&3" + rankName + " " + val + " + 100"));
        inv.setItem(25, increaseOf10);
        ItemStack increaseOf50 = createButton(Material.EMERALD_BLOCK, 1, changeIncreaseValueLore, SpigotPrison.format("&3" + rankName + " " + val + " + 1000"));
        inv.setItem(34, increaseOf50);
        ItemStack increaseOf100 = createButton(Material.EMERALD_BLOCK, 1, changeIncreaseValueLore, SpigotPrison.format("&3" + rankName + " " + val + " + 10000"));
        inv.setItem(43, increaseOf100);
    }

}
