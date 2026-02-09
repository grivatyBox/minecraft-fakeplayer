package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.CollectionPredicate;
import net.minecraft.advancements.critereon.CriterionConditionValue;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WrittenBookContent;

public record WrittenBookPredicate(Optional<CollectionPredicate<Filterable<IChatBaseComponent>, WrittenBookPredicate.a>> pages, Optional<String> author, Optional<String> title, CriterionConditionValue.IntegerRange generation, Optional<Boolean> resolved) implements SingleComponentItemPredicate<WrittenBookContent> {

    public static final Codec<WrittenBookPredicate> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CollectionPredicate.codec(WrittenBookPredicate.a.CODEC).optionalFieldOf("pages").forGetter(WrittenBookPredicate::pages), Codec.STRING.optionalFieldOf("author").forGetter(WrittenBookPredicate::author), Codec.STRING.optionalFieldOf("title").forGetter(WrittenBookPredicate::title), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("generation", CriterionConditionValue.IntegerRange.ANY).forGetter(WrittenBookPredicate::generation), Codec.BOOL.optionalFieldOf("resolved").forGetter(WrittenBookPredicate::resolved)).apply(instance, WrittenBookPredicate::new);
    });

    @Override
    public DataComponentType<WrittenBookContent> componentType() {
        return DataComponents.WRITTEN_BOOK_CONTENT;
    }

    public boolean matches(WrittenBookContent writtenbookcontent) {
        return this.author.isPresent() && !((String) this.author.get()).equals(writtenbookcontent.author()) ? false : (this.title.isPresent() && !((String) this.title.get()).equals(writtenbookcontent.title().raw()) ? false : (!this.generation.matches(writtenbookcontent.generation()) ? false : (this.resolved.isPresent() && (Boolean) this.resolved.get() != writtenbookcontent.resolved() ? false : !this.pages.isPresent() || ((CollectionPredicate) this.pages.get()).test(writtenbookcontent.pages()))));
    }

    public static record a(IChatBaseComponent contents) implements Predicate<Filterable<IChatBaseComponent>> {

        public static final Codec<WrittenBookPredicate.a> CODEC = ComponentSerialization.CODEC.xmap(WrittenBookPredicate.a::new, WrittenBookPredicate.a::contents);

        public boolean test(Filterable<IChatBaseComponent> filterable) {
            return ((IChatBaseComponent) filterable.raw()).equals(this.contents);
        }
    }
}
