package net.minecraft.world.entity.decoration;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockDiodeAbstract;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.apache.commons.lang3.Validate;

public abstract class EntityHanging extends BlockAttachedEntity {

    private static final DataWatcherObject<EnumDirection> DATA_DIRECTION = DataWatcher.<EnumDirection>defineId(EntityHanging.class, DataWatcherRegistry.DIRECTION);
    private static final EnumDirection DEFAULT_DIRECTION = EnumDirection.SOUTH;

    protected EntityHanging(EntityTypes<? extends EntityHanging> entitytypes, World world) {
        super(entitytypes, world);
    }

    protected EntityHanging(EntityTypes<? extends EntityHanging> entitytypes, World world, BlockPosition blockposition) {
        this(entitytypes, world);
        this.pos = blockposition;
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityHanging.DATA_DIRECTION, EntityHanging.DEFAULT_DIRECTION);
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        super.onSyncedDataUpdated(datawatcherobject);
        if (datawatcherobject.equals(EntityHanging.DATA_DIRECTION)) {
            this.setDirection(this.getDirection());
        }

    }

    @Override
    public EnumDirection getDirection() {
        return (EnumDirection) this.entityData.get(EntityHanging.DATA_DIRECTION);
    }

    protected void setDirectionRaw(EnumDirection enumdirection) {
        this.entityData.set(EntityHanging.DATA_DIRECTION, enumdirection);
    }

    public void setDirection(EnumDirection enumdirection) {
        Objects.requireNonNull(enumdirection);
        Validate.isTrue(enumdirection.getAxis().isHorizontal());
        this.setDirectionRaw(enumdirection);
        this.setYRot((float) (enumdirection.get2DDataValue() * 90));
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    protected void recalculateBoundingBox() {
        if (this.getDirection() != null) {
            AxisAlignedBB axisalignedbb = this.calculateBoundingBox(this.pos, this.getDirection());
            Vec3D vec3d = axisalignedbb.getCenter();

            this.setPosRaw(vec3d.x, vec3d.y, vec3d.z);
            this.setBoundingBox(axisalignedbb);
        }
    }

    protected abstract AxisAlignedBB calculateBoundingBox(BlockPosition blockposition, EnumDirection enumdirection);

    @Override
    public boolean survives() {
        if (!this.level().noCollision(this, this.getPopBox())) {
            return false;
        } else {
            boolean flag = BlockPosition.betweenClosedStream(this.calculateSupportBox()).allMatch((blockposition) -> {
                IBlockData iblockdata = this.level().getBlockState(blockposition);

                return iblockdata.isSolid() || BlockDiodeAbstract.isDiode(iblockdata);
            });

            return flag && this.canCoexist(false);
        }
    }

    protected AxisAlignedBB calculateSupportBox() {
        return this.getBoundingBox().move(this.getDirection().step().mul(-0.5F)).deflate(1.0E-7D);
    }

    protected boolean canCoexist(boolean flag) {
        Predicate<EntityHanging> predicate = (entityhanging) -> {
            boolean flag1 = !flag && entityhanging.getType() == this.getType();
            boolean flag2 = entityhanging.getDirection() == this.getDirection();

            return entityhanging != this && (flag1 || flag2);
        };

        return !this.level().hasEntities(EntityTypeTest.forClass(EntityHanging.class), this.getPopBox(), predicate);
    }

    protected AxisAlignedBB getPopBox() {
        return this.getBoundingBox();
    }

    public abstract void playPlacementSound();

    @Override
    public EntityItem spawnAtLocation(WorldServer worldserver, ItemStack itemstack, float f) {
        EntityItem entityitem = new EntityItem(this.level(), this.getX() + (double) ((float) this.getDirection().getStepX() * 0.15F), this.getY() + (double) f, this.getZ() + (double) ((float) this.getDirection().getStepZ() * 0.15F), itemstack);

        entityitem.setDefaultPickUpDelay();
        this.level().addFreshEntity(entityitem);
        return entityitem;
    }

    @Override
    public float rotate(EnumBlockRotation enumblockrotation) {
        EnumDirection enumdirection = this.getDirection();

        if (enumdirection.getAxis() != EnumDirection.EnumAxis.Y) {
            switch (enumblockrotation) {
                case CLOCKWISE_180:
                    enumdirection = enumdirection.getOpposite();
                    break;
                case COUNTERCLOCKWISE_90:
                    enumdirection = enumdirection.getCounterClockWise();
                    break;
                case CLOCKWISE_90:
                    enumdirection = enumdirection.getClockWise();
            }

            this.setDirection(enumdirection);
        }

        float f = MathHelper.wrapDegrees(this.getYRot());
        float f1;

        switch (enumblockrotation) {
            case CLOCKWISE_180:
                f1 = f + 180.0F;
                break;
            case COUNTERCLOCKWISE_90:
                f1 = f + 90.0F;
                break;
            case CLOCKWISE_90:
                f1 = f + 270.0F;
                break;
            default:
                f1 = f;
        }

        return f1;
    }

    @Override
    public float mirror(EnumBlockMirror enumblockmirror) {
        return this.rotate(enumblockmirror.getRotation(this.getDirection()));
    }
}
