package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.CollectionPredicate;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WritableBookContent;

public record WritableBookPredicate(Optional<CollectionPredicate<Filterable<String>, WritableBookPredicate.a>> pages) implements SingleComponentItemPredicate<WritableBookContent> {

    public static final Codec<WritableBookPredicate> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CollectionPredicate.codec(WritableBookPredicate.a.CODEC).optionalFieldOf("pages").forGetter(WritableBookPredicate::pages)).apply(instance, WritableBookPredicate::new);
    });

    @Override
    public DataComponentType<WritableBookContent> componentType() {
        return DataComponents.WRITABLE_BOOK_CONTENT;
    }

    public boolean matches(WritableBookContent writablebookcontent) {
        return !this.pages.isPresent() || ((CollectionPredicate) this.pages.get()).test(writablebookcontent.pages());
    }

    public static record a(String contents) implements Predicate<Filterable<String>> {

        public static final Codec<WritableBookPredicate.a> CODEC = Codec.STRING.xmap(WritableBookPredicate.a::new, WritableBookPredicate.a::contents);

        public boolean test(Filterable<String> filterable) {
            return ((String) filterable.raw()).equals(this.contents);
        }
    }
}
