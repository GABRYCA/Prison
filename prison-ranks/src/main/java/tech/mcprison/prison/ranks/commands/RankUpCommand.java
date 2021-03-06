/*
 * Copyright (C) 2017-2020 The MC-Prison Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tech.mcprison.prison.ranks.commands;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.PrisonAPI;
import tech.mcprison.prison.commands.Arg;
import tech.mcprison.prison.commands.BaseCommands;
import tech.mcprison.prison.commands.Command;
import tech.mcprison.prison.integration.EconomyIntegration;
import tech.mcprison.prison.internal.CommandSender;
import tech.mcprison.prison.internal.Player;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.ranks.PrisonRanks;
import tech.mcprison.prison.ranks.RankUtil;
import tech.mcprison.prison.ranks.RankUtil.PromoteForceCharge;
import tech.mcprison.prison.ranks.RankUtil.RankupModes;
import tech.mcprison.prison.ranks.RankUtil.RankupStatus;
import tech.mcprison.prison.ranks.RankupResults;
import tech.mcprison.prison.ranks.data.Rank;
import tech.mcprison.prison.ranks.data.RankLadder;
import tech.mcprison.prison.ranks.data.RankPlayer;
import tech.mcprison.prison.ranks.managers.LadderManager;

/**
 * The commands for this module.
 *
 * @author Faizaan A. Datoo
 * @author GABRYCA
 * @author RoyalBlueRanger
 */
