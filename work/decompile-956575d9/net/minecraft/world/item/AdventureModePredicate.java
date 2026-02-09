package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.advancements.critereon.CriterionConditionBlock;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock;

public class AdventureModePredicate {

    public static final Codec<AdventureModePredicate> CODEC = ExtraCodecs.compactListCodec(CriterionConditionBlock.CODEC, ExtraCodecs.nonEmptyList(CriterionConditionBlock.CODEC.listOf())).xmap(AdventureModePredicate::new, (adventuremodepredicate) -> {
        return adventuremodepredicate.predicates;
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, AdventureModePredicate> STREAM_CODEC = StreamCodec.composite(CriterionConditionBlock.STREAM_CODEC.apply(ByteBufCodecs.list()), (adventuremodepredicate) -> {
        return adventuremodepredicate.predicates;
    }, AdventureModePredicate::new);
    public static final IChatBaseComponent CAN_BREAK_HEADER = IChatBaseComponent.translatable("item.canBreak").withStyle(EnumChatFormat.GRAY);
    public static final IChatBaseComponent CAN_PLACE_HEADER = IChatBaseComponent.translatable("item.canPlace").withStyle(EnumChatFormat.GRAY);
    private static final IChatBaseComponent UNKNOWN_USE = IChatBaseComponent.translatable("item.canUse.unknown").withStyle(EnumChatFormat.GRAY);
    private final List<CriterionConditionBlock> predicates;
    @Nullable
    private List<IChatBaseComponent> cachedTooltip;
    @Nullable
    private ShapeDetectorBlock lastCheckedBlock;
    private boolean lastResult;
    private boolean checksBlockEntity;

    public AdventureModePredicate(List<CriterionConditionBlock> list) {
        this.predicates = list;
    }

    private static boolean areSameBlocks(ShapeDetectorBlock shapedetectorblock, @Nullable ShapeDetectorBlock shapedetectorblock1, boolean flag) {
        if (shapedetectorblock1 != null && shapedetectorblock.getState() == shapedetectorblock1.getState()) {
            if (!flag) {
                return true;
            } else if (shapedetectorblock.getEntity() == null && shapedetectorblock1.getEntity() == null) {
                return true;
            } else if (shapedetectorblock.getEntity() != null && shapedetectorblock1.getEntity() != null) {
                IRegistryCustom iregistrycustom = shapedetectorblock.getLevel().registryAccess();

                return Objects.equals(shapedetectorblock.getEntity().saveWithId(iregistrycustom), shapedetectorblock1.getEntity().saveWithId(iregistrycustom));
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean test(ShapeDetectorBlock shapedetectorblock) {
        if (areSameBlocks(shapedetectorblock, this.lastCheckedBlock, this.checksBlockEntity)) {
            return this.lastResult;
        } else {
            this.lastCheckedBlock = shapedetectorblock;
            this.checksBlockEntity = false;

            for (CriterionConditionBlock criterionconditionblock : this.predicates) {
                if (criterionconditionblock.matches(shapedetectorblock)) {
                    this.checksBlockEntity |= criterionconditionblock.requiresNbt();
                    this.lastResult = true;
                    return true;
                }
            }

            this.lastResult = false;
            return false;
        }
    }

    private List<IChatBaseComponent> tooltip() {
        if (this.cachedTooltip == null) {
            this.cachedTooltip = computeTooltip(this.predicates);
        }

        return this.cachedTooltip;
    }

    public void addToTooltip(Consumer<IChatBaseComponent> consumer) {
        this.tooltip().forEach(consumer);
    }

    private static List<IChatBaseComponent> computeTooltip(List<CriterionConditionBlock> list) {
        for (CriterionConditionBlock criterionconditionblock : list) {
            if (criterionconditionblock.blocks().isEmpty()) {
                return List.of(AdventureModePredicate.UNKNOWN_USE);
            }
        }

        return list.stream().flatMap((criterionconditionblock1) -> {
            return ((HolderSet) criterionconditionblock1.blocks().orElseThrow()).stream();
        }).distinct().map((holder) -> {
            return ((Block) holder.value()).getName().withStyle(EnumChatFormat.DARK_GRAY);
        }).toList();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object instanceof AdventureModePredicate) {
            AdventureModePredicate adventuremodepredicate = (AdventureModePredicate) object;

            return this.predicates.equals(adventuremodepredicate.predicates);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.predicates.hashCode();
    }

    public String toString() {
        return "AdventureModePredicate{predicates=" + String.valueOf(this.predicates) + "}";
    }
}
