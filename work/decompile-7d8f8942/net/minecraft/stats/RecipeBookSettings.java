package net.minecraft.stats;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.UnaryOperator;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.RecipeBookType;

public final class RecipeBookSettings {

    public static final StreamCodec<PacketDataSerializer, RecipeBookSettings> STREAM_CODEC = StreamCodec.composite(RecipeBookSettings.a.STREAM_CODEC, (recipebooksettings) -> {
        return recipebooksettings.crafting;
    }, RecipeBookSettings.a.STREAM_CODEC, (recipebooksettings) -> {
        return recipebooksettings.furnace;
    }, RecipeBookSettings.a.STREAM_CODEC, (recipebooksettings) -> {
        return recipebooksettings.blastFurnace;
    }, RecipeBookSettings.a.STREAM_CODEC, (recipebooksettings) -> {
        return recipebooksettings.smoker;
    }, RecipeBookSettings::new);
    public static final MapCodec<RecipeBookSettings> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(RecipeBookSettings.a.CRAFTING_MAP_CODEC.forGetter((recipebooksettings) -> {
            return recipebooksettings.crafting;
        }), RecipeBookSettings.a.FURNACE_MAP_CODEC.forGetter((recipebooksettings) -> {
            return recipebooksettings.furnace;
        }), RecipeBookSettings.a.BLAST_FURNACE_MAP_CODEC.forGetter((recipebooksettings) -> {
            return recipebooksettings.blastFurnace;
        }), RecipeBookSettings.a.SMOKER_MAP_CODEC.forGetter((recipebooksettings) -> {
            return recipebooksettings.smoker;
        })).apply(instance, RecipeBookSettings::new);
    });
    private RecipeBookSettings.a crafting;
    private RecipeBookSettings.a furnace;
    private RecipeBookSettings.a blastFurnace;
    private RecipeBookSettings.a smoker;

    public RecipeBookSettings() {
        this(RecipeBookSettings.a.DEFAULT, RecipeBookSettings.a.DEFAULT, RecipeBookSettings.a.DEFAULT, RecipeBookSettings.a.DEFAULT);
    }

    private RecipeBookSettings(RecipeBookSettings.a recipebooksettings_a, RecipeBookSettings.a recipebooksettings_a1, RecipeBookSettings.a recipebooksettings_a2, RecipeBookSettings.a recipebooksettings_a3) {
        this.crafting = recipebooksettings_a;
        this.furnace = recipebooksettings_a1;
        this.blastFurnace = recipebooksettings_a2;
        this.smoker = recipebooksettings_a3;
    }

    @VisibleForTesting
    public RecipeBookSettings.a getSettings(RecipeBookType recipebooktype) {
        RecipeBookSettings.a recipebooksettings_a;

        switch (recipebooktype) {
            case CRAFTING:
                recipebooksettings_a = this.crafting;
                break;
            case FURNACE:
                recipebooksettings_a = this.furnace;
                break;
            case BLAST_FURNACE:
                recipebooksettings_a = this.blastFurnace;
                break;
            case SMOKER:
                recipebooksettings_a = this.smoker;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return recipebooksettings_a;
    }

    private void updateSettings(RecipeBookType recipebooktype, UnaryOperator<RecipeBookSettings.a> unaryoperator) {
        switch (recipebooktype) {
            case CRAFTING:
                this.crafting = (RecipeBookSettings.a) unaryoperator.apply(this.crafting);
                break;
            case FURNACE:
                this.furnace = (RecipeBookSettings.a) unaryoperator.apply(this.furnace);
                break;
            case BLAST_FURNACE:
                this.blastFurnace = (RecipeBookSettings.a) unaryoperator.apply(this.blastFurnace);
                break;
            case SMOKER:
                this.smoker = (RecipeBookSettings.a) unaryoperator.apply(this.smoker);
        }

    }

    public boolean isOpen(RecipeBookType recipebooktype) {
        return this.getSettings(recipebooktype).open;
    }

    public void setOpen(RecipeBookType recipebooktype, boolean flag) {
        this.updateSettings(recipebooktype, (recipebooksettings_a) -> {
            return recipebooksettings_a.setOpen(flag);
        });
    }

    public boolean isFiltering(RecipeBookType recipebooktype) {
        return this.getSettings(recipebooktype).filtering;
    }

    public void setFiltering(RecipeBookType recipebooktype, boolean flag) {
        this.updateSettings(recipebooktype, (recipebooksettings_a) -> {
            return recipebooksettings_a.setFiltering(flag);
        });
    }

    public RecipeBookSettings copy() {
        return new RecipeBookSettings(this.crafting, this.furnace, this.blastFurnace, this.smoker);
    }

    public void replaceFrom(RecipeBookSettings recipebooksettings) {
        this.crafting = recipebooksettings.crafting;
        this.furnace = recipebooksettings.furnace;
        this.blastFurnace = recipebooksettings.blastFurnace;
        this.smoker = recipebooksettings.smoker;
    }

    public static record a(boolean open, boolean filtering) {

        public static final RecipeBookSettings.a DEFAULT = new RecipeBookSettings.a(false, false);
        public static final MapCodec<RecipeBookSettings.a> CRAFTING_MAP_CODEC = codec("isGuiOpen", "isFilteringCraftable");
        public static final MapCodec<RecipeBookSettings.a> FURNACE_MAP_CODEC = codec("isFurnaceGuiOpen", "isFurnaceFilteringCraftable");
        public static final MapCodec<RecipeBookSettings.a> BLAST_FURNACE_MAP_CODEC = codec("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable");
        public static final MapCodec<RecipeBookSettings.a> SMOKER_MAP_CODEC = codec("isSmokerGuiOpen", "isSmokerFilteringCraftable");
        public static final StreamCodec<ByteBuf, RecipeBookSettings.a> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, RecipeBookSettings.a::open, ByteBufCodecs.BOOL, RecipeBookSettings.a::filtering, RecipeBookSettings.a::new);

        public String toString() {
            return "[open=" + this.open + ", filtering=" + this.filtering + "]";
        }

        public RecipeBookSettings.a setOpen(boolean flag) {
            return new RecipeBookSettings.a(flag, this.filtering);
        }

        public RecipeBookSettings.a setFiltering(boolean flag) {
            return new RecipeBookSettings.a(this.open, flag);
        }

        private static MapCodec<RecipeBookSettings.a> codec(String s, String s1) {
            return RecordCodecBuilder.mapCodec((instance) -> {
                return instance.group(Codec.BOOL.optionalFieldOf(s, false).forGetter(RecipeBookSettings.a::open), Codec.BOOL.optionalFieldOf(s1, false).forGetter(RecipeBookSettings.a::filtering)).apply(instance, RecipeBookSettings.a::new);
            });
        }
    }
}
