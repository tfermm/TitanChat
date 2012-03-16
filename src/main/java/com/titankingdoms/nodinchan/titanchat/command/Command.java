package com.titankingdoms.nodinchan.titanchat.command;

import java.lang.reflect.Method;

import org.bukkit.entity.Player;

import com.titankingdoms.nodinchan.titanchat.TitanChat;
import com.titankingdoms.nodinchan.titanchat.addon.Addon;
import com.titankingdoms.nodinchan.titanchat.channel.CustomChannel;

/**
 * Command - Command base
 * 
 * @author NodinChan
 *
 */
public class Command {

	protected final TitanChat plugin;
	
	public Command() {
		this.plugin = TitanChat.getInstance();
	}
	
	/**
	 * Called when it is loaded as an external command
	 */
	public void init() {}
	
	/**
	 * Sends a warning for invalid argument length
	 * 
	 * @param player the player to send to
	 * 
	 * @param name the command's name
	 */
	public final void invalidArgLength(Player player, String name) {
		plugin.sendWarning(player, "Invalid Argument Length");
		
		Method method = plugin.getCommandManager().getCommandExecutor(name).getMethod();
		
		if (method.getAnnotation(CommandInfo.class) != null)
			plugin.sendInfo(player, "Usage: /titanchat " + method.getAnnotation(CommandInfo.class).usage());
	}
	
	/**
	 * Registers the addon
	 * 
	 * @param addon the addon to register
	 */
	public final void register(Addon addon) {
		plugin.getAddonManager().register(addon);
	}
	
	/**
	 * Registers the command
	 * 
	 * @param command the command to register
	 */
	public final void register(Command command) {
		plugin.getCommandManager().register(command);
	}
	
	/**
	 * Registers the custom channel
	 * 
	 * @param channel the channel to register
	 */
	public final void register(CustomChannel channel) {
		plugin.getChannelManager().register(channel);
	}
}