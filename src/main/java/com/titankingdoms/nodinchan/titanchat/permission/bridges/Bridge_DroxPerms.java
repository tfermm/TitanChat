package com.titankingdoms.nodinchan.titanchat.permission.bridges;

import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.titankingdoms.nodinchan.titanchat.permission.PermissionBridge;

import de.hydrox.bukkit.DroxPerms.DroxPerms;
import de.hydrox.bukkit.DroxPerms.DroxPermsAPI;

public final class Bridge_DroxPerms extends PermissionBridge {
	
	private DroxPermsAPI api;

	public Bridge_DroxPerms() {
		super("DroxPerms");
		
		Plugin perm = plugin.getServer().getPluginManager().getPlugin("DroxPerms");
		
		if (perm != null) {
			api = ((DroxPerms) perm).getAPI();
			log(Level.INFO, getName() + " hooked");
		}
		
		plugin.getServer().getPluginManager().registerEvents(new ServerListener(new DroxPermsChecker()), plugin);
	}
	
	@Override
	public String getPrefix(Player player, Permissible permissible) {
		if (permissible == null)
			return "";
		
		String prefix = "";
		
		switch (permissible) {
		
		case GROUP:
			prefix = api.getGroupInfo(api.getPlayerGroup(player.getName()), "prefix");
			break;
			
		case PLAYER:
			prefix = api.getPlayerInfo(player.getName(), "prefix");
			break;
		}
		
		return (prefix != null) ? prefix : "";
	}
	
	@Override
	public String getSuffix(Player player, Permissible permissible) {
		if (permissible == null)
			return "";
		
		String prefix = "";
		
		switch (permissible) {
		
		case GROUP:
			prefix = api.getGroupInfo(api.getPlayerGroup(player.getName()), "suffix");
			break;
			
		case PLAYER:
			prefix = api.getPlayerInfo(player.getName(), "suffix");
			break;
		}
		
		return (prefix != null) ? prefix : "";
	}
	
	@Override
	public boolean hasPermission(Player player, String permission) {
		return player.hasPermission(permission);
	}
	
	@Override
	public boolean isEnabled() {
		return api != null;
	}
	
	public final class DroxPermsChecker extends PluginChecker {
		
		@Override
		public void onPluginDisable(Plugin plugin) {
			if (api != null) {
				if (plugin.getName().equals("DroxPerms")) {
					api = null;
					log(Level.INFO, getName() + " unhooked");
				}
			}
		}
		
		@Override
		public void onPluginEnable(Plugin plugin) {
			if (api == null) {
				Plugin perm = plugin.getServer().getPluginManager().getPlugin("DroxPerms");
				
				if (perm != null) {
					api = ((DroxPerms) perm).getAPI();
					log(Level.INFO, getName() + " hooked");
				}
			}
		}
	}
}