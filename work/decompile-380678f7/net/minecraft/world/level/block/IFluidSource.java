package net.minecraft.world.level.block;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.block.state.IBlockData;

public interface IFluidSource {

    ItemStack pickupBlock(@Nullable EntityLiving entityliving, GeneratorAccess generatoraccess, BlockPosition blockposition, IBlockData iblockdata);

    Optional<SoundEffect> getPickupSound();
}
