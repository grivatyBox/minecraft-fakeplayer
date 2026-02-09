package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootTable;

public class AppendLoot implements RuleBlockEntityModifier {

    public static final MapCodec<AppendLoot> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(LootTable.KEY_CODEC.fieldOf("loot_table").forGetter((appendloot) -> {
            return appendloot.lootTable;
        })).apply(instance, AppendLoot::new);
    });
    private final ResourceKey<LootTable> lootTable;

    public AppendLoot(ResourceKey<LootTable> resourcekey) {
        this.lootTable = resourcekey;
    }

    @Override
    public NBTTagCompound apply(RandomSource randomsource, @Nullable NBTTagCompound nbttagcompound) {
        NBTTagCompound nbttagcompound1 = nbttagcompound == null ? new NBTTagCompound() : nbttagcompound.copy();

        nbttagcompound1.store("LootTable", LootTable.KEY_CODEC, this.lootTable);
        nbttagcompound1.putLong("LootTableSeed", randomsource.nextLong());
        return nbttagcompound1;
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.APPEND_LOOT;
    }
}
