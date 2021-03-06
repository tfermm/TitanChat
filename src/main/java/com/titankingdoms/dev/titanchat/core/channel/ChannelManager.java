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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.titankingdoms.dev.titanchat.TitanChat;
import com.titankingdoms.dev.titanchat.core.channel.standard.StandardLoader;
import com.titankingdoms.dev.titanchat.loading.Loader;
import com.titankingdoms.dev.titanchat.loading.Loader.ExtensionFilter;
import com.titankingdoms.dev.titanchat.util.C;
import com.titankingdoms.dev.titanchat.util.Debugger;
import com.titankingdoms.dev.titanchat.util.Messaging;

public final class ChannelManager {
	
	private final TitanChat plugin;
	
	private final Debugger db = new Debugger(2, "ChannelManager");
	
	private final Map<String, Channel> aliases;
	private final Map<String, Channel> channels;
	private final Map<String, ChannelLoader> loaders;
	private final Map<Type, Set<Channel>> types;
	
	public ChannelManager() {
		this.plugin = TitanChat.getInstance();
		
		if (getCustomChannelDirectory().mkdirs())
			plugin.log(Level.INFO, "Creating custom channel directory...");
		
		if (getLoaderDirectory().mkdirs())
			plugin.log(Level.INFO, "Creating channel loader directory...");
		
		this.aliases = new HashMap<String, Channel>();
		this.channels = new TreeMap<String, Channel>();
		this.loaders = new TreeMap<String, ChannelLoader>();
		this.types = new HashMap<Type, Set<Channel>>();
	}
	
	public void createChannel(CommandSender sender, String name, ChannelLoader loader) {
		db.i(sender.getName() + " is creating channel " + name);
		
		Channel channel = loader.create(sender, name, Type.NONE);
		register(channel);
		
		Messaging.sendMessage(sender, C.GOLD + "You have created " + channel.getName());
		
		channel.getAdmins().add(sender.getName());
		channel.join(plugin.getParticipantManager().getParticipant(sender.getName()));
		
		channel.getConfig().options().copyDefaults(true);
		channel.saveConfig();
	}
	
	public void deleteChannel(CommandSender sender, String name) {
		db.i(sender.getName() + " is deleting channel " + name);
		
		Channel channel = getChannel(name);
		channels.remove(channel.getName().toLowerCase());
		
		Messaging.sendMessage(sender, C.RED + "You have deleted " + channel.getName());
		Messaging.broadcast(channel, C.RED + channel.getName() + " has been deleted");
		
		for (String participantName : channel.getParticipants())
			channel.leave(plugin.getParticipantManager().getParticipant(participantName));
		
		File config = new File(getChannelDirectory(), channel.getName() + ".yml");
		
		if (config.exists())
			config.delete();
	}
	
	public boolean existingChannel(String name) {
		return channels.containsKey(name.toLowerCase());
	}
	
	public boolean existingChannel(Channel channel) {
		return existingChannel(channel.getName());
	}
	
	public boolean existingChannelAlias(String alias) {
		return aliases.containsKey(alias.toLowerCase());
	}
	
	public boolean existingLoader(String name) {
		return loaders.containsKey(name.toLowerCase());
	}
	
	public boolean existingLoader(ChannelLoader loader) {
		return existingLoader(loader.getName());
	}
	
	public Channel getChannel(String name) {
		return channels.get(name.toLowerCase());
	}
	
	public Channel getChannel(Player player) {
		return null;
	}
	
	public Channel getChannelByAlias(String alias) {
		return aliases.get(alias.toLowerCase());
	}
	
	public File getChannelDirectory() {
		return new File(plugin.getDataFolder(), "channels");
	}
	
	public List<Channel> getChannels() {
		return new ArrayList<Channel>(channels.values());
	}
	
	public Set<Channel> getChannels(Type type) {
		Set<Channel> channels = this.types.get(type);
		return (channels != null) ? new HashSet<Channel>(channels) : new HashSet<Channel>();
	}
	
	public File getCustomChannelDirectory() {
		return new File(plugin.getAddonManager().getAddonDirectory(), "channels");
	}
	
	public int getLimit() {
		return plugin.getConfig().getInt("channels.limit", -1);
	}
	
	public ChannelLoader getLoader(String name) {
		return loaders.get(name.toLowerCase());
	}
	
	public File getLoaderDirectory() {
		return new File(plugin.getAddonManager().getAddonDirectory(), "channel-loaders");
	}
	
	public List<ChannelLoader> getLoaders() {
		return new ArrayList<ChannelLoader>(loaders.values());
	}
	
	public void load() {
		db.i("Loading...");
		
		register(new StandardLoader());
		register(Loader.load(ChannelLoader.class, getLoaderDirectory()).toArray(new ChannelLoader[0]));
		
		if (!loaders.isEmpty())
			plugin.log(Level.INFO, "ChannelLoaders loaded: " + StringUtils.join(loaders.keySet(), ", "));
		
		load(getChannelDirectory().listFiles(new ExtensionFilter(".yml")));
		
		register(Loader.load(Channel.class, getCustomChannelDirectory()).toArray(new Channel[0]));
		
		if (!channels.isEmpty())
			plugin.log(Level.INFO, "Channels loaded: " + StringUtils.join(channels.keySet(), ", "));
	}
	
	private void load(File... files) {
		for (File file : files) {
			if (!file.getName().endsWith(".yml"))
				continue;
			
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			
			if (config.getString("channel-type") == null || config.getString("loader-type") == null) {
				db.w("Failed to load " + file + ": Invalid config");
				continue;
			}
			
			Type type = Type.fromName(config.getString("channel-type"));
			ChannelLoader loader = getLoader(config.getString("loader-type"));
			
			if (type == null || loader == null) {
				db.w("Failed to load " + file + ": Invalid loader/channel type");
				continue;
			}
			
			register(loader.load(file.getName().substring(0, file.getName().lastIndexOf(".yml")), type));
		}
	}
	
	public void register(Channel... channels) {
		for (Channel channel : channels) {
			if (existingChannel(channel))
				continue;
			
			this.channels.put(channel.getName().toLowerCase(), channel);
			this.aliases.put(channel.getName().toLowerCase(), channel);
			
			for (String alias : channel.getAliases())
				if (!existingChannelAlias(alias))
					this.aliases.put(alias.toLowerCase(), channel);
			
			if (!this.types.containsKey(channel.getType()))
				this.types.put(channel.getType(), new HashSet<Channel>());
			
			this.types.get(channel.getType()).add(channel);
			db.i("Registered Channel: " + channel.getName());
		}
	}
	
	public void register(ChannelLoader... loaders) {
		for (ChannelLoader loader : loaders) {
			if (existingLoader(loader))
				continue;
			
			this.loaders.put(loader.getName().toLowerCase(), loader);
			db.i("Registered ChannelLoader: " + loader.getName());
		}
	}
	
	public void reload() {
		db.i("Reloading...");
		
		for (ChannelLoader loader : getLoaders())
			loader.reload();
		
		for (Channel channel : getChannels())
			channel.reload();
	}
	
	public void unload() {
		db.i("Unloading...");
		
		aliases.clear();
		channels.clear();
		loaders.clear();
		types.clear();
	}
}