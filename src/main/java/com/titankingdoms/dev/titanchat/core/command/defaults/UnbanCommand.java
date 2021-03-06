/*
 *     TitanChat
 *     Copyright (C) 2012  Nodin Chan <nodinchan@live.com>
 *     
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *     
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *     
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.titankingdoms.dev.titanchat.core.command.defaults;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.titankingdoms.dev.titanchat.core.channel.Channel;
import com.titankingdoms.dev.titanchat.core.command.Command;
import com.titankingdoms.dev.titanchat.util.C;
import com.titankingdoms.dev.titanchat.util.Messaging;

public final class UnbanCommand extends Command {
	
	public UnbanCommand() {
		super("Unban");
		setAliases("ub", "pardon");
		setArgumentRange(1, 1024);
		setBriefDescription("Unbans the player(s)");
		setFullDescription(
				"Description: Unbans the player(s) from the specified channel\n" +
				"Aliases: 'unban', 'ub', 'pardon'\n" +
				"Usage: /titanchat <@[channel]> unban [player]...");
		setUsage("[player]...");
	}
	
	@Override
	public void execute(CommandSender sender, Channel channel, String[] args) {
		if (channel == null) {
			msg(sender, C.RED + "Please specify or join a channel to use the command");
			return;
		}
		
		switch (channel.getType()) {
		
		case CUSTOM:
		case NONE:
			for (String playerName : args) {
				OfflinePlayer player = getOfflinePlayer(playerName);
				
				if (channel.isBlacklisted(player)) {
					channel.getBlacklist().remove(player.getName());
					
					msg(player, C.RED + "You have been unbanned from " + channel.getName());
					
					if (!channel.isParticipating(sender.getName()))
						msg(sender, C.GOLD + getDisplayName(player.getName()) + " has been unbanned");
					
					Messaging.broadcast(channel, getDisplayName(player.getName()) + " has been unbanned");
					
				} else { msg(sender, C.RED + getDisplayName(player.getName()) + " was not banned"); }
			}
			break;
			
		default:
			msg(sender, C.RED + "Command not available for " + channel.getType().getName() + " channels");
			break;
		}
	}
	
	@Override
	public boolean permissionCheck(CommandSender sender, Channel channel) {
		if (channel.isAdmin(sender.getName()))
			return true;
		
		if (hasPermission(sender, "TitanChat.ban." + channel.getName()))
			return true;
		
		return false;
	}
}