package net.minecraft.network.chat.contents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.IChatFormatted;

public interface LiteralContents extends ComponentContents {

    MapCodec<LiteralContents> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.STRING.fieldOf("text").forGetter(LiteralContents::text)).apply(instance, LiteralContents::create);
    });
    LiteralContents EMPTY = new LiteralContents() {
        public String toString() {
            return "empty";
        }

        @Override
        public String text() {
            return "";
        }
    };

    static LiteralContents create(String s) {
        return (LiteralContents) (s.isEmpty() ? LiteralContents.EMPTY : new LiteralContents.a(s));
    }

    String text();

    @Override
    default MapCodec<LiteralContents> codec() {
        return LiteralContents.MAP_CODEC;
    }

    public static record a(String text) implements LiteralContents {

        @Override
        public <T> Optional<T> visit(IChatFormatted.a<T> ichatformatted_a) {
            return ichatformatted_a.accept(this.text);
        }

        @Override
        public <T> Optional<T> visit(IChatFormatted.b<T> ichatformatted_b, ChatModifier chatmodifier) {
            return ichatformatted_b.accept(chatmodifier, this.text);
        }

        public String toString() {
            return "literal{" + this.text + "}";
        }
    }
}
