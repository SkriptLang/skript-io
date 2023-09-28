package org.skriptlang.skript_io.elements.common.effects;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript_io.SkriptIO;
import org.skriptlang.skript_io.utility.Readable;
import org.skriptlang.skript_io.utility.Resource;
import org.skriptlang.skript_io.utility.Writable;

@Name("Contents of Resource")
@Description("""
    The contents of (the text inside) a resource, such as an open file.
    This will return nothing if the resource is unreadable.""")
@Examples({
    "open a website:",
    "\tbroadcast the content of the request's body",
    "open file ./test.txt:",
    "\tbroadcast the contents of file"
})
@Since("1.0.0")
public class ExprContentOfResource extends SimplePropertyExpression<Resource, String> {
    
    static {
        if (!SkriptIO.isTest())
            register(ExprContentOfResource.class, String.class, "[text] content[s]", "resource");
    }
    
    @Override
    protected @NotNull String getPropertyName() {
        return "content";
    }
    
    @Override
    public @Nullable String convert(Resource resource) {
        return resource instanceof Readable readable ? readable.readAll() : null;
    }
    
    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }
    
    @Override
    public Class<?> @Nullable [] acceptChange(Changer.@NotNull ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(String.class);
            case RESET, DELETE -> CollectionUtils.array();
            default -> null;
        };
    }
    
    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        if (delta == null || delta.length == 0 || delta[0] == null) return;
        if (mode == Changer.ChangeMode.SET) {
            if (!(delta[0] instanceof String content)) return;
            final Resource[] files = this.getExpr().getArray(event);
            for (final Resource file : files)
                if (file instanceof Writable writable) writable.write(content);
        } else {
            final Resource[] files = this.getExpr().getArray(event);
            for (final Resource file : files)
                if (file instanceof Writable writable) writable.clear();
        }
    }
    
    @Override
    public boolean isSingle() {
        return true;
    }
    
}
