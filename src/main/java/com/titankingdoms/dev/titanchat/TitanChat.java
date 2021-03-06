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

package com.titankingdoms.dev.titanchat;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import com.titankingdoms.dev.titanchat.core.addon.AddonManager;
import com.titankingdoms.dev.titanchat.core.channel.Channel;
import com.titankingdoms.dev.titanchat.core.channel.ChannelManager;
import com.titankingdoms.dev.titanchat.core.channel.Type;
import com.titankingdoms.dev.titanchat.core.command.CommandManager;
import com.titankingdoms.dev.titanchat.core.participant.ParticipantManager;
import com.titankingdoms.dev.titanchat.format.FormatHandler;
import com.titankingdoms.dev.titanchat.help.Help;
import com.titankingdoms.dev.titanchat.metrics.Metrics;
import com.titankingdoms.dev.titanchat.util.C;
import com.titankingdoms.dev.titanchat.util.Debugger;
import com.titankingdoms.dev.titanchat.util.MessageProperties;
import com.titankingdoms.dev.titanchat.util.Messaging;
import com.titankingdoms.dev.titanchat.vault.Vault;

public final class TitanChat extends JavaPlugin {
	
	private static TitanChat instance;
	
	private final String NAME = "TitanChat v4.0";
	
	private static final Logger log = Logger.getLogger("TitanLog");
	private static final Debugger db = new Debugger(0, "TitanChat");
	
	private AddonManager addonManager;
	private ChannelManager channelManager;
	private CommandManager commandManager;
	private ParticipantManager participantManager;
	private FormatHandler formatHandler;
	
	private Help help;
	
	public void chatLog(String line) {
		if (getConfig().getBoolean("logging.colouring"))
			getConsoleSender().sendMessage(line.trim());
		else
			getConsoleSender().sendMessage(line.replaceAll("(?i)(\u00A7)([0-9a-fk-or])", "").trim());
	}
	
	public boolean enableChannels() {
		return getConfig().getBoolean("channels.enable");
	}
	
	public AddonManager getAddonManager() {
		return addonManager;
	}
	
	public ChannelManager getChannelManager() {
		return channelManager;
	}
	
	public CommandManager getCommandManager() {
		return commandManager;
	}
	
	public ConsoleCommandSender getConsoleSender() {
		return getServer().getConsoleSender();
	}
	
	public FormatHandler getFormatHandler() {
		return formatHandler;
	}
	
	public Help getHelp() {
		return help;
	}
	
	public static TitanChat getInstance() {
		return instance;
	}
	
	public OfflinePlayer getOfflinePlayer(String name) {
		OfflinePlayer player = getServer().getOfflinePlayer(name);
		return player;
	}
	
	public ParticipantManager getParticipantManager() {
		return participantManager;
	}
	
	public Player getPlayer(String name) {
		return getServer().getPlayer(name);
	}
	
	private boolean initMetrics() {
		if (!getConfig().getBoolean("metrics-statistics", true))
			return true;
		
		log(Level.INFO, "Hooking Metrics");
		
		try {
			Metrics metrics = new Metrics(this);
			
			if (metrics.isOptOut())
				return true;
			
			return metrics.start();
			
		} catch (Exception e) { return false; }
	}
	
	private void loadDefaultMessageProperties() {
		MessageProperties.setDefault("invalid_arg_length", "Invalid Argument Length");
		MessageProperties.setDefault("no_permission", "You do not have permission");
	}
	
