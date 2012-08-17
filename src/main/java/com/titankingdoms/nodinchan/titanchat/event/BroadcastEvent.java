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

package com.titankingdoms.nodinchan.titanchat.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.titankingdoms.nodinchan.titanchat.event.util.Message;

/**
 * BroadcastEvent - Called when the broadcast command is used
 * 
 * @author NodinChan
 *
 */
public final class BroadcastEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	
	private final CommandSender sender;
	
	private final Message message;
	
	public BroadcastEvent(CommandSender sender, Message message) {
		this.sender = sender;
		this.message = message;
	}
	
	/**
	 * Gets the format
	 * 
	 * @return The broadcast format
	 */
	public String getFormat() {
		return message.getFormat();
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	/**
	 * Gets the message
	 * 
	 * @return The broadcast message
	 */
	public String getMessage() {
		return message.getMessage();
	}
	
	/**
	 * Gets the command sender
	 * 
	 * @return The command sender
	 */
	public CommandSender getSender() {
		return sender;
	}
	
	/**
	 * Sets the format
	 * 
	 * @param format The new broadcast format
	 */
	public void setFormat(String format) {
		this.message.setFormat(format);
	}
	
	/**
	 * Sets the message
	 * 
	 * @param message The new broadcast message
	 */
	public void setMessage(String message) {
		this.message.setMessage(message);
	}
}