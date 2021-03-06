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

package com.titankingdoms.dev.titanchat.loading;

import java.io.File;
import java.io.InputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.titankingdoms.dev.titanchat.TitanChat;

public class Loadable implements Comparable<Loadable> {
	
	protected final TitanChat plugin;
	
	private final String name;
	
	private ClassLoader loader;
	private File file;
	private File dataFolder;
	
	private File configFile;
	private FileConfiguration config;
	
	private boolean initialised = false;
	
	public Loadable(String name) {
		this.plugin = TitanChat.getInstance();
		this.name = name;
	}
	
	public int compareTo(Loadable loadable) {
		return getName().compareTo(loadable.getName());
	}
	
	public final ClassLoader getClassLoader() {
		return loader;
	}
	
	public FileConfiguration getConfig() {
		if (config == null)
			reloadConfig();
		
		return config;
	}
	
	public File getDataFolder() {
		return dataFolder;
	}
	
	protected final File getFile() {
		return file;
	}
	
	public final String getName() {
		return name;
	}
	
	public InputStream getResource(String name) {
		return loader.getResourceAsStream(name);
	}
	
	public InitResult init() {
		return new InitResult(true);
	}
	
	protected final void initialise(ClassLoader loader, File file, File dataFolder) {
		if (initialised)
			return;
		
		this.loader = loader;
		this.file = file;
		this.dataFolder = dataFolder;
		this.initialised = true;
	}
	
	public void reloadConfig() {
		if (configFile == null)
			configFile = new File(dataFolder, "config.yml");
		
		config = YamlConfiguration.loadConfiguration(configFile);
		
		InputStream defConfigStream = getResource("config.yml");
		
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
		}
	}
	
	public void saveConfig() {
		if (configFile == null || config == null)
			return;
		
		try { config.save(configFile); } catch (Exception e) {}
	}
	
	public final class InitResult {
		
		private final boolean success;
		private final String message;
		
		public InitResult(boolean success) {
			this(success, "");
		}
		
		public InitResult(boolean success, String message) {
			this.success = success;
			this.message = message;
		}
		
		public String getMessage() {
			return message;
		}
		
		public boolean getResult() {
			return success;
		}
	}
}