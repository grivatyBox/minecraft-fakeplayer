package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.SystemUtils;

public class FilteredBooksFix extends ItemStackTagFix {

    public FilteredBooksFix(Schema schema) {
        super(schema, "Remove filtered text from books", (s) -> {
            return s.equals("minecraft:writable_book") || s.equals("minecraft:written_book");
        });
    }

    @Override
    protected Typed<?> fixItemStackTag(Typed<?> typed) {
        return SystemUtils.writeAndReadTypedOrThrow(typed, typed.getType(), (dynamic) -> {
            return dynamic.remove("filtered_title").remove("filtered_pages");
        });
    }
}
