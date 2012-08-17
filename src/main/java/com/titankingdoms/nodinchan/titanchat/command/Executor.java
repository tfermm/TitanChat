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

package com.titankingdoms.nodinchan.titanchat.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.titankingdoms.nodinchan.titanchat.channel.Channel;
import com.titankingdoms.nodinchan.titanchat.command.info.*;

/**
 * Executor - Represents a command executor
 * 
 * @author NodinChan
 *
 */
public final class Executor implements Comparable<Executor> {
	
	private final CommandBase command;
	
	private final Method method;
	
	private final String name;
	private String[] aliases = new String[0];
	private String description = "";
	private String usage = "";
	
	private boolean channel = false;
	private boolean console = true;
	
	public Executor(CommandBase command, Method method) {
		this.method = method;
		this.command = command;
		this.name = method.getName();
		
		if (method.isAnnotationPresent(CommandOption.class)) {
			channel = method.getAnnotation(CommandOption.class).requireChannel();
			console = method.getAnnotation(CommandOption.class).allowConsoleUsage();
		}
		
		if (method.isAnnotationPresent(Aliases.class))
			this.aliases = method.getAnnotation(Aliases.class).value();
		
		if (method.isAnnotationPresent(Description.class))
			this.description = method.getAnnotation(Description.class).value();
		
		if (method.isAnnotationPresent(Usage.class))
			this.usage = method.getAnnotation(Usage.class).value();
	}
	
	/**
	 * Checks if the command allows console usage
	 * 
	 * @return True if the command can be used at the console
	 */
	public boolean allowConsoleUsage() {
		return console;
	}
	
	public int compareTo(Executor executor) {
		return getName().compareTo(executor.getName());
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof Executor)
			return ((Executor) object).method.equals(method) && ((Executor) object).command.equals(command);
		
		return false;
	}
	
	/**
	 * Executes the command
	 * 
	 * @param sender The command sender
	 * 
	 * @param channel The channel
	 * 
	 * @param args The arguments
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public void execute(CommandSender sender, Channel channel, String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		method.invoke(command, sender, channel, args);
	}
	
	/**
	 * Executes the command
	 * 
	 * @param sender The command sender
	 * 
	 * @param channel The channel
	 * 
	 * @param args The arguments
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public void execute(Player sender, Channel channel, String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		method.invoke(command, sender, channel, args);
	}
	
	/**
	 * Gets the command aliases
	 * 
	 * @return The aliases of the command
	 */
	public String[] getAliases() {
		return aliases;
	}
	
	/**
	 * Gets the command
	 * 
	 * @return The command
	 */
	public CommandBase getCommand() {
		return command;
	}
	
	/**
	 * Gets the command description
	 * 
	 * @return The description of the command
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Gets the method to execute the command
	 * 
	 * @return The method of the command
	 */
	public Method getMethod() {
		return method;
	}
	
	/**
	 * Gets the command name
	 * 
	 * @return The name of the command
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the command usage
	 * 
	 * @return The usage of the command
	 */
	public String getUsage() {
		return usage;
	}
	
	/**
	 * Checks if the command requires a channel
	 * 
	 * @return True if the command requires a channel
	 */
	public boolean requireChannel() {
		return channel;
	}
	
	@Override
	public String toString() {
		return "Command:" + name;
	}
}