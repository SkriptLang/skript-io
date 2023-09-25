package org.skriptlang.skript_io;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Version;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class SkriptIO extends JavaPlugin {
    
    private SkriptAddon addon;
    private Types types;
    
    @Override
    public void onEnable() {
        final PluginManager manager = this.getServer().getPluginManager();
        final Plugin skript = manager.getPlugin("Skript");
        if (skript == null || !skript.isEnabled()) {
            this.getLogger().severe(
                "[skript-gui] Could not find Skript! Make sure you have it installed and that it properly loaded. Disabling...");
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
            e.printStackTrace();
            manager.disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        this.addon = null;
    }
    
    public static File file(URI path) {
        try {
            if (path == null) return null;
            if (!path.isAbsolute()) Skript.error("'" + path + "' is not an absolute file path");
            else if (path.isOpaque()) Skript.error("'" + path + "' is not hierarchical");
            else if (!"file".equalsIgnoreCase(path.getScheme())) Skript.error("'" + path + "' is not a file path");
            else if (path.getPath().isEmpty()) return null;
            else return new File(path);
            return null;
        } catch (IllegalArgumentException ex) {
            Skript.error(ex.getMessage(), ErrorQuality.SEMANTIC_ERROR);
            return null;
        }
    }
    
}
