package com.titankingdoms.nodinchan.titanchat.command.commands;

import org.bukkit.entity.Player;

import com.titankingdoms.nodinchan.titanchat.channel.Channel;
import com.titankingdoms.nodinchan.titanchat.channel.ChannelManager;
import com.titankingdoms.nodinchan.titanchat.command.Command;
import com.titankingdoms.nodinchan.titanchat.command.CommandID;
import com.titankingdoms.nodinchan.titanchat.command.CommandInfo;

/**
 * RankingCommand - Command for promotion, demotion and whitelisting on Channels
 * 
 * @author NodinChan
 *
 */
public class RankingCommand extends Command {

	private ChannelManager cm;
	
	public RankingCommand() {
		this.cm = plugin.getChannelManager();
	}
	
	@CommandID(name = "Add", triggers = "add")
	@CommandInfo(description = "Whitelists the player for the channel", usage = "add [player] <channel>")
	public void add(Player player, String[] args) {
		if (args.length < 1) { invalidArgLength(player, "Add"); return; }
		
		try {
			if (cm.exists(args[1])) {
				if (cm.getChannel(args[1]).canRank(player)) {
					if (plugin.getPlayer(args[0]) != null) {
						plugin.whitelistMember(plugin.getPlayer(args[0]), cm.getChannel(args[1]));
						plugin.sendInfo(player, plugin.getPlayer(args[0]).getDisplayName() + " has been added to the Member List");
						
					} else { plugin.sendWarning(player, "Player not online"); }
					
				} else { plugin.sendWarning(player, "You do not have permission"); }
				
			} else { plugin.sendWarning(player, "No such channel"); }
			
		} catch (IndexOutOfBoundsException e) {
			if (cm.getChannel(player).canRank(player)) {
				if (plugin.getPlayer(args[0]) != null) {
					plugin.whitelistMember(plugin.getPlayer(args[0]), cm.getChannel(player));
					plugin.sendInfo(player, plugin.getPlayer(args[0]).getDisplayName() + " has been added to the Member List");
					
				} else { plugin.sendWarning(player, "Player not online"); }
				
			} else { plugin.sendWarning(player, "You do not have permission"); }
		}
	}
	
	@CommandID(name = "Demote", triggers = "demote")
	@CommandInfo(description = "Demotes the player of the channel", usage = "demote [player] <channel>")
	public void demote(Player player, String[] args) {
		if (args.length < 1) { invalidArgLength(player, "Demote"); }
		
		try {
			if (cm.exists(args[1])) {
				Channel channel = cm.getChannel(args[1]);
				
				if (channel.canRank(player)) {
					if (plugin.getPlayer(args[0]) != null) {
						Player targetPlayer = plugin.getPlayer(args[0]);
						
						if (channel.getAdminList().contains(targetPlayer.getName())) {
							channel.getAdminList().remove(targetPlayer.getName());
							channel.save();
							
							plugin.sendInfo(targetPlayer, "You have been demoted in " + channel.getName());
							plugin.sendInfo(channel.getParticipants(), targetPlayer.getDisplayName() + " has been demoted");
							
						} else { plugin.sendWarning(player, targetPlayer.getDisplayName() + " is not an Admin"); }
						
					} else { plugin.sendWarning(player, "Player not online"); }
					
				} else { plugin.sendWarning(player, "You do not have permission"); }
				
			} else { plugin.sendWarning(player, "No such channel"); }
			
		} catch (IndexOutOfBoundsException e) {
			Channel channel = cm.getChannel(player);
			
			if (channel.canRank(player)) {
				if (plugin.getPlayer(args[0]) != null) {
					Player targetPlayer = plugin.getPlayer(args[0]);
					
					if (channel.getAdminList().contains(targetPlayer.getName())) {
						channel.getAdminList().remove(targetPlayer.getName());
						channel.save();
						
						plugin.sendInfo(targetPlayer, "You have been demoted in " + channel.getName());
						plugin.sendInfo(channel.getParticipants(), targetPlayer.getDisplayName() + " has been demoted");
						
					} else { plugin.sendWarning(player, targetPlayer.getDisplayName() + " is not an Admin"); }
					
				} else { plugin.sendWarning(player, "Player not online"); }
				
			} else { plugin.sendWarning(player, "You do not have permission"); }
		}
	}
	
	@CommandID(name = "Promote", triggers = "promote")
	@CommandInfo(description = "Promotes the player of the channel", usage = "promote [player] <channel>")
	public void promote(Player player, String[] args) {
		if (args.length < 1) { invalidArgLength(player, "Promote"); }
		
		try {
			if (cm.exists(args[0])) {
				if (cm.getChannel(args[0]).canRank(player)) {
					if (plugin.getPlayer(args[1]) != null) {
						Player targetPlayer = plugin.getPlayer(args[1]);
						
						if (!cm.getChannel(args[0]).getAdminList().contains(player.getName())) {
							plugin.assignAdmin(targetPlayer, cm.getChannel(args[0]));
							plugin.sendInfo(player, "You have been promoted in " + cm.getExact(args[0]));
							plugin.sendInfo(cm.getChannel(args[0]).getParticipants(), targetPlayer.getDisplayName() + " has been promoted");
							
						} else { plugin.sendWarning(player, targetPlayer.getDisplayName() + " is already an Admin"); }
						
					} else { plugin.sendWarning(player, "Player not online"); }
					
					
				} else { plugin.sendWarning(player, "You do not have permission"); }
				
			} else { plugin.sendWarning(player, "No such channel"); }
			
		} catch (IndexOutOfBoundsException e) {
			if (cm.getChannel(player).canRank(player)) {
				if (plugin.getPlayer(args[0]) != null) {
					Player targetPlayer = plugin.getPlayer(args[0]);
					
					if (!cm.getChannel(player).getAdminList().contains(player.getName())) {
						plugin.assignAdmin(targetPlayer, cm.getChannel(player));
						plugin.sendInfo(player, "You have been promoted in " + cm.getChannel(player).getName());
						plugin.sendInfo(cm.getChannel(args[0]).getParticipants(), targetPlayer.getDisplayName() + " has been promoted");
						
					} else { plugin.sendWarning(player, targetPlayer.getDisplayName() + " is already an Admin"); }
					
				} else { plugin.sendWarning(player, "Player not online"); }
				
				
			} else { plugin.sendWarning(player, "You do not have permission"); }
		}
	}
}