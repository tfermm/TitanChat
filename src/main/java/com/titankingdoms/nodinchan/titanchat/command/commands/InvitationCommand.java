/*     Copyright (C) 2012  Nodin Chan <nodinchan@live.com>
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

package com.titankingdoms.nodinchan.titanchat.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.titankingdoms.nodinchan.titanchat.command.CommandBase;
import com.titankingdoms.nodinchan.titanchat.command.info.*;
import com.titankingdoms.nodinchan.titanchat.core.channel.Channel;
import com.titankingdoms.nodinchan.titanchat.core.channel.ChannelManager;
import com.titankingdoms.nodinchan.titanchat.core.channel.util.Invitation.Response;

/**
 * InvitationCommand - Commands for invitations
 * 
 * @author NodinChan
 *
 */
public class InvitationCommand extends CommandBase {

	private ChannelManager cm;
	
	public InvitationCommand() {
		this.cm = plugin.getManager().getChannelManager();
	}
	
	/**
	 * Accept Command - Accepts the channel join invitation and joins the channel
	 */
	@Command
	@CommandOption(requireChannel = true, allowConsoleUsage = false)
	@Description("Accepts the channel join invitation and joins the channel")
	@Usage("accept")
	public void accept(Player player, Channel channel, String[] args) {
		cm.getParticipant(player).getInvitation().response(channel, Response.ACCEPT);
		channel.join(player);
	}
	
	/**
	 * Decline Command - Declines the channel join invitation
	 */
	@Command
	@CommandOption(requireChannel = true, allowConsoleUsage = false)
	@Description("Declines the channel join invitation")
	@Usage("decline [channel]")
	public void decline(Player player, Channel channel, String[] args) {
		cm.getParticipant(player).getInvitation().response(channel, Response.DECLINE);
	}
	
	/**
	 * Invite Command - Invites the player to join the channel
	 */
	@Command
	@CommandOption(requireChannel = true)
	@Description("Invites the player to join the channel")
	@Usage("invite [player]")
	public void invite(CommandSender sender, Channel channel, String[] args) {
		if (channel.handleCommand(sender, "invite", args))
			return;
		
		try {
			Player targetPlayer = plugin.getPlayer(args[0]);
			
			if (isOffline(sender, args[0]))
				return;
			
			cm.getParticipant(targetPlayer).getInvitation().invite(channel, sender);
			
		} catch (IndexOutOfBoundsException e) { invalidArgLength(sender, "invite"); }
	}
}