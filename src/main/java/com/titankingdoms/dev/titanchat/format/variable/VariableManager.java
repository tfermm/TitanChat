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

package com.titankingdoms.dev.titanchat.format.variable;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.titankingdoms.dev.titanchat.TitanChat;
import com.titankingdoms.dev.titanchat.core.channel.Channel;
import com.titankingdoms.dev.titanchat.core.participant.Participant;
import com.titankingdoms.dev.titanchat.format.variable.defaults.*;

public final class VariableManager {
	
	private final TitanChat plugin;
	
	private File configFile;
	private FileConfiguration config;
	
	private final Pattern format = Pattern.compile("(%)([a-z0-9]+)", Pattern.CASE_INSENSITIVE);
	
	private final Map<String, PresetSet> permissionPresets;
	private final Map<String, PresetSet> userPresets;
	private final Map<String, FormatVariable> variables;
	
	public VariableManager() {
		this.plugin = TitanChat.getInstance();
		this.permissionPresets = new HashMap<String, PresetSet>();
		this.userPresets = new HashMap<String, PresetSet>();
		this.variables = new HashMap<String, FormatVariable>();
	}
	
	private boolean existingVariable(String formatTag) {
		return variables.containsKey(formatTag);
	}
	
	private boolean existingVariable(FormatVariable variable) {
		return existingVariable(getFormatTag(variable));
	}
	
	public FileConfiguration getConfig() {
		if (config == null)
			reloadConfig();
		
		return config;
	}
	
	public String getDefaultVariableFormat() {
		return getConfig().getString("formatting.default", "%var%");
	}
	
	public String getFormatTag(FormatVariable variable) {
		return getFormatTag(variable.getName(), variable.getDefaultFormatTag());
	}
	
	private String getFormatTag(String name, String def) {
		return getConfig().getString("variables." + name + ".tag", def);
	}
	
	public PresetSet getPermissionPresets(String group) {
		return permissionPresets.get(group.toLowerCase());
	}
	
	public PresetSet getUserPresets(String user) {
		return userPresets.get(user.toLowerCase());
	}
	
	private FormatVariable getVariable(String formatTag) {
		return variables.get(formatTag);
	}
	
	public String getVariableFormat(String formatTag) {
		return getConfig().getString("formatting.variables." + formatTag, getDefaultVariableFormat());
	}
	
	public void load() {
		register(
				new ChannelTagVariable(),
				new DisplayNameVariable(),
				new GroupPrefixVariable(),
				new GroupSuffixVariable(),
				new NameVariable(),
				new PrefixVariable(),
				new SuffixVariable()
		);
		
		ConfigurationSection userSection = getConfig().getConfigurationSection("presets.users");
		
		for (String name : userSection.getKeys(false)) {
			ConfigurationSection nameSection = userSection.getConfigurationSection(name);
			
			if (!userPresets.containsKey(name))
				userPresets.put(name.toLowerCase(), new PresetSet());
			
			PresetSet set = userPresets.get(name.toLowerCase());
			
			for (String type : nameSection.getKeys(false))
				set.registerVariable(new PresetVariable(type, nameSection.getString(type)));
		}
		
		ConfigurationSection permissionSection = getConfig().getConfigurationSection("presets.permissions");
		
		for (String group : permissionSection.getKeys(false)) {
			ConfigurationSection groupSection = permissionSection.getConfigurationSection(group);
			int priority = groupSection.getInt("priority", 0);
			
			if (!permissionPresets.containsKey(group.toLowerCase()))
				permissionPresets.put(group.toLowerCase(), new PresetSet(group, priority));
			
			PresetSet set = permissionPresets.get(group.toLowerCase());
			
			for (String type : groupSection.getKeys(false))
				set.registerVariable(new PresetVariable(type, groupSection.getString(type)));
		}
	}
	
	public String parse(Participant sender, Channel channel, String format) {
		StringBuffer parsed = new StringBuffer();
		Matcher match = this.format.matcher(format);
		
		PresetSet userSets = null;
		List<PresetSet> permissionSets = new ArrayList<PresetSet>();
		
		if (this.userPresets.containsKey(sender.getName().toLowerCase()))
			userSets = getUserPresets(sender.getName());
		
		for (PresetSet set : permissionPresets.values())
			if (set.hasPermission(sender))
				permissionSets.add(set);
		
		if (!permissionSets.isEmpty()) {
			Collections.sort(permissionSets);
			Collections.reverse(permissionSets);
		}
		
		while (match.find()) {
			String varFormat = getVariableFormat(match.group());
			
			if (userSets != null) {
				if (userSets.existingVariable(match.group())) {
					String variable = userSets.getVariable(match.group()).getVariable(sender, channel);
					String rep = (!variable.isEmpty()) ? varFormat.replace("%var%", variable) : "";
					match.appendReplacement(parsed, rep);
					continue;
				}
			}
			
			if (!permissionSets.isEmpty()) {
				boolean cont = false;
				
				for (PresetSet set : permissionSets) {
					if (set.existingVariable(match.group())) {
						String variable = set.getVariable(match.group()).getVariable(sender, channel);
						String rep = (!variable.isEmpty()) ? varFormat.replace("%var%", variable) : "";
						match.appendReplacement(parsed, rep);
						cont = true;
						break;
					}
				}
				
				if (cont)
					continue;
			}
			
			if (!existingVariable(match.group())) {
				match.appendReplacement(parsed, match.group());
				continue;
			}
			
			String variable = getVariable(match.group()).getVariable(sender, channel);
			String rep = (!variable.isEmpty()) ? varFormat.replace("%var%", variable) : "";
			match.appendReplacement(parsed, rep);
		}
	
		return match.appendTail(parsed).toString();
	}
	
	public void register(FormatVariable... variables) {
		for (FormatVariable variable : variables) {
			if (existingVariable(variable))
				continue;
			
			this.variables.put(getFormatTag(variable), variable);
		}
	}
	
	public void reloadConfig() {
		if (configFile == null)
			configFile = new File(plugin.getDataFolder(), "variables.yml");
		
		config = YamlConfiguration.loadConfiguration(configFile);
		
		InputStream defConfigStream = plugin.getResource("variables.yml");
		
		if (defConfigStream != null)
			config.setDefaults(YamlConfiguration.loadConfiguration(defConfigStream));
	}
	
	public void saveConfig() {
		if (configFile == null || config == null)
			return;
		
		try { config.save(configFile); } catch (Exception e) {}
	}
}