	public void log(Level level, String msg) {
		log.log(level, "[" + NAME + "] " + msg);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("titanchat")) {
			if (args.length < 1 || (args[0].startsWith("@") && args.length < 2)) {
				db.i("/TitanChat");
				Messaging.sendMessage(sender, C.DARK_PURPLE + "You are running " + C.GOLD + this);
				Messaging.sendMessage(sender, C.GOLD + "\"/titanchat help [page]\" for help");
				return true;
			}
			
			String commandName = "";
			String channelName = "";
			
			if (args[0].startsWith("@")) {
				commandName = args[1];
				channelName = args[0].substring(1);
				args = Arrays.copyOfRange(args, 2, args.length);
				
			} else {
				commandName = args[0];
				args = Arrays.copyOfRange(args, 1, args.length);
			}
			
			Channel channel = null;
			
			if (!channelName.isEmpty()) {
				if (!channelManager.existingChannel(channelName)) {
					Messaging.sendMessage(sender, C.RED + "Channel does not exist");
					return true;
				}
				
				channel = channelManager.getChannel(channelName);
				
			} else { channel = participantManager.getParticipant(sender).getCurrentChannel(); }
			
			db.i("Dispatching command...");
			commandManager.dispatch(sender, channel, commandName, args);
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onDisable() {
		log(Level.INFO, "is now disabling...");
		log(Level.INFO, "Unloading managers...");
		
		addonManager.unload();
		channelManager.unload();
		commandManager.unload();
		participantManager.unload();
		
		log(Level.INFO, "is now disabled");
	}
	
	@Override
	public void onEnable() {
		log(Level.INFO, "is now enabling...");
		
		if (instance == null)
			instance = this;
		
		Vault.initialise(getServer());
		
		for (int id : getConfig().getIntegerList("logging.debug"))
			Debugger.startDebug(id);
		
		registerListener(new TitanChatListener());
		
		if (!initMetrics())
			log(Level.WARNING, "Failed to hook into Metrics");
		
		
		InputStream permissionStream = getResource("permissions.yml");
		
		if (permissionStream != null) {
			Map<?, ?> yamlMap = (Map<?, ?>) new Yaml(new SafeConstructor()).load(permissionStream);
			Map<?, ?> permissionMap = (Map<?, ?>) yamlMap.get("permissions");
			
			List<Permission> permissions = new LinkedList<Permission>();
			
			if (permissionMap != null)
				permissions.addAll(Permission.loadPermissions(permissionMap, "Permission node '%s' in plugin description file for " + getDescription().getFullName() + " is invalid", PermissionDefault.OP));
			
			for (Permission permission : permissions)
				try { getServer().getPluginManager().addPermission(permission); } catch (Exception e) {}
		}
		
		addonManager.load();
		channelManager.load();
		commandManager.load();
		participantManager.load();
		formatHandler.getVariableManager().load();
		help.load();
		
		if (channelManager.getChannels(Type.DEFAULT).isEmpty())
			log(Level.WARNING, "A default channel is not defined");
		
		log(Level.INFO, "is now enabled");
	}
	
	@Override
	public void onLoad() {
		instance = this;
		
		File config = new File(getDataFolder(), "config.yml");
		
		if (!config.exists()) {
			log(Level.INFO, "Loading default config...");
			saveResource("config.yml", false);
		}
		
		this.addonManager = new AddonManager();
		this.channelManager = new ChannelManager();
		this.commandManager = new CommandManager();
		this.participantManager = new ParticipantManager();
		this.formatHandler = new FormatHandler();
		this.help = new Help();
		
		if (channelManager.getChannelDirectory().mkdirs()) {
			log(Level.INFO, "Creating channel directory...");
			saveResource("channels/Default.yml", false);
			saveResource("channels/Global.yml", false);
			saveResource("channels/Local.yml", false);
			saveResource("channels/Password.yml", false);
			saveResource("channels/Private.yml", false);
			saveResource("channels/Public.yml", false);
			saveResource("channels/README.txt", false);
			saveResource("channels/Staff.yml", false);
			saveResource("channels/World.yml", false);
		}
		
		File variables = new File(getDataFolder(), "variables.yml");
		
		if (!variables.exists()) {
			log(Level.INFO, "Loading default variables.yml...");
			saveResource("variables.yml", false);
		}
		
		MessageProperties.reloadProperties();
		loadDefaultMessageProperties();
	}
	
	public void registerListener(Listener... listeners) {
		for (Listener listener : listeners)
			getServer().getPluginManager().registerEvents(listener, this);
	}
}