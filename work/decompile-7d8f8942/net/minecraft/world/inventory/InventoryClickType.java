package net.minecraft.world.inventory;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum InventoryClickType {

    PICKUP(0), QUICK_MOVE(1), SWAP(2), CLONE(3), THROW(4), QUICK_CRAFT(5), PICKUP_ALL(6);

    private static final IntFunction<InventoryClickType> BY_ID = ByIdMap.<InventoryClickType>continuous(InventoryClickType::id, values(), ByIdMap.a.ZERO);
    public static final StreamCodec<ByteBuf, InventoryClickType> STREAM_CODEC = ByteBufCodecs.idMapper(InventoryClickType.BY_ID, InventoryClickType::id);
    private final int id;

    private InventoryClickType(final int i) {
        this.id = i;
    }

    public int id() {
        return this.id;
    }
}
