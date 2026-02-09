package net.minecraft.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;

public record WorldMapFrame(BlockPosition pos, int rotation, int entityId) {

    public static final Codec<WorldMapFrame> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(BlockPosition.CODEC.fieldOf("pos").forGetter(WorldMapFrame::pos), Codec.INT.fieldOf("rotation").forGetter(WorldMapFrame::rotation), Codec.INT.fieldOf("entity_id").forGetter(WorldMapFrame::entityId)).apply(instance, WorldMapFrame::new);
    });

    public String getId() {
        return frameId(this.pos);
    }

    public static String frameId(BlockPosition blockposition) {
        int i = blockposition.getX();

        return "frame-" + i + "," + blockposition.getY() + "," + blockposition.getZ();
    }
}
