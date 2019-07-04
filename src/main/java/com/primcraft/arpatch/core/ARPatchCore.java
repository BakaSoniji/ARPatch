package com.primcraft.arpatch.core;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ARPatchCore extends JavaPlugin implements Listener {

    private FileConfiguration config = new YamlConfiguration();

    private Map<Integer, DropChestProperties> hashes = new HashMap<>();

    @Override
    public void onEnable() {

        getLogger().info("====== AR Patch Enabled ======");

        initializeConfig();
        taskChainFactory = BukkitTaskChainFactory.create(this);

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void initializeConfig() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                FileUtils.copyToFile(getResource("config.yml"), configFile);
            }
            config.load(configFile);
            loadDropChestProperties();
        } catch (InvalidConfigurationException e) {
            getLogger().warning("Invalid config...");
            Bukkit.getPluginManager().disablePlugin(this);
        } catch (IOException e) {
            getLogger().warning("Failed while loading config");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void loadDropChestProperties() {
        List<Integer> keys = config.getConfigurationSection("drop_chest").getKeys(false)
                .stream().map(Integer::parseInt).collect(Collectors.toList());
        for (Integer key : keys) {
            String name = config
                    .getConfigurationSection("drop_chest")
                    .getConfigurationSection(String.valueOf(key))
                    .getString("name");
            String lootTable = config
                    .getConfigurationSection("drop_chest")
                    .getConfigurationSection(String.valueOf(key))
                    .getString("lootTable");
            hashes.put(key, new DropChestProperties(name, lootTable));
        }
        getLogger().info(String.format("Hashes: %s", hashes.keySet()));
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {

        int hashCode = event.getItemDrop().getItemStack().toString().hashCode();
        String team = event.getPlayer().getScoreboard().getEntryTeam(event.getPlayer().getName()).getName();

        // run only when in dungeon or in training area
        if (!("in_dungeon".equals(team) || "in_training".equals(team))) {
            return;
        }

        // if the drop item is one of the items in the config
        if (hashes.containsKey(hashCode)) {
            getLogger().info(event.getItemDrop().getItemStack().toString().hashCode() + "");

            // wait until the item has hit the ground, then execute
            newChain()
                    .delay(5)
                    .async(() -> {
                        while (!event.getItemDrop().isOnGround()) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .sync(() -> itemDropHandler(event))
                    .execute();
        }
    }

    /**
     * An reimplementation of the mcfunction handling drop chest spawning
     *
     * @param event the upstream event
     */
    private void itemDropHandler(PlayerDropItemEvent event) {
        int hashCode = event.getItemDrop().getItemStack().toString().hashCode();

        if (!hashes.containsKey(hashCode)) {
            return;
        }

        Location location = event.getItemDrop().getLocation();

        if (!(location.getBlock().isPassable()) || (location.clone().add(0, 1, 0).getBlock().getBlockData().getMaterial().isSolid())) {
            return;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();



        String name = hashes.get(hashCode).getName();
        String type = hashes.get(hashCode).getLootTable();
        List<String> commands = new ArrayList<>();
        commands.add(String.format(
                "playsound minecraft:block.note_block.xylophone voice @a[x=%1$s,y=%2$s,z=%3$s,distance=..10] " +
                        "%1$s %2$s %3$s 1 0.5",
                x, y, z
        ));
        commands.add(String.format(
                "particle minecraft:block white_wool %s %s %s 0.3 0.5 0.3 1 50",
                x, y + 0.2, z
        ));
        commands.add(String.format(
                "setblock %s %s %s minecraft:trapped_chest{CustomName:\"%s\"," +
                        "Lock:\"\",LootTable:\"ad:chests/%s\"}",
                x, y, z, name, type
        ));
        commands.forEach(c -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c));
        event.getItemDrop().remove();
    }



    @Override
    public void onDisable() {
        getLogger().info("====== AR Patch Disabled ======");
    }

    private static TaskChainFactory taskChainFactory;

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

    private static class DropChestProperties {
        private String name;

        private String lootTable;

        public DropChestProperties(String name, String lootTable) {
            this.name = name;
            this.lootTable = lootTable;
        }

        public String getName() {
            return name;
        }

        public String getLootTable() {
            return lootTable;
        }

        @Override
        public String toString() {
            return String.format("{name: %s, lootTable: %s}", name, lootTable);
        }
    }
}
