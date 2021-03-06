package tech.mcprison.prison.spigot.gui.rank;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.modules.Module;
import tech.mcprison.prison.modules.ModuleManager;
import tech.mcprison.prison.ranks.PrisonRanks;
import tech.mcprison.prison.ranks.data.Rank;
import tech.mcprison.prison.ranks.data.RankLadder;
import tech.mcprison.prison.ranks.data.RankPlayer;
import tech.mcprison.prison.ranks.managers.LadderManager;
import tech.mcprison.prison.ranks.managers.PlayerManager;
import tech.mcprison.prison.spigot.SpigotPrison;
import tech.mcprison.prison.spigot.gui.ListenersPrisonManager;
import tech.mcprison.prison.spigot.gui.SpigotGUIComponents;

/**
 * @author GABRYCA
 */
public class SpigotPlayerRanksGUI extends SpigotGUIComponents {

    private final Player player;

    private PrisonRanks rankPlugin;
    private RankPlayer rankPlayer;
    // Load config
    private final Configuration guiConfig = guiConfig();
    private final Configuration messages = messages();

    public SpigotPlayerRanksGUI(Player player) {
        this.player = player;

        // If you need to get a SpigotPlayer:
        //        SpigotPlayer sPlayer = new SpigotPlayer(p);

        Server server = SpigotPrison.getInstance().getServer();
        PrisonRanks rankPlugin;
        RankPlayer rPlayer;
        ModuleManager modMan = Prison.get().getModuleManager();
 	    Module module = modMan == null ? null : modMan.getModule( PrisonRanks.MODULE_NAME ).orElse( null );
        rankPlugin = (PrisonRanks) module;

 	    // Check
        if (!(checkRanks(player))){
            return;
        }

        if (rankPlugin == null){
            player.sendMessage(SpigotPrison.format("&cError: rankPlugin == null"));
            return;
        }

 	    if (rankPlugin.getPlayerManager() == null) {
 	        player.sendMessage(SpigotPrison.format("&cError: rankPlugin.getPlayerManager() == null"));
 	    	return;
 	    }

 	    PlayerManager playerManager = rankPlugin.getPlayerManager();
    	rPlayer = playerManager.getPlayer( player.getUniqueId(), player.getName() ).orElse( null );
        Plugin plugin = server.getPluginManager().getPlugin( PrisonRanks.MODULE_NAME );

        if (plugin instanceof PrisonRanks) {
            rankPlugin = (PrisonRanks) plugin;
            Optional<RankPlayer> oPlayer = rankPlugin.getPlayerManager().
            								getPlayer( getPlayer().getUniqueId(), getPlayer().getName() );
            if ( oPlayer.isPresent() ) {
                rPlayer = oPlayer.get();
            }
        }
        this.rankPlugin = rankPlugin;
        this.rankPlayer = rPlayer;

    }

    public Player getPlayer() {
        return player;
    }

    public PrisonRanks getRankPlugin() {
        return rankPlugin;
    }

    public RankPlayer getRankPlayer() {
        return rankPlayer;
    }

    public void open() {

        // First ensure the ranks module is enabled:
        if ( getRankPlugin() == null ) {
            // Error? Cannot open if Rank module is not loaded.
            getPlayer().closeInventory();
            return;
        }

        LadderManager lm = getRankPlugin().getLadderManager();
        Optional<RankLadder> ladder = lm.getLadder(guiConfig.getString("Options.Ranks.Ladder"));

        // Ensure ladder is present and that it has a rank:
        if (!ladder.isPresent() || !ladder.get().getLowestRank().isPresent()){
            getPlayer().sendMessage(SpigotPrison.format(messages.getString("Message.NoRanksFoundHelp1") + guiConfig.getString("Options.Ranks.Ladder") + messages.getString("Message.NoRanksFoundHelp2")));
            getPlayer().closeInventory();
            return;
        }

        // Get the dimensions and if needed increases them
        if (ladder.get().ranks.size() == 0) {
            getPlayer().sendMessage(SpigotPrison.format(messages.getString("Message.NoRanksFound")));
            return;
        }

        // Create the inventory and set up the owner, dimensions or number of slots, and title
        int dimension = (int) (Math.ceil(ladder.get().ranks.size() / 9D) * 9) + 9;

        // Create the inventory
        Inventory inv = Bukkit.createInventory(null, dimension, SpigotPrison.format("&3" + "Ranks -> PlayerRanks"));

        // Get many parameters
        RankLadder ladderData = ladder.get();
        Rank rank = ladderData.getLowestRank().get();
        Rank playerRank = getRankPlayer().getRank( ladderData ).orElse( null );

        // Call the whole GUI and build it
        if (guiBuilder(dimension, inv, rank, playerRank)) return;

        // Open the inventory
        openGUI(getPlayer(), inv);
    }

    private boolean guiBuilder( int dimension, Inventory inv, Rank rank, Rank playerRank) {
        try {
            buttonsSetup(dimension, inv, rank, playerRank);
        } catch (NullPointerException ex){
            getPlayer().sendMessage(SpigotPrison.format("&cThere's a null value in the GuiConfig.yml [broken]"));
            ex.printStackTrace();
            return true;
        }
        return false;
    }

    private void buttonsSetup(int dimension, Inventory inv, Rank rank, Rank playerRank) {

        // Not sure how you want to represent this:
        Material materialHas = Material.getMaterial(guiConfig.getString("Options.Ranks.Item_gotten_rank"));
        Material materialHasNot = Material.getMaterial(guiConfig.getString("Options.Ranks.Item_not_gotten_rank"));

        // Variables
        boolean playerHasThisRank = true;
        int hackyCounterEnchant = 0;
        int amount = 1;

        while ( rank != null ) {

            List<String> ranksLore = createLore(
                    messages.getString("Lore.Info"),
                    messages.getString("Lore.Price3") + rank.cost
            );

            ItemStack itemRank = createButton(
                    (playerHasThisRank ? materialHas : materialHasNot),
                    amount++, ranksLore, SpigotPrison.format(rank.tag));

            if (playerRank != null && playerRank.equals(rank)){
                playerHasThisRank = false;
            }

            if (!(playerHasThisRank)){
                if (hackyCounterEnchant <= 0) {
                    hackyCounterEnchant++;
                    if (guiConfig.getString("Options.Ranks.Enchantment_effect_current_rank").equalsIgnoreCase("true")) {
                        itemRank.addUnsafeEnchantment(Enchantment.LUCK, 1);
                    }
                }
            }

            inv.addItem(itemRank);
            rank = rank.rankNext;
        }

        List<String> rankupLore = createLore(
                messages.getString("Lore.IfYouHaveEnoughMoney"),
                messages.getString("Lore.ClickToRankup")
        );

        ItemStack rankupButton = createButton(Material.EMERALD_BLOCK, 1, rankupLore, SpigotPrison.format(messages.getString("Lore.Rankup")));
        inv.setItem(dimension - 5, rankupButton);
    }
}