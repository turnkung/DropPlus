package me.araidkub.dropplus;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	private static Main plugin;
	private static Logger logger = Logger.getLogger("Drops");

	@Override
	public void onEnable() {
		plugin = this;
		Utils.generateDefaultConfig();
		getServer().getPluginManager().registerEvents(new EventListener(), this);
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public static Main getPlugin() {
		return plugin;
	}
	
	public Logger getLogger() {
		return logger;
	}
}
