package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.GeneratorAccessSeed;

class AnyOfPredicate extends CombiningPredicate {

    public static final MapCodec<AnyOfPredicate> CODEC = codec(AnyOfPredicate::new);

    public AnyOfPredicate(List<BlockPredicate> list) {
        super(list);
    }

    public boolean test(GeneratorAccessSeed generatoraccessseed, BlockPosition blockposition) {
        for (BlockPredicate blockpredicate : this.predicates) {
            if (blockpredicate.test(generatoraccessseed, blockposition)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.ANY_OF;
    }
}