public class RankUpCommand 
				extends BaseCommands {

	public RankUpCommand() {
		super( "RankUpCommand" );
	}
	
    /*
     * /rankup command
     */
	
    @Command(identifier = "rankupMax", 
    			description = "Ranks up to the max rank that the player can afford. If the player has the " +
    					"perm ranks.rankupmax.prestige it will try to rankup prestige once it maxes out " +
    					"on the default ladder.", 
    			permissions = "ranks.user", 
    			altPermissions = "ranks.rankupmax.[ladderName] ranks.rankupmax.prestige", 
    			onlyPlayers = false) 
    public void rankUpMax(CommandSender sender,
    		@Arg(name = "ladder", description = "The ladder to rank up on.", def = "default")  String ladder 
    		) {
    	rankUpPrivate(sender, ladder, RankupModes.MAX_RANKS, "ranks.rankupmax." );
    }
	
    @Command(identifier = "rankup", description = "Ranks up to the next rank.", 
			permissions = "ranks.user", altPermissions = "ranks.rankup.[ladderName]", onlyPlayers = true) 
    public void rankUp(CommandSender sender,
		@Arg(name = "ladder", description = "The ladder to rank up on.", def = "default")  String ladder
		) {
    	rankUpPrivate(sender, ladder, RankupModes.ONE_RANK, "ranks.rankup." );
    }

    private void rankUpPrivate(CommandSender sender, String ladder, RankupModes mode, String permission ) {

        // RETRIEVE THE LADDER

        // This player has to have permission to rank up on this ladder.
        if (!ladder.equalsIgnoreCase("default") && !sender
            .hasPermission(permission + ladder.toLowerCase())) {
            Output.get()
                .sendError(sender, "You need the permission '%s' to rank up on this ladder.",
                		permission + ladder.toLowerCase());
            return;
        }

        
        // 
        if ( mode == null ) {
        	
        	Output.get()
        		.sendError(sender, "&7Invalid rankup mode. Internal failure. Please report." );
        	return;
        }
        
        Player player = getPlayer( sender, null );
        
        //UUID playerUuid = player.getUUID();
        
		ladder = confirmLadder( sender, ladder );
		if ( ladder == null ) {
			// ladder cannot be null, 
			return;
		}

        RankPlayer rankPlayer = getPlayer( sender, player.getUUID(), player.getName() );
        Rank pRank = rankPlayer.getRank( ladder );
		Rank pRankSecond = rankPlayer.getRank("default");
		Rank pRankAfter = null;
		LadderManager lm = PrisonRanks.getInstance().getLadderManager();
		boolean willPrestige = false;

		// If the ladder's the prestige one, it'll execute all of this
		if ( ladder!= null && ladder.equalsIgnoreCase("prestiges")) {

			if (!(lm.getLadder("default").isPresent())){
				sender.sendMessage("&c[ERROR] There isn't a default ladder! Please report this to an admin!");
				return;
			}
			if (!(lm.getLadder("default").get().getLowestRank().isPresent())){
				sender.sendMessage("&c[ERROR] Can't get the lowest rank! Please report this to an admin!");
				return;
			}

			Rank rank = lm.getLadder("default").get().getLowestRank().get();

			while (rank.rankNext != null) {
				rank = rank.rankNext;
			}

			if (!(rank == pRankSecond)) {
				sender.sendMessage("&cYou aren't at the last rank!");
				return;
			}
			// IF everything's ready, this will be true and the prestige method will start
			willPrestige = true;
		}
        
        // Get currency if it exists, otherwise it will be null if the Rank has no currency:
        String currency = rankPlayer == null || pRank == null ? null : pRank.currency;

		boolean rankupWithSuccess = false;

        if ( ladder != null && rankPlayer != null ) {
        	RankupResults results = new RankUtil().rankupPlayer(rankPlayer, ladder, sender.getName());
        	
        	processResults( sender, null, results, true, null, ladder, currency );

        	if (results.getStatus() == RankupStatus.RANKUP_SUCCESS && mode == RankupModes.MAX_RANKS && 
        									!ladder.equals("prestiges")) {
        		rankUpPrivate( sender, ladder, mode, permission );
        	}
        	if (results.getStatus() == RankupStatus.RANKUP_SUCCESS){
        		rankupWithSuccess = true;
			}

        	// Get the player rank after
        	pRankAfter = rankPlayer.getRank(ladder);

        	
        	// Prestige method
        	prestigePlayer(player, rankPlayer, pRank, pRankAfter, lm, willPrestige, rankupWithSuccess);
        }
	}

	private void prestigePlayer(Player player, RankPlayer rankPlayer, Rank pRank, Rank pRankAfter, 
								LadderManager lm, boolean willPrestige, boolean rankupWithSuccess) {
		
		// Get the player rank after, just to check if it has success
    	Rank pRankSecond;
    	// Conditions
		if (willPrestige && rankupWithSuccess && pRankAfter != null && pRank != pRankAfter) {
			// Set the player rank to the first one of the default ladder
			PrisonAPI.dispatchCommand("ranks set rank " + player.getName() + " " + 
											lm.getLadder("default").get().getLowestRank().get().name + " default");
			// Get that rank
			pRankSecond = rankPlayer.getRank("default");
			// Check if the ranks match
			if (pRankSecond == lm.getLadder("default").get().getLowestRank().get()) {
				// Get economy
				EconomyIntegration economy = PrisonAPI.getIntegrationManager().getEconomy();
				
				if ( economy != null ) {
					
					// Set the player balance to 0 (reset)
					economy.setBalance(player, 0);
					// Send a message to the player because he did prestige!
					player.sendMessage("&7[&3Congratulations&7] &3You've &6Prestige&3 to " + pRankAfter.tag + "&c!");
				}
				else {
					player.sendMessage( "&3No economy is available.  Cannot perform action." );
				}
			}
		}
	}


	@Command(identifier = "ranks promote", description = "Promotes a player to the next rank.",
    			permissions = "ranks.promote", onlyPlayers = false) 
    public void promotePlayer(CommandSender sender,
    	@Arg(name = "playerName", def = "", description = "Player name") String playerName,
        @Arg(name = "ladder", description = "The ladder to promote on.", def = "default") String ladder,
        @Arg(name = "chargePlayers", description = "Force the player to pay for the rankup (no_charge, charge_player)", 
        					def = "no_charge") String chargePlayer
    		) {

    	Player player = getPlayer( sender, playerName );
    	
    	if (player == null) {
    		sender.sendMessage( "&3You must be a player in the game to run this command, " +
    															"and/or the player must be online." );
    		return;
    	}
    	
    	PromoteForceCharge pForceCharge = PromoteForceCharge.fromString( chargePlayer );
    	if ( pForceCharge == null|| pForceCharge == PromoteForceCharge.refund_player ) {
    		sender.sendMessage( 
    				String.format( "&3Invalid value for chargePlayer. Valid values are: %s %s", 
    						PromoteForceCharge.no_charge.name(), PromoteForceCharge.charge_player.name()) );
    		return;
    	}

        UUID playerUuid = player.getUUID();
        
		ladder = confirmLadder( sender, ladder );

        RankPlayer rankPlayer = getPlayer( sender, playerUuid, player.getName() );
        Rank pRank = rankPlayer.getRank( ladder );
        
        // Get currency if it exists, otherwise it will be null if the Rank has no currency:
        String currency = rankPlayer == null || pRank == null ? null : pRank.currency;
        
        

        if ( ladder != null && rankPlayer != null ) {
        	RankupResults results = new RankUtil().promotePlayer(rankPlayer, ladder, 
        												player.getName(), sender.getName(), pForceCharge);
        	
        	processResults( sender, player, results, true, null, ladder, currency );
        }
    }


    @Command(identifier = "ranks demote", description = "Demotes a player to the next lower rank.", 
    			permissions = "ranks.demote", onlyPlayers = false) 
    public void demotePlayer(CommandSender sender,
    	@Arg(name = "playerName", def = "", description = "Player name") String playerName,
        @Arg(name = "ladder", description = "The ladder to demote on.", def = "default") String ladder,
        @Arg(name = "chargePlayers", description = "Refund the player for the demotion (no_charge, refund_player)", 
        				def = "no_charge") String refundPlayer
        ) {

    	Player player = getPlayer( sender, playerName );
    	
    	if (player == null) {
    		sender.sendMessage( "&3You must be a player in the game to run this command, " +
    															"and/or the player must be online." );
    		return;
    	}
    	
    	PromoteForceCharge pForceCharge = PromoteForceCharge.fromString( refundPlayer );
    	if ( pForceCharge == null || pForceCharge == PromoteForceCharge.charge_player ) {
    		sender.sendMessage( 
    				String.format( "&3Invalid value for refundPlayer. Valid values are: %s %s", 
    						PromoteForceCharge.no_charge.name(), PromoteForceCharge.refund_player.name()) );
    		return;
    	}
    	
        UUID playerUuid = player.getUUID();
        
		ladder = confirmLadder( sender, ladder );

        RankPlayer rankPlayer = getPlayer( sender, playerUuid, player.getName() );
        Rank pRank = rankPlayer.getRank( ladder );
        
        // Get currency if it exists, otherwise it will be null if the Rank has no currency:
        String currency = rankPlayer == null || pRank == null ? null : pRank.currency;

        if ( ladder != null && rankPlayer != null ) {
        	RankupResults results = new RankUtil().demotePlayer(rankPlayer, ladder, 
        												player.getName(), sender.getName(), pForceCharge);
        	
        	processResults( sender, player, results, false, null, ladder, currency );
        }
    }


    @Command(identifier = "ranks set rank", description = "Sets a play to a specified rank.", 
    			permissions = "ranks.setrank", onlyPlayers = false) 
    public void setRank(CommandSender sender,
    	@Arg(name = "playerName", def = "", description = "Player name") String playerName,
    	@Arg(name = "rankName", description = "The rank to assign to the player") String rank,
        @Arg(name = "ladder", description = "The ladder to demote on.", def = "default") String ladder) {

    	Player player = getPlayer( sender, playerName );
    	
    	if (player == null) {
    		sender.sendMessage( "&3You must be a player in the game to run this command, " +
    										"and/or the player must be online." );
    		return;
    	}

        setPlayerRank( player, rank, ladder, sender );
    }


	private void setPlayerRank( Player player, String rank, String ladder, CommandSender sender ) {
		UUID playerUuid = player.getUUID();
        
		ladder = confirmLadder( sender, ladder );

        RankPlayer rankPlayer = getPlayer( sender, playerUuid, player.getName() );
        Rank pRank = rankPlayer.getRank( ladder );
        
        // Get currency if it exists, otherwise it will be null if the Rank has no currency:
        String currency = rankPlayer == null || pRank == null ? null : pRank.currency;

        if ( ladder != null && rankPlayer != null ) {
        	RankupResults results = new RankUtil().setRank(rankPlayer, ladder, rank, 
        												player.getName(), sender.getName());
        	
        	processResults( sender, player, results, true, rank, ladder, currency );
        }
	}



	public String confirmLadder( CommandSender sender, String ladderName ) {
		String results = null;
		Optional<RankLadder> ladderOptional =
            PrisonRanks.getInstance().getLadderManager().getLadder(ladderName);

        // The ladder doesn't exist
        if (!ladderOptional.isPresent()) {
            Output.get().sendError(sender, "The ladder '%s' does not exist.", ladderName);
        }
        else {
        	results = ladderOptional.get().name;
        }
        return results;
	}


	public RankPlayer getPlayer( CommandSender sender, UUID playerUuid, String playerName ) {
		Optional<RankPlayer> playerOptional =
            PrisonRanks.getInstance().getPlayerManager().getPlayer(playerUuid, playerName);

        // Well, this isn't supposed to happen...
        if (!playerOptional.isPresent()) {
            Output.get().sendError(sender,
                "You don't exist! The server has no records of you. Try rejoining, " +
            									"or contact a server administrator for help.");
        }

        return playerOptional.isPresent() ? playerOptional.get() : null;
	}


	public void processResults( CommandSender sender, Player player, 
					RankupResults results, 
					boolean rankup, String rank, String ladder, String currency ) {
	
		switch (results.getStatus()) {
            case RANKUP_SUCCESS:
            	if ( rankup ) {
            		String message = String.format( "Congratulations! %s ranked up to rank '%s'. %s",
            				(player == null ? "You have" : player.getName()),
            				(results.getTargetRank() == null ? "" : results.getTargetRank().name), 
            				(results.getMessage() != null ? results.getMessage() : "") );
            		Output.get().sendInfo(sender, message);
            		Output.get().logInfo( "%s initiated rank change: %s", sender.getName(), message );
            		
            		String messageGlobal = String.format( "Congratulations! %s ranked up to rank '%s'.",
            				(player == null ? "Someone" : player.getName()),
            				(results.getTargetRank() == null ? "" : results.getTargetRank().name) );
            		broadcastToWholeServer( sender, messageGlobal );
            	} else {
	            	String message = String.format( "Unfortunately, %s has been demoted to rank '%s'. %s",
            				(player == null ? "You have" : player.getName()),
            				(results.getTargetRank() == null ? "" : results.getTargetRank().name), 
            				(results.getMessage() != null ? results.getMessage() : ""));
            		Output.get().sendInfo(sender, message);
            		Output.get().logInfo( "%s initiated rank change: %s", sender.getName(), message );
            		
            		String messageGlobal = String.format( "Unfortunately, %s has been demoted to rank '%s'.",
            				(player == null ? "Someone" : player.getName()),
            				(results.getTargetRank() == null ? "" : results.getTargetRank().name) );
            		broadcastToWholeServer( sender, messageGlobal );
				}
                break;
            case RANKUP_CANT_AFFORD:
                Output.get().sendError(sender,
                    "You don't have enough money to rank up! The next rank costs %s.",
                    RankUtil.doubleToDollarString(
                    				results.getTargetRank() == null ? 0 : results.getTargetRank().cost));
                break;
            case RANKUP_LOWEST:
            	Output.get().sendInfo(sender, "%s already at the lowest rank!",
            				(player == null ? "You are" : player.getName()));
            	break;
            case RANKUP_HIGHEST:
                Output.get().sendInfo(sender, "%s already at the highest rank!",
            				(player == null ? "You are" : player.getName()));
                break;
            case RANKUP_FAILURE:
                Output.get().sendError(sender,
                    "Failed to retrieve or write data. Your files may be corrupted. " +
                														"Alert a server administrator.");
                break;
            case RANKUP_NO_RANKS:
                Output.get().sendError(sender, "There are no ranks in this ladder.");
                break;
            case RANKUP_FAILURE_RANK_DOES_NOT_EXIST:
            	Output.get().sendError(sender, "The rank %s does not exist on this server.", rank);
            	break;
			case RANKUP_FAILURE_RANK_IS_NOT_IN_LADDER:            
				Output.get().sendError(sender, "The rank %s does not exist in the ladder %s.", rank, ladder);
				break;
            
			case RANKUP_FAILURE_CURRENCY_IS_NOT_SUPPORTED:
				Output.get().sendError(sender, "The currency, %s, is not supported by any " +
													"loaded economies.", results.getTargetRank().currency);
				break;
				
			case IN_PROGRESS:
				Output.get().sendError(sender, "Rankup failed to complete normally. No status was set.");
				break;
			default:
				break;
        }
	}

	
	
	private void broadcastToWholeServer( CommandSender sender, String message ) {
    	
		String broadcastRankups = Prison.get().getPlatform().getConfigString( "broadcast-rankups" );
		
		if ( broadcastRankups == null || broadcastRankups.equalsIgnoreCase( "true" ) ) {
			
			Player player = getPlayer( sender, sender.getName() );
			List<Player> players = Prison.get().getPlatform().getOnlinePlayers();
			
			for ( Player p : players ) {
				if ( !p.equals( player ) ) {
					p.sendMessage( message );
				}
			}
		}
    }
    
}
