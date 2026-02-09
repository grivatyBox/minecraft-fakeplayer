package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;

public class CriterionTriggerEnterBlock extends CriterionTriggerAbstract<CriterionTriggerEnterBlock.a> {

    public CriterionTriggerEnterBlock() {}

    @Override
    public Codec<CriterionTriggerEnterBlock.a> codec() {
        return CriterionTriggerEnterBlock.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, IBlockData iblockdata) {
        this.trigger(entityplayer, (criteriontriggerenterblock_a) -> {
            return criteriontriggerenterblock_a.matches(iblockdata);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<CriterionTriggerProperties> state) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerEnterBlock.a> CODEC = ExtraCodecs.validate(RecordCodecBuilder.create((instance) -> {
            return instance.group(ExtraCodecs.strictOptionalField(CriterionConditionEntity.ADVANCEMENT_CODEC, "player").forGetter(CriterionTriggerEnterBlock.a::player), ExtraCodecs.strictOptionalField(BuiltInRegistries.BLOCK.holderByNameCodec(), "block").forGetter(CriterionTriggerEnterBlock.a::block), ExtraCodecs.strictOptionalField(CriterionTriggerProperties.CODEC, "state").forGetter(CriterionTriggerEnterBlock.a::state)).apply(instance, CriterionTriggerEnterBlock.a::new);
        }), CriterionTriggerEnterBlock.a::validate);

        private static DataResult<CriterionTriggerEnterBlock.a> validate(CriterionTriggerEnterBlock.a criteriontriggerenterblock_a) {
            return (DataResult) criteriontriggerenterblock_a.block.flatMap((holder) -> {
                return criteriontriggerenterblock_a.state.flatMap((criteriontriggerproperties) -> {
                    return criteriontriggerproperties.checkState(((Block) holder.value()).getStateDefinition());
                }).map((s) -> {
                    return DataResult.error(() -> {
                        return "Block" + holder + " has no property " + s;
                    });
                });
            }).orElseGet(() -> {
                return DataResult.success(criteriontriggerenterblock_a);
            });
        }

        public static Criterion<CriterionTriggerEnterBlock.a> entersBlock(Block block) {
            return CriterionTriggers.ENTER_BLOCK.createCriterion(new CriterionTriggerEnterBlock.a(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.empty()));
        }

        public boolean matches(IBlockData iblockdata) {
            return this.block.isPresent() && !iblockdata.is((Holder) this.block.get()) ? false : !this.state.isPresent() || ((CriterionTriggerProperties) this.state.get()).matches(iblockdata);
        }
    }
}
