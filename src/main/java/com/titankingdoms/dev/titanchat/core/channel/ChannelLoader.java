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

package com.titankingdoms.dev.titanchat.core.channel;

import org.bukkit.command.CommandSender;

import com.titankingdoms.dev.titanchat.loading.Loadable;

public abstract class ChannelLoader extends Loadable {
	
	public ChannelLoader(String name) {
		super(name);
	}
	
	public abstract Channel create(CommandSender sender, String name, Type type);
	
	public abstract Channel load(String name, Type type);
	
	public abstract void reload();
}