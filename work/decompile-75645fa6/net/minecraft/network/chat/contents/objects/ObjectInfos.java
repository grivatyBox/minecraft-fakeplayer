package net.minecraft.network.chat.contents.objects;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;

public class ObjectInfos {

    private static final ExtraCodecs.b<String, MapCodec<? extends ObjectInfo>> ID_MAPPER = new ExtraCodecs.b<String, MapCodec<? extends ObjectInfo>>();
    public static final MapCodec<ObjectInfo> CODEC = ComponentSerialization.<ObjectInfo>createLegacyComponentMatcher(ObjectInfos.ID_MAPPER, ObjectInfo::codec, "object");

    public ObjectInfos() {}

    static {
        ObjectInfos.ID_MAPPER.put("atlas", AtlasSprite.MAP_CODEC);
        ObjectInfos.ID_MAPPER.put("player", PlayerSprite.MAP_CODEC);
    }
}
