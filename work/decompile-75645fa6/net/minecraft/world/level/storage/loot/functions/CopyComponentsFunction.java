package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyComponentsFunction extends LootItemFunctionConditional {

    private static final ExtraCodecs.b<String, CopyComponentsFunction.e<?>> SOURCES = new ExtraCodecs.b<String, CopyComponentsFunction.e<?>>();
    public static final MapCodec<CopyComponentsFunction> CODEC;
    private final CopyComponentsFunction.e<?> source;
    private final Optional<List<DataComponentType<?>>> include;
    private final Optional<List<DataComponentType<?>>> exclude;
    private final Predicate<DataComponentType<?>> bakedPredicate;

    CopyComponentsFunction(List<LootItemCondition> list, CopyComponentsFunction.e<?> copycomponentsfunction_e, Optional<List<DataComponentType<?>>> optional, Optional<List<DataComponentType<?>>> optional1) {
        super(list);
        this.source = copycomponentsfunction_e;
        this.include = optional.map(List::copyOf);
        this.exclude = optional1.map(List::copyOf);
        List<Predicate<DataComponentType<?>>> list1 = new ArrayList(2);

        optional1.ifPresent((list2) -> {
            list1.add((Predicate) (datacomponenttype) -> {
                return !list2.contains(datacomponenttype);
            });
        });
        optional.ifPresent((list2) -> {
            Objects.requireNonNull(list2);
            list1.add(list2::contains);
        });
        this.bakedPredicate = SystemUtils.allOf(list1);
    }

    @Override
    public LootItemFunctionType<CopyComponentsFunction> getType() {
        return LootItemFunctions.COPY_COMPONENTS;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(this.source.contextParam());
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        DataComponentGetter datacomponentgetter = this.source.get(loottableinfo);

        if (datacomponentgetter != null) {
            if (datacomponentgetter instanceof DataComponentMap) {
                DataComponentMap datacomponentmap = (DataComponentMap) datacomponentgetter;

                itemstack.applyComponents(datacomponentmap.filter(this.bakedPredicate));
            } else {
                Collection<DataComponentType<?>> collection = (Collection) this.exclude.orElse(List.of());

                ((Stream) this.include.map(Collection::stream).orElse(BuiltInRegistries.DATA_COMPONENT_TYPE.listElements().map(Holder::value))).forEach((datacomponenttype) -> {
                    if (!collection.contains(datacomponenttype)) {
                        TypedDataComponent<?> typeddatacomponent = datacomponentgetter.getTyped(datacomponenttype);

                        if (typeddatacomponent != null) {
                            itemstack.set(typeddatacomponent);
                        }

                    }
                });
            }
        }

        return itemstack;
    }

    public static CopyComponentsFunction.b copyComponentsFromEntity(ContextKey<? extends Entity> contextkey) {
        return new CopyComponentsFunction.b(new CopyComponentsFunction.c(contextkey));
    }

    public static CopyComponentsFunction.b copyComponentsFromBlockEntity(ContextKey<? extends TileEntity> contextkey) {
        return new CopyComponentsFunction.b(new CopyComponentsFunction.a(contextkey));
    }

    static {
        for (LootTableInfo.EntityTarget loottableinfo_entitytarget : LootTableInfo.EntityTarget.values()) {
            CopyComponentsFunction.SOURCES.put(loottableinfo_entitytarget.getSerializedName(), new CopyComponentsFunction.c(loottableinfo_entitytarget.getParam()));
        }

        for (LootTableInfo.a loottableinfo_a : LootTableInfo.a.values()) {
            CopyComponentsFunction.SOURCES.put(loottableinfo_a.getSerializedName(), new CopyComponentsFunction.a(loottableinfo_a.getParam()));
        }

        for (LootTableInfo.d loottableinfo_d : LootTableInfo.d.values()) {
            CopyComponentsFunction.SOURCES.put(loottableinfo_d.getSerializedName(), new CopyComponentsFunction.d(loottableinfo_d.getParam()));
        }

        CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return commonFields(instance).and(instance.group(CopyComponentsFunction.SOURCES.codec(Codec.STRING).fieldOf("source").forGetter((copycomponentsfunction) -> {
                return copycomponentsfunction.source;
            }), DataComponentType.CODEC.listOf().optionalFieldOf("include").forGetter((copycomponentsfunction) -> {
                return copycomponentsfunction.include;
            }), DataComponentType.CODEC.listOf().optionalFieldOf("exclude").forGetter((copycomponentsfunction) -> {
                return copycomponentsfunction.exclude;
            }))).apply(instance, CopyComponentsFunction::new);
        });
    }

    public static class b extends LootItemFunctionConditional.a<CopyComponentsFunction.b> {

        private final CopyComponentsFunction.e<?> source;
        private Optional<ImmutableList.Builder<DataComponentType<?>>> include = Optional.empty();
        private Optional<ImmutableList.Builder<DataComponentType<?>>> exclude = Optional.empty();

        b(CopyComponentsFunction.e<?> copycomponentsfunction_e) {
            this.source = copycomponentsfunction_e;
        }

        public CopyComponentsFunction.b include(DataComponentType<?> datacomponenttype) {
            if (this.include.isEmpty()) {
                this.include = Optional.of(ImmutableList.builder());
            }

            ((Builder) this.include.get()).add(datacomponenttype);
            return this;
        }

        public CopyComponentsFunction.b exclude(DataComponentType<?> datacomponenttype) {
            if (this.exclude.isEmpty()) {
                this.exclude = Optional.of(ImmutableList.builder());
            }

            ((Builder) this.exclude.get()).add(datacomponenttype);
            return this;
        }

        @Override
        protected CopyComponentsFunction.b getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyComponentsFunction(this.getConditions(), this.source, this.include.map(Builder::build), this.exclude.map(Builder::build));
        }
    }

    public interface e<T> {

        ContextKey<? extends T> contextParam();

        DataComponentGetter get(T t0);

        @Nullable
        default DataComponentGetter get(LootTableInfo loottableinfo) {
            T t0 = (T) loottableinfo.getOptionalParameter(this.contextParam());

            return t0 != null ? this.get(t0) : null;
        }
    }

    private static record a(ContextKey<? extends TileEntity> contextParam) implements CopyComponentsFunction.e<TileEntity> {

        public DataComponentGetter get(TileEntity tileentity) {
            return tileentity.collectComponents();
        }
    }

    private static record c(ContextKey<? extends Entity> contextParam) implements CopyComponentsFunction.e<Entity> {

        public DataComponentGetter get(Entity entity) {
            return entity;
        }
    }

    private static record d(ContextKey<? extends ItemStack> contextParam) implements CopyComponentsFunction.e<ItemStack> {

        public DataComponentGetter get(ItemStack itemstack) {
            return itemstack.getComponents();
        }
    }
}
