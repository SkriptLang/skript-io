package org.skriptlang.skript_io;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.util.Version;
import mx.kenzie.clockwork.io.IOQueue;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

public class SkriptIO extends JavaPlugin {
    
    public static IOConfig config;
    private static IOQueue queue;
    private SkriptAddon addon;
    private Types types;
    
    public static @NotNull IOQueue queue() {
        return queue;
    }
    
    public static File file(URI path) {
        try {
            if (path == null) return null;
            else if (path.getPath().isEmpty()) return null;
            else return new File(path.getPath());
        } catch (IllegalArgumentException ex) {
            SkriptIO.error(ex);
            return null;
        }
    }
    
    public static File fileNoError(URI path) {
        try {
            if (path == null) return null;
            else if (path.getPath().isEmpty()) return null;
            else return new File(path.getPath());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
    
    public static void error(String message) {
        Bukkit.getLogger().log(Level.SEVERE, message);
    }
    
    public static void error(Throwable throwable) {
        Bukkit.getLogger().log(Level.SEVERE, throwable.getMessage(), throwable);
    }
    
    @Override
    public void onEnable() {
        final PluginManager manager = this.getServer().getPluginManager();
        final Plugin skript = manager.getPlugin("Skript");
        if (skript == null || !skript.isEnabled()) {
            this.getLogger().severe(
                "[skript-io] Could not find Skript! Make sure you have it installed and that it properly loaded. Disabling...");
            manager.disablePlugin(this);
            return;
        } else if (!Skript.getVersion()
            .isLargerThan(new Version(2, 7, 0))) { // Skript is not any version after 2.5.3 (aka 2.6)
            this.getLogger().severe(
                "[skript-io] You are running an unsupported version of Skript. Please update to at least Skript 2.7.0. Disabling...");
            manager.disablePlugin(this);
            return;
        }
        this.types = new Types();
        try {
            this.addon = Skript.registerAddon(this);
            this.addon.loadClasses("org.skriptlang.skript_io.elements");
            this.types.register();
        } catch (IOException e) {
            this.getLogger().severe("An error occurred while trying to enable this addon.");
            SkriptIO.error(e);
            manager.disablePlugin(this);
        }
        queue = new IOQueue();
        this.loadConfig();
    }
    
    public void loadConfig() { // todo
        try {
            final File file = new File(this.getDataFolder(), "config.sk");
            if (!file.exists()) this.saveResource("config.sk", false);
            config = new IOConfig(file, false, false, ":");
        } catch (IOException ex) {
            this.getLogger().severe("[skript-io] Unable to load config.");
        }
    }
    
    @Override
    public void onDisable() {
        queue.shutdown(1000);
        this.addon = null;
        this.types = null;
    }
    
}
