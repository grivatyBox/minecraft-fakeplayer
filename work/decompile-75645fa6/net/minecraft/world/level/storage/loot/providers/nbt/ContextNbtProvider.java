package net.minecraft.world.level.storage.loot.providers.nbt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.CriterionConditionNBT;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.storage.loot.LootTableInfo;

public class ContextNbtProvider implements NbtProvider {

    private static final ExtraCodecs.b<String, ContextNbtProvider.c<?>> SOURCES = new ExtraCodecs.b<String, ContextNbtProvider.c<?>>();
    private static final Codec<ContextNbtProvider.c<?>> GETTER_CODEC;
    public static final MapCodec<ContextNbtProvider> MAP_CODEC;
    public static final Codec<ContextNbtProvider> INLINE_CODEC;
    private final ContextNbtProvider.c<?> source;

    private ContextNbtProvider(ContextNbtProvider.c<?> contextnbtprovider_c) {
        this.source = contextnbtprovider_c;
    }

    @Override
    public LootNbtProviderType getType() {
        return NbtProviders.CONTEXT;
    }

    @Nullable
    @Override
    public NBTBase get(LootTableInfo loottableinfo) {
        return this.source.get(loottableinfo);
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(this.source.contextParam());
    }

    public static NbtProvider forContextEntity(LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        return new ContextNbtProvider(new ContextNbtProvider.b(loottableinfo_entitytarget.getParam()));
    }

    static {
        for (LootTableInfo.EntityTarget loottableinfo_entitytarget : LootTableInfo.EntityTarget.values()) {
            ContextNbtProvider.SOURCES.put(loottableinfo_entitytarget.getSerializedName(), new ContextNbtProvider.b(loottableinfo_entitytarget.getParam()));
        }

        for (LootTableInfo.a loottableinfo_a : LootTableInfo.a.values()) {
            ContextNbtProvider.SOURCES.put(loottableinfo_a.getSerializedName(), new ContextNbtProvider.a(loottableinfo_a.getParam()));
        }

        GETTER_CODEC = ContextNbtProvider.SOURCES.codec(Codec.STRING);
        MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ContextNbtProvider.GETTER_CODEC.fieldOf("target").forGetter((contextnbtprovider) -> {
                return contextnbtprovider.source;
            })).apply(instance, ContextNbtProvider::new);
        });
        INLINE_CODEC = ContextNbtProvider.GETTER_CODEC.xmap(ContextNbtProvider::new, (contextnbtprovider) -> {
            return contextnbtprovider.source;
        });
    }

    private interface c<T> {

        ContextKey<? extends T> contextParam();

        @Nullable
        NBTBase get(T t0);

        @Nullable
        default NBTBase get(LootTableInfo loottableinfo) {
            T t0 = (T) loottableinfo.getOptionalParameter(this.contextParam());

            return t0 != null ? this.get(t0) : null;
        }
    }

    private static record a(ContextKey<? extends TileEntity> contextParam) implements ContextNbtProvider.c<TileEntity> {

        public NBTBase get(TileEntity tileentity) {
            return tileentity.saveWithFullMetadata((HolderLookup.a) tileentity.getLevel().registryAccess());
        }
    }

    private static record b(ContextKey<? extends Entity> contextParam) implements ContextNbtProvider.c<Entity> {

        public NBTBase get(Entity entity) {
            return CriterionConditionNBT.getEntityTagToCompare(entity);
        }
    }
}
