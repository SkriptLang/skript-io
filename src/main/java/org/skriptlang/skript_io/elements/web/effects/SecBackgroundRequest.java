package org.skriptlang.skript_io.elements.web.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;

public abstract class SecBackgroundRequest extends EffectSection {
    
    protected boolean loadDelayed(@Nullable SectionNode sectionNode, SkriptParser.ParseResult result) {
        if (this.hasSection()) {
            assert sectionNode != null;
            final Kleenean hadDelayBefore = this.getParser().getHasDelayBefore();
            this.getParser().setHasDelayBefore(Kleenean.FALSE);
            this.loadOptionalCode(sectionNode);
            if (last != null) last.setNext(null);
            if (!this.getParser().getHasDelayBefore().isFalse()) {
                Skript.error("Delays can't be used within " + result.expr);
                return false;
            }
            this.getParser().setHasDelayBefore(hadDelayBefore.or(this.getParser().getHasDelayBefore()));
        }
        return true;
    }
    
}
