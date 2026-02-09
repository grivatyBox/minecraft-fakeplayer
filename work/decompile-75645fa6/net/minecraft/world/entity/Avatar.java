package net.minecraft.world.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;

public class Avatar extends EntityLiving {

    public static final EnumMainHand DEFAULT_MAIN_HAND = EnumMainHand.RIGHT;
    public static final int DEFAULT_MODEL_CUSTOMIZATION = 0;
    public static final float DEFAULT_EYE_HEIGHT = 1.62F;
    public static final Vec3D DEFAULT_VEHICLE_ATTACHMENT = new Vec3D(0.0D, 0.6D, 0.0D);
    private static final float CROUCH_BB_HEIGHT = 1.5F;
    private static final float SWIMMING_BB_WIDTH = 0.6F;
    public static final float SWIMMING_BB_HEIGHT = 0.6F;
    protected static final EntitySize STANDING_DIMENSIONS = EntitySize.scalable(0.6F, 1.8F).withEyeHeight(1.62F).withAttachments(EntityAttachments.builder().attach(EntityAttachment.VEHICLE, Avatar.DEFAULT_VEHICLE_ATTACHMENT));
    protected static final Map<EntityPose, EntitySize> POSES = ImmutableMap.builder().put(EntityPose.STANDING, Avatar.STANDING_DIMENSIONS).put(EntityPose.SLEEPING, Avatar.SLEEPING_DIMENSIONS).put(EntityPose.FALL_FLYING, EntitySize.scalable(0.6F, 0.6F).withEyeHeight(0.4F)).put(EntityPose.SWIMMING, EntitySize.scalable(0.6F, 0.6F).withEyeHeight(0.4F)).put(EntityPose.SPIN_ATTACK, EntitySize.scalable(0.6F, 0.6F).withEyeHeight(0.4F)).put(EntityPose.CROUCHING, EntitySize.scalable(0.6F, 1.5F).withEyeHeight(1.27F).withAttachments(EntityAttachments.builder().attach(EntityAttachment.VEHICLE, Avatar.DEFAULT_VEHICLE_ATTACHMENT))).put(EntityPose.DYING, EntitySize.fixed(0.2F, 0.2F).withEyeHeight(1.62F)).build();
    protected static final DataWatcherObject<Byte> DATA_PLAYER_MAIN_HAND = DataWatcher.<Byte>defineId(Avatar.class, DataWatcherRegistry.BYTE);
    public static final DataWatcherObject<Byte> DATA_PLAYER_MODE_CUSTOMISATION = DataWatcher.<Byte>defineId(Avatar.class, DataWatcherRegistry.BYTE);

    protected Avatar(EntityTypes<? extends EntityLiving> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(Avatar.DATA_PLAYER_MAIN_HAND, (byte) Avatar.DEFAULT_MAIN_HAND.getId());
        datawatcher_a.define(Avatar.DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0);
    }

    @Override
    public EnumMainHand getMainArm() {
        return (Byte) this.entityData.get(Avatar.DATA_PLAYER_MAIN_HAND) == 0 ? EnumMainHand.LEFT : EnumMainHand.RIGHT;
    }

    public void setMainArm(EnumMainHand enummainhand) {
        this.entityData.set(Avatar.DATA_PLAYER_MAIN_HAND, (byte) (enummainhand == EnumMainHand.LEFT ? 0 : 1));
    }

    public boolean isModelPartShown(PlayerModelPart playermodelpart) {
        return ((Byte) this.getEntityData().get(Avatar.DATA_PLAYER_MODE_CUSTOMISATION) & playermodelpart.getMask()) == playermodelpart.getMask();
    }

    @Override
    public EntitySize getDefaultDimensions(EntityPose entitypose) {
        return (EntitySize) Avatar.POSES.getOrDefault(entitypose, Avatar.STANDING_DIMENSIONS);
    }
}
