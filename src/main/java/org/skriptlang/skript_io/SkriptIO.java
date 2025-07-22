package org.skriptlang.skript_io;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.util.Version;
import mx.kenzie.clockwork.io.DataTask;
import mx.kenzie.clockwork.io.IOQueue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript_io.format.*;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

public class SkriptIO extends JavaPlugin {

    private static SkriptIO instance;
    public static final Version MINIMUM_SUPPORTED_SK_VERSION = new Version(2, 11, 2);
    public static final URI ROOT = URI.create("/");
    public static boolean testMode;
    private static IOQueue queue;
    private static IOQueue remoteQueue;
    private static final ThreadLocal<Boolean> areWeInQueue = ThreadLocal.withInitial(() -> false);
    private SkriptAddon addon;
    private Types types;

    public static DataTask queue(DataTask task) {
        if (areWeInQueue.get()) {
            task.run();
        } else {
            return queue().queue(task);
        }
        return task;
    }

    public static @NotNull IOQueue queue() {
        return queue;
    }

    public static @NotNull IOQueue remoteQueue() {
        return remoteQueue;
    }

    /**
     * Returns a new File by its URI path.
     * @param path The file path
     * @return a new File object, or null if the path is null or empty
     */
    public static File file(URI path) {
        try {
            if (path == null || path.getPath().isEmpty()) {
                return null;
            } else {
                return new File(path.getPath());
            }
        } catch (IllegalArgumentException ex) {
            SkriptIO.error(ex);
            return null;
        }
    }

    /**
     * Returns a new File by its URI path without logging potential errors
     * @param path The file path
     * @return a new File object, or null if the path is null or empty
     */
    public static File fileNoError(URI path) {
        try {
            if (path == null || path.getPath().isEmpty()) {
                return null;
            } else {
                return new File(path.getPath());
            }
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Creates a new URI
     * @param path The path
     * @return a new URI object, or null if the URI is invalid
     */
    public static URI uri(String path) {
        try {
            return new URI(path);
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    /**
     * Logs an error message
     * @param message The message
     */
    public static void error(String message) {
        instance.getLogger().log(Level.SEVERE, message);
    }

    /**
     * Logs an exception
     * @param throwable The exception
     */
    public static void error(Throwable throwable) {
        throwSafe(throwable);
//        Bukkit.getLogger().log(Level.SEVERE, throwable.getMessage(), throwable); // todo ???
    }

    public static boolean isTestMode() {
        return testMode;
    }

    public static SkriptIO getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        queue.shutdown(1000);
        remoteQueue.shutdown(1000);
        addon = null;
        types = null;
    }

    @Override
    public void onEnable() {
        instance = this;
        PluginManager manager = getServer().getPluginManager();

        if (!Skript.getVersion().isLargerThan(MINIMUM_SUPPORTED_SK_VERSION)) {
            getLogger().severe("You are running an unsupported version of Skript." +
                    "Please update to at least Skript " + MINIMUM_SUPPORTED_SK_VERSION + " or else you might run into issues!");
            return;
        }

        types = new Types();
        //types.removeEffChange(); // why???????
        try {
            addon = Skript.registerAddon(this);
            addon.loadClasses("org.skriptlang.skript_io.elements");
            types.registerTypes();
            types.registerFileFormats();
            types.registerComparators();
            types.registerConverters();
            types.registerPathArithmetic();
            types.registerErrorTypes(addon);
            types.loadFormats(addon,
                    new GZipFormat(),
                    new YamlFormat(),
                    new JsonFormat(),
                    new PrettyJsonFormat(),
                    new URLEncodedFormat(),
                    new Base64EncodedFormat(),
                    new HTMLEncodedFormat()
            );

            Functions.registerFunctions();
        } catch (IOException e) {
            getLogger().severe("An error occurred while trying to enable this addon.");
            SkriptIO.error(e);
            manager.disablePlugin(this);
            return;
        } finally {
            //types.reAddEffChange();
        }
        queue = new IOQueue(50);
        remoteQueue = new IOQueue(100);
        DataTask markQueue = new DataTask() {
            @Override
            public void execute() {
                areWeInQueue.set(true);
            }
        }; // Tell things we're inside a queue right now
        queue.queue(markQueue);
        remoteQueue.queue(markQueue);
    }

    public static void throwSafe(Throwable throwable) {
        if (throwable == null) {
            throw new RuntimeException();
        }
        if (throwable instanceof IOError || throwable instanceof Exception) {
            throwUncheckedException(throwable);
        } else {
            throw new RuntimeException(throwable);
        }
    }

    @SuppressWarnings("unchecked")
    private static <Unknown extends Throwable> void throwUncheckedException(Throwable exception) throws Unknown {
        throw (Unknown) exception;
    }

}
