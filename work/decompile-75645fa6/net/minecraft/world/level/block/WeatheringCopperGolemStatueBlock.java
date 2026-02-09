package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.animal.coppergolem.CopperGolem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class WeatheringCopperGolemStatueBlock extends CopperGolemStatueBlock implements WeatheringCopper {

    public static final MapCodec<WeatheringCopperGolemStatueBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(WeatheringCopper.a.CODEC.fieldOf("weathering_state").forGetter(ChangeOverTimeBlock::getAge), propertiesCodec()).apply(instance, WeatheringCopperGolemStatueBlock::new);
    });

    @Override
    public MapCodec<WeatheringCopperGolemStatueBlock> codec() {
        return WeatheringCopperGolemStatueBlock.CODEC;
    }

    public WeatheringCopperGolemStatueBlock(WeatheringCopper.a weatheringcopper_a, BlockBase.Info blockbase_info) {
        super(weatheringcopper_a, blockbase_info);
    }

    @Override
    protected boolean isRandomlyTicking(IBlockData iblockdata) {
        return WeatheringCopper.getNext(iblockdata.getBlock()).isPresent();
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        this.changeOverTime(iblockdata, worldserver, blockposition, randomsource);
    }

    @Override
    public WeatheringCopper.a getAge() {
        return this.getWeatheringState();
    }

    @Override
    protected EnumInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof CopperGolemStatueBlockEntity coppergolemstatueblockentity) {
            if (!itemstack.is(TagsItem.AXES)) {
                if (itemstack.is(Items.HONEYCOMB)) {
                    return EnumInteractionResult.PASS;
                }

                this.updatePose(world, iblockdata, blockposition, entityhuman);
                return EnumInteractionResult.SUCCESS;
            }

            if (this.getAge().equals(WeatheringCopper.a.UNAFFECTED)) {
                CopperGolem coppergolem = coppergolemstatueblockentity.removeStatue(iblockdata);

                itemstack.hurtAndBreak(1, entityhuman, enumhand.asEquipmentSlot());
                if (coppergolem != null) {
                    world.addFreshEntity(coppergolem);
                    world.removeBlock(blockposition, false);
                    return EnumInteractionResult.SUCCESS;
                }
            }
        }

        return EnumInteractionResult.PASS;
    }
}
