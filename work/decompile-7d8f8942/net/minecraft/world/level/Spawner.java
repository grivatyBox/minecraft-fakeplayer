package net.minecraft.world.level;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.component.CustomData;

public interface Spawner {

    void setEntityId(EntityTypes<?> entitytypes, RandomSource randomsource);

    static void appendHoverText(CustomData customdata, Consumer<IChatBaseComponent> consumer, String s) {
        IChatBaseComponent ichatbasecomponent = getSpawnEntityDisplayName(customdata, s);

        if (ichatbasecomponent != null) {
            consumer.accept(ichatbasecomponent);
        } else {
            consumer.accept(CommonComponents.EMPTY);
            consumer.accept(IChatBaseComponent.translatable("block.minecraft.spawner.desc1").withStyle(EnumChatFormat.GRAY));
            consumer.accept(CommonComponents.space().append((IChatBaseComponent) IChatBaseComponent.translatable("block.minecraft.spawner.desc2").withStyle(EnumChatFormat.BLUE)));
        }

    }

    @Nullable
    static IChatBaseComponent getSpawnEntityDisplayName(CustomData customdata, String s) {
        return (IChatBaseComponent) customdata.getUnsafe().getCompound(s).flatMap((nbttagcompound) -> {
            return nbttagcompound.getCompound("entity");
        }).flatMap((nbttagcompound) -> {
            return nbttagcompound.read("id", EntityTypes.CODEC);
        }).map((entitytypes) -> {
            return IChatBaseComponent.translatable(entitytypes.getDescriptionId()).withStyle(EnumChatFormat.GRAY);
        }).orElse((Object) null);
    }
}
