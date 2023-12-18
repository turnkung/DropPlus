package me.araidkub.dropplus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import eu.decentsoftware.holograms.api.DHAPI;

import java.util.UUID;

public class Utils {
	
	private static Plugin plugin = Main.getPlugin();
	
	public static void addItems(UUID itemUUID, UUID playerUUID, String dropSource) {
		EventListener.dropMap.put(itemUUID, playerUUID);
		EventListener.sourceMap.put(itemUUID, dropSource);
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (EventListener.dropMap.containsKey(itemUUID)) {
					EventListener.dropMap.remove(itemUUID);
					
					EventListener.hologramList.remove(itemUUID.toString());
					DHAPI.removeHologram(itemUUID.toString());
					
					EventListener.sourceMap.remove(itemUUID);
				}
			}
			
		}, 60*20);
	}
	
	public static void removeItems(UUID playerUUID) {
		if (EventListener.dropMap.size() == 0) return;
		
		Map<UUID, UUID> dropMap = EventListener.dropMap;
		for (Iterator<Map.Entry<UUID, UUID>> iteration = dropMap.entrySet().iterator(); iteration.hasNext(); ) {
			Map.Entry<UUID, UUID> entryMap = iteration.next();
			if (((UUID)entryMap.getValue()).equals(playerUUID)) {
				iteration.remove();
			}
		}
	}
	
	public static void announceToOnlinePlayers(String message) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(message);
		}
	}
	
	public static void generateDefaultConfig() {
		plugin.getConfig().addDefault("items-to-announce", Arrays.asList(new String[] {"Ancient Seal - Red Shard", "Ancient Power - Black Shard"}));
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortbyValue(Map<K, V> map) {
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Entry.comparingByValue());
		Map<K, V> result = new LinkedHashMap<>();
		for (Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}