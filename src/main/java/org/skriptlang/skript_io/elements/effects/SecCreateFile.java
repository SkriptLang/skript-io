package org.skriptlang.skript_io.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.FileController;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.skriptlang.skript_io.utility.FileController.WRITE;

@Name("Create File")
@Description("Creates a new file at a path. If the file already exists or was successfully created, opens an editing section.")
@Examples({
    "command /guiviewers: # Returns a list of all players with a GUI open.",
    "\tset {_viewers::*} to all players where [input has a gui]",
    "\tsend \"GUI Viewers: %{_viewers::*}%\" to player"
})
@Since("1.0.0")
public class SecCreateFile extends SecAccessFile {
    
    static {
        Skript.registerSection(SecCreateFile.class,
            "(create|make) [a] [new] file [at] %path%"
        );
    }
    
    private Expression<URI> pathExpression;
    
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult result, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> list) {
        this.pathExpression = (Expression<URI>) expressions[0];
        if (this.hasSection()) {
            assert sectionNode != null;
            this.loadOptionalCode(sectionNode);
            if (last != null) last.setNext(null);
        }
        return true;
    }
    
    @Override
    protected @Nullable TriggerItem walk(Event event) {
        final URI uri = pathExpression.getSingle(event);
        if (uri == null) return this.walk(event, false);
        final File file = SkriptIO.file(uri);
        if (file == null) return this.walk(event, false);
        if (file.exists() && !file.isFile()) return this.walk(event, false);
        try {
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        if (!file.exists() || !file.isFile()) return this.walk(event, false);
        TriggerItem walk = null;
        try (final FileController controller = FileController.getController(file, WRITE)) {
            FileController.push(event, controller);
            walk = this.walk(event, true);
        } catch (IOException ex) {
            SkriptIO.error(ex);
        } finally {
            FileController.pop(event);
            if (walk == null) walk = this.walk(event, false);
        }
        return walk;
    }
    
    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "create file " + pathExpression.toString(event, debug);
    }
    
}
