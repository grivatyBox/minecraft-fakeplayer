package net.minecraft.world.level.block.state.properties;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.INamable;

public enum BlockPropertyStructureMode implements INamable {

    SAVE("save"), LOAD("load"), CORNER("corner"), DATA("data");

    /** @deprecated */
    @Deprecated
    public static final Codec<BlockPropertyStructureMode> LEGACY_CODEC = ExtraCodecs.<BlockPropertyStructureMode>legacyEnum(BlockPropertyStructureMode::valueOf);
    private final String name;
    private final IChatBaseComponent displayName;

    private BlockPropertyStructureMode(final String s) {
        this.name = s;
        this.displayName = IChatBaseComponent.translatable("structure_block.mode_info." + s);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public IChatBaseComponent getDisplayName() {
        return this.displayName;
    }
}
