package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.INamableTileEntity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileEntityEnchantTable extends TileEntity implements INamableTileEntity {

    private static final IChatBaseComponent DEFAULT_NAME = IChatBaseComponent.translatable("container.enchant");
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final RandomSource RANDOM = RandomSource.create();
    @Nullable
    private IChatBaseComponent name;

    public TileEntityEnchantTable(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.ENCHANTING_TABLE, blockposition, iblockdata);
    }

    @Override
    protected void saveAdditional(ValueOutput valueoutput) {
        super.saveAdditional(valueoutput);
        valueoutput.storeNullable("CustomName", ComponentSerialization.CODEC, this.name);
    }

    @Override
    protected void loadAdditional(ValueInput valueinput) {
        super.loadAdditional(valueinput);
        this.name = parseCustomNameSafe(valueinput, "CustomName");
    }

    public static void bookAnimationTick(World world, BlockPosition blockposition, IBlockData iblockdata, TileEntityEnchantTable tileentityenchanttable) {
        tileentityenchanttable.oOpen = tileentityenchanttable.open;
        tileentityenchanttable.oRot = tileentityenchanttable.rot;
        EntityHuman entityhuman = world.getNearestPlayer((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D, 3.0D, false);

        if (entityhuman != null) {
            double d0 = entityhuman.getX() - ((double) blockposition.getX() + 0.5D);
            double d1 = entityhuman.getZ() - ((double) blockposition.getZ() + 0.5D);

            tileentityenchanttable.tRot = (float) MathHelper.atan2(d1, d0);
            tileentityenchanttable.open += 0.1F;
            if (tileentityenchanttable.open < 0.5F || TileEntityEnchantTable.RANDOM.nextInt(40) == 0) {
                float f = tileentityenchanttable.flipT;

                do {
                    tileentityenchanttable.flipT += (float) (TileEntityEnchantTable.RANDOM.nextInt(4) - TileEntityEnchantTable.RANDOM.nextInt(4));
                } while (f == tileentityenchanttable.flipT);
            }
        } else {
            tileentityenchanttable.tRot += 0.02F;
            tileentityenchanttable.open -= 0.1F;
        }

        while (tileentityenchanttable.rot >= (float) Math.PI) {
            tileentityenchanttable.rot -= ((float) Math.PI * 2F);
        }

        while (tileentityenchanttable.rot < -(float) Math.PI) {
            tileentityenchanttable.rot += ((float) Math.PI * 2F);
        }

        while (tileentityenchanttable.tRot >= (float) Math.PI) {
            tileentityenchanttable.tRot -= ((float) Math.PI * 2F);
        }

        while (tileentityenchanttable.tRot < -(float) Math.PI) {
            tileentityenchanttable.tRot += ((float) Math.PI * 2F);
        }

        float f1;

        for (f1 = tileentityenchanttable.tRot - tileentityenchanttable.rot; f1 >= (float) Math.PI; f1 -= ((float) Math.PI * 2F)) {
            ;
        }

        while (f1 < -(float) Math.PI) {
            f1 += ((float) Math.PI * 2F);
        }

        tileentityenchanttable.rot += f1 * 0.4F;
        tileentityenchanttable.open = MathHelper.clamp(tileentityenchanttable.open, 0.0F, 1.0F);
        ++tileentityenchanttable.time;
        tileentityenchanttable.oFlip = tileentityenchanttable.flip;
        float f2 = (tileentityenchanttable.flipT - tileentityenchanttable.flip) * 0.4F;
        float f3 = 0.2F;

        f2 = MathHelper.clamp(f2, -0.2F, 0.2F);
        tileentityenchanttable.flipA += (f2 - tileentityenchanttable.flipA) * 0.9F;
        tileentityenchanttable.flip += tileentityenchanttable.flipA;
    }

    @Override
    public IChatBaseComponent getName() {
        return this.name != null ? this.name : TileEntityEnchantTable.DEFAULT_NAME;
    }

    public void setCustomName(@Nullable IChatBaseComponent ichatbasecomponent) {
        this.name = ichatbasecomponent;
    }

    @Nullable
    @Override
    public IChatBaseComponent getCustomName() {
        return this.name;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter datacomponentgetter) {
        super.applyImplicitComponents(datacomponentgetter);
        this.name = (IChatBaseComponent) datacomponentgetter.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.a datacomponentmap_a) {
        super.collectImplicitComponents(datacomponentmap_a);
        datacomponentmap_a.set(DataComponents.CUSTOM_NAME, this.name);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput valueoutput) {
        valueoutput.discard("CustomName");
    }
}
