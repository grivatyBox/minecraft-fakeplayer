package net.minecraft.network.chat.contents;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.IChatFormatted;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.network.chat.contents.objects.ObjectInfos;

public record ObjectContents(ObjectInfo contents) implements ComponentContents {

    private static final String PLACEHOLDER = Character.toString('\ufffc');
    public static final MapCodec<ObjectContents> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ObjectInfos.CODEC.forGetter(ObjectContents::contents)).apply(instance, ObjectContents::new);
    });

    @Override
    public MapCodec<ObjectContents> codec() {
        return ObjectContents.MAP_CODEC;
    }

    @Override
    public <T> Optional<T> visit(IChatFormatted.a<T> ichatformatted_a) {
        return ichatformatted_a.accept(this.contents.description());
    }

    @Override
    public <T> Optional<T> visit(IChatFormatted.b<T> ichatformatted_b, ChatModifier chatmodifier) {
        return ichatformatted_b.accept(chatmodifier.withFont(this.contents.fontDescription()), ObjectContents.PLACEHOLDER);
    }
}
