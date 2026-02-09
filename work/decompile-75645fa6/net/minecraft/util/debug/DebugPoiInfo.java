package net.minecraft.util.debug;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.village.poi.VillagePlaceRecord;
import net.minecraft.world.entity.ai.village.poi.VillagePlaceType;

public record DebugPoiInfo(BlockPosition pos, Holder<VillagePlaceType> poiType, int freeTicketCount) {

    public static final StreamCodec<RegistryFriendlyByteBuf, DebugPoiInfo> STREAM_CODEC = StreamCodec.composite(BlockPosition.STREAM_CODEC, DebugPoiInfo::pos, ByteBufCodecs.holderRegistry(Registries.POINT_OF_INTEREST_TYPE), DebugPoiInfo::poiType, ByteBufCodecs.VAR_INT, DebugPoiInfo::freeTicketCount, DebugPoiInfo::new);

    public DebugPoiInfo(VillagePlaceRecord villageplacerecord) {
        this(villageplacerecord.getPos(), villageplacerecord.getPoiType(), villageplacerecord.getFreeTickets());
    }
}
