package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.GeneratorAccessSeed;

class AllOfPredicate extends CombiningPredicate {

    public static final MapCodec<AllOfPredicate> CODEC = codec(AllOfPredicate::new);

    public AllOfPredicate(List<BlockPredicate> list) {
        super(list);
    }

    public boolean test(GeneratorAccessSeed generatoraccessseed, BlockPosition blockposition) {
        for (BlockPredicate blockpredicate : this.predicates) {
            if (!blockpredicate.test(generatoraccessseed, blockposition)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.ALL_OF;
    }
}
