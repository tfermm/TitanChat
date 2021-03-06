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

package com.titankingdoms.dev.titanchat.format.variable.defaults;

import org.bukkit.entity.Player;

import com.titankingdoms.dev.titanchat.core.channel.Channel;
import com.titankingdoms.dev.titanchat.core.participant.Participant;
import com.titankingdoms.dev.titanchat.format.variable.FormatVariable;
import com.titankingdoms.dev.titanchat.vault.Vault;

public final class GroupPrefixVariable implements FormatVariable {
	
	public String getDefaultFormatTag() {
		return "%gprefix";
	}
	
	public String getName() {
		return "GroupPrefix";
	}
	
	public String getVariable(Participant sender, Channel channel) {
		if (sender.getCommandSender() instanceof Player)
			return Vault.getGroupPrefix((Player) sender.getCommandSender());
		
		return "";
	}
}