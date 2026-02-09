package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.sheep.EntitySheep;
import net.minecraft.world.phys.Vec3D;

public record SheepPredicate(Optional<Boolean> sheared) implements EntitySubPredicate {

    public static final MapCodec<SheepPredicate> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.BOOL.optionalFieldOf("sheared").forGetter(SheepPredicate::sheared)).apply(instance, SheepPredicate::new);
    });

    @Override
    public MapCodec<SheepPredicate> codec() {
        return EntitySubPredicates.SHEEP;
    }

    @Override
    public boolean matches(Entity entity, WorldServer worldserver, @Nullable Vec3D vec3d) {
        if (entity instanceof EntitySheep entitysheep) {
            return !this.sheared.isPresent() || entitysheep.isSheared() == (Boolean) this.sheared.get();
        } else {
            return false;
        }
    }

    public static SheepPredicate hasWool() {
        return new SheepPredicate(Optional.of(false));
    }
}
