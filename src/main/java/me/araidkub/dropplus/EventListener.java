package me.araidkub.dropplus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;

public class EventListener implements Listener {
	
	public static Plugin plugin = Main.getPlugin();
	
	public static Map<UUID, Map<UUID, Double>> entityDamageMap = new HashMap<>();
	public static Map<UUID, UUID> dropMap = new HashMap<>();
	public static Map<UUID, String> sourceMap = new HashMap<>();
	
	public static List<String> hologramList = new ArrayList<>();
	// public static List<String> itemToAnnounce = new ArrayList<>();
	
	private Logger logger = plugin.getLogger();
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity damaged = event.getEntity();
		Player damager = null;
		
		if (event.getDamager() instanceof Player) {
			damager = (Player) event.getDamager();
		}
		
		if (damager != null && damaged instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) damaged;
			Double damage = event.getFinalDamage();
			
			Map<UUID, Double> playerDamageMap = new HashMap<>();
			playerDamageMap.put(damager.getUniqueId(), damage);
			
			if (!entityDamageMap.containsKey(livingEntity.getUniqueId())) {
				entityDamageMap.put(livingEntity.getUniqueId(), playerDamageMap);
			} else {
				if (!entityDamageMap.get(livingEntity.getUniqueId()).containsKey(damager.getUniqueId())) {
					entityDamageMap.get(livingEntity.getUniqueId()).put(damager.getUniqueId(), damage);
				} else {
					Double oldTotalDamage = entityDamageMap.get(livingEntity.getUniqueId()).get(damager.getUniqueId());
					entityDamageMap.get(livingEntity.getUniqueId()).replace(damager.getUniqueId(), oldTotalDamage + damage);
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		LivingEntity livingEntity = event.getEntity();
		
		ItemStack titem = new ItemStack(Material.NETHERITE_INGOT);
		ItemMeta titemMeta = titem.getItemMeta();
		titemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&9Ancient Power - Black Shard"));
		titem.setItemMeta(titemMeta);
		
		List<ItemStack> customDrops = new ArrayList<>();
		customDrops.add(titem);
		
		if (!(livingEntity.getKiller() instanceof Player)) return;
		
		if (entityDamageMap.containsKey(livingEntity.getUniqueId())) {
			Map<UUID, Double> playerDamageMap = Utils.sortbyValue(entityDamageMap.get(livingEntity.getUniqueId()));
			
			String mostDamagedUUID = playerDamageMap.keySet().toArray()[playerDamageMap.size() - 1].toString();
			
			entityDamageMap.remove(livingEntity.getUniqueId());
	
			String mostDamagedPlayerName = plugin.getServer().getPlayer((UUID) playerDamageMap.keySet().toArray()[playerDamageMap.size() - 1]).getName();
			
			Collection<ItemStack> defaultDrops = event.getDrops();
			for (ItemStack itemDrop : defaultDrops) {
				Location location = livingEntity.getLocation().getBlock().getLocation();
				location.add(0.5, 1, 0.5);
				Item drop = livingEntity.getLocation().getWorld().dropItem(location, itemDrop);
				drop.setVelocity(new Vector(0, 0, 0));
				
				String dropSource = "defeated " + livingEntity.getName(); 
				Utils.addItems(drop.getUniqueId(), UUID.fromString(mostDamagedUUID), dropSource);
				
				List<String> lines = Arrays.asList(mostDamagedPlayerName + "'s " + drop.getName());
				Hologram hologram = DHAPI.createHologram(drop.getUniqueId().toString(), drop.getLocation(), lines);
				hologramList.add(hologram.getName());
			}
			
			for (ItemStack customDrop : customDrops) {
				Location location = livingEntity.getLocation().getBlock().getLocation();
				location.add(0.5, 1, 0.5);
				Item drop = livingEntity.getLocation().getWorld().dropItemNaturally(location, customDrop);
				
				String dropSource = "defeated " + livingEntity.getName();
				Utils.addItems(drop.getUniqueId(), UUID.fromString(mostDamagedUUID), dropSource);
				
				List<String> lines = Arrays.asList(mostDamagedPlayerName + "'s " + drop.getItemStack().getItemMeta().getDisplayName());
				Hologram hologram = DHAPI.createHologram(drop.getUniqueId().toString(), drop.getLocation(), lines);
				hologramList.add(hologram.getName());
				
			}
			
			event.getDrops().clear();
		}
	}
	
	@EventHandler
	public void onPlayerPickupItem(EntityPickupItemEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			Item item = event.getItem();
			if (dropMap.containsKey(item.getUniqueId()) && !dropMap.get(item.getUniqueId()).equals(player.getUniqueId())) {
				event.setCancelled(true);
			} else {
				dropMap.remove(item.getUniqueId());
				
				if (hologramList.contains(item.getUniqueId().toString())) {
					hologramList.remove(item.getUniqueId().toString());
					DHAPI.removeHologram(item.getUniqueId().toString());
				}
			}
			
			// Drop Announce
			List<?> itemToAnnounce = plugin.getConfig().getList("items-to-announce");
			if (item.getItemStack().hasItemMeta()) {
				logger.info("List Item to Announce : " + itemToAnnounce);
				logger.info("Current Item : " + ChatColor.stripColor(item.getItemStack().getItemMeta().getDisplayName()));
				if (itemToAnnounce.contains(ChatColor.stripColor(item.getItemStack().getItemMeta().getDisplayName()))) {
					String dropSource = sourceMap.get(item.getUniqueId());
					String message = ChatColor.translateAlternateColorCodes('&', "&7" + player.getName() + " got " + "&6" + item.getItemStack().getItemMeta().getDisplayName() + "&7 from " + dropSource);
					Utils.announceToOnlinePlayers(message);
					
					player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, (float) 1.5);
				}
			}
		}
	}
}