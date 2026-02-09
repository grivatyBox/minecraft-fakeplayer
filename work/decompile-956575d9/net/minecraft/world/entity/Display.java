package net.minecraft.world.entity;

import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Brightness;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FormattedString;
import net.minecraft.util.INamable;
import net.minecraft.util.MathHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.EnumPistonReaction;
import net.minecraft.world.phys.AxisAlignedBB;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

public abstract class Display extends Entity {

    static final Logger LOGGER = LogUtils.getLogger();
    public static final int NO_BRIGHTNESS_OVERRIDE = -1;
    private static final DataWatcherObject<Integer> DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID = DataWatcher.<Integer>defineId(Display.class, DataWatcherRegistry.INT);
    private static final DataWatcherObject<Integer> DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID = DataWatcher.<Integer>defineId(Display.class, DataWatcherRegistry.INT);
    public static final DataWatcherObject<Integer> DATA_POS_ROT_INTERPOLATION_DURATION_ID = DataWatcher.<Integer>defineId(Display.class, DataWatcherRegistry.INT);
    private static final DataWatcherObject<Vector3f> DATA_TRANSLATION_ID = DataWatcher.<Vector3f>defineId(Display.class, DataWatcherRegistry.VECTOR3);
    private static final DataWatcherObject<Vector3f> DATA_SCALE_ID = DataWatcher.<Vector3f>defineId(Display.class, DataWatcherRegistry.VECTOR3);
    private static final DataWatcherObject<Quaternionf> DATA_LEFT_ROTATION_ID = DataWatcher.<Quaternionf>defineId(Display.class, DataWatcherRegistry.QUATERNION);
    private static final DataWatcherObject<Quaternionf> DATA_RIGHT_ROTATION_ID = DataWatcher.<Quaternionf>defineId(Display.class, DataWatcherRegistry.QUATERNION);
    private static final DataWatcherObject<Byte> DATA_BILLBOARD_RENDER_CONSTRAINTS_ID = DataWatcher.<Byte>defineId(Display.class, DataWatcherRegistry.BYTE);
    private static final DataWatcherObject<Integer> DATA_BRIGHTNESS_OVERRIDE_ID = DataWatcher.<Integer>defineId(Display.class, DataWatcherRegistry.INT);
    private static final DataWatcherObject<Float> DATA_VIEW_RANGE_ID = DataWatcher.<Float>defineId(Display.class, DataWatcherRegistry.FLOAT);
    private static final DataWatcherObject<Float> DATA_SHADOW_RADIUS_ID = DataWatcher.<Float>defineId(Display.class, DataWatcherRegistry.FLOAT);
    private static final DataWatcherObject<Float> DATA_SHADOW_STRENGTH_ID = DataWatcher.<Float>defineId(Display.class, DataWatcherRegistry.FLOAT);
    private static final DataWatcherObject<Float> DATA_WIDTH_ID = DataWatcher.<Float>defineId(Display.class, DataWatcherRegistry.FLOAT);
    private static final DataWatcherObject<Float> DATA_HEIGHT_ID = DataWatcher.<Float>defineId(Display.class, DataWatcherRegistry.FLOAT);
    private static final DataWatcherObject<Integer> DATA_GLOW_COLOR_OVERRIDE_ID = DataWatcher.<Integer>defineId(Display.class, DataWatcherRegistry.INT);
    private static final IntSet RENDER_STATE_IDS = IntSet.of(new int[]{Display.DATA_TRANSLATION_ID.id(), Display.DATA_SCALE_ID.id(), Display.DATA_LEFT_ROTATION_ID.id(), Display.DATA_RIGHT_ROTATION_ID.id(), Display.DATA_BILLBOARD_RENDER_CONSTRAINTS_ID.id(), Display.DATA_BRIGHTNESS_OVERRIDE_ID.id(), Display.DATA_SHADOW_RADIUS_ID.id(), Display.DATA_SHADOW_STRENGTH_ID.id()});
    private static final int INITIAL_TRANSFORMATION_INTERPOLATION_DURATION = 0;
    private static final int INITIAL_TRANSFORMATION_START_INTERPOLATION = 0;
    private static final int INITIAL_POS_ROT_INTERPOLATION_DURATION = 0;
    private static final float INITIAL_SHADOW_RADIUS = 0.0F;
    private static final float INITIAL_SHADOW_STRENGTH = 1.0F;
    private static final float INITIAL_VIEW_RANGE = 1.0F;
    private static final float INITIAL_WIDTH = 0.0F;
    private static final float INITIAL_HEIGHT = 0.0F;
    private static final int NO_GLOW_COLOR_OVERRIDE = -1;
    public static final String TAG_POS_ROT_INTERPOLATION_DURATION = "teleport_duration";
    public static final String TAG_TRANSFORMATION_INTERPOLATION_DURATION = "interpolation_duration";
    public static final String TAG_TRANSFORMATION_START_INTERPOLATION = "start_interpolation";
    public static final String TAG_TRANSFORMATION = "transformation";
    public static final String TAG_BILLBOARD = "billboard";
    public static final String TAG_BRIGHTNESS = "brightness";
    public static final String TAG_VIEW_RANGE = "view_range";
    public static final String TAG_SHADOW_RADIUS = "shadow_radius";
    public static final String TAG_SHADOW_STRENGTH = "shadow_strength";
    public static final String TAG_WIDTH = "width";
    public static final String TAG_HEIGHT = "height";
    public static final String TAG_GLOW_COLOR_OVERRIDE = "glow_color_override";
    private long interpolationStartClientTick = -2147483648L;
    private int interpolationDuration;
    private float lastProgress;
    private AxisAlignedBB cullingBoundingBox;
    private boolean noCulling = true;
    protected boolean updateRenderState;
    private boolean updateStartTick;
    private boolean updateInterpolationDuration;
    @Nullable
    private Display.j renderState;
    private final InterpolationHandler interpolation = new InterpolationHandler(this, 0);

    public Display(EntityTypes<?> entitytypes, World world) {
        super(entitytypes, world);
        this.noPhysics = true;
        this.cullingBoundingBox = this.getBoundingBox();
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        super.onSyncedDataUpdated(datawatcherobject);
        if (Display.DATA_HEIGHT_ID.equals(datawatcherobject) || Display.DATA_WIDTH_ID.equals(datawatcherobject)) {
            this.updateCulling();
        }

        if (Display.DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID.equals(datawatcherobject)) {
            this.updateStartTick = true;
        }

        if (Display.DATA_POS_ROT_INTERPOLATION_DURATION_ID.equals(datawatcherobject)) {
            this.interpolation.setInterpolationLength(this.getPosRotInterpolationDuration());
        }

        if (Display.DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID.equals(datawatcherobject)) {
            this.updateInterpolationDuration = true;
        }

        if (Display.RENDER_STATE_IDS.contains(datawatcherobject.id())) {
            this.updateRenderState = true;
        }

    }

    @Override
    public final boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        return false;
    }

    public static Transformation createTransformation(DataWatcher datawatcher) {
        Vector3f vector3f = (Vector3f) datawatcher.get(Display.DATA_TRANSLATION_ID);
        Quaternionf quaternionf = (Quaternionf) datawatcher.get(Display.DATA_LEFT_ROTATION_ID);
        Vector3f vector3f1 = (Vector3f) datawatcher.get(Display.DATA_SCALE_ID);
        Quaternionf quaternionf1 = (Quaternionf) datawatcher.get(Display.DATA_RIGHT_ROTATION_ID);

        return new Transformation(vector3f, quaternionf, vector3f1, quaternionf1);
    }

    @Override
    public void tick() {
        Entity entity = this.getVehicle();

        if (entity != null && entity.isRemoved()) {
            this.stopRiding();
        }

        if (this.level().isClientSide) {
            if (this.updateStartTick) {
                this.updateStartTick = false;
                int i = this.getTransformationInterpolationDelay();

                this.interpolationStartClientTick = (long) (this.tickCount + i);
            }

            if (this.updateInterpolationDuration) {
                this.updateInterpolationDuration = false;
                this.interpolationDuration = this.getTransformationInterpolationDuration();
            }

            if (this.updateRenderState) {
                this.updateRenderState = false;
                boolean flag = this.interpolationDuration != 0;

                if (flag && this.renderState != null) {
                    this.renderState = this.createInterpolatedRenderState(this.renderState, this.lastProgress);
                } else {
                    this.renderState = this.createFreshRenderState();
                }

                this.updateRenderSubState(flag, this.lastProgress);
            }

            this.interpolation.interpolate();
        }

    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolation;
    }

    protected abstract void updateRenderSubState(boolean flag, float f);

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(Display.DATA_POS_ROT_INTERPOLATION_DURATION_ID, 0);
        datawatcher_a.define(Display.DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID, 0);
        datawatcher_a.define(Display.DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID, 0);
        datawatcher_a.define(Display.DATA_TRANSLATION_ID, new Vector3f());
        datawatcher_a.define(Display.DATA_SCALE_ID, new Vector3f(1.0F, 1.0F, 1.0F));
        datawatcher_a.define(Display.DATA_RIGHT_ROTATION_ID, new Quaternionf());
        datawatcher_a.define(Display.DATA_LEFT_ROTATION_ID, new Quaternionf());
        datawatcher_a.define(Display.DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, Display.BillboardConstraints.FIXED.getId());
        datawatcher_a.define(Display.DATA_BRIGHTNESS_OVERRIDE_ID, -1);
        datawatcher_a.define(Display.DATA_VIEW_RANGE_ID, 1.0F);
        datawatcher_a.define(Display.DATA_SHADOW_RADIUS_ID, 0.0F);
        datawatcher_a.define(Display.DATA_SHADOW_STRENGTH_ID, 1.0F);
        datawatcher_a.define(Display.DATA_WIDTH_ID, 0.0F);
        datawatcher_a.define(Display.DATA_HEIGHT_ID, 0.0F);
        datawatcher_a.define(Display.DATA_GLOW_COLOR_OVERRIDE_ID, -1);
    }

    @Override
    protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        this.setTransformation((Transformation) nbttagcompound.read("transformation", Transformation.EXTENDED_CODEC).orElse(Transformation.identity()));
        this.setTransformationInterpolationDuration(nbttagcompound.getIntOr("interpolation_duration", 0));
        this.setTransformationInterpolationDelay(nbttagcompound.getIntOr("start_interpolation", 0));
        int i = nbttagcompound.getIntOr("teleport_duration", 0);

        this.setPosRotInterpolationDuration(MathHelper.clamp(i, 0, 59));
        this.setBillboardConstraints((Display.BillboardConstraints) nbttagcompound.read("billboard", Display.BillboardConstraints.CODEC).orElse(Display.BillboardConstraints.FIXED));
        this.setViewRange(nbttagcompound.getFloatOr("view_range", 1.0F));
        this.setShadowRadius(nbttagcompound.getFloatOr("shadow_radius", 0.0F));
        this.setShadowStrength(nbttagcompound.getFloatOr("shadow_strength", 1.0F));
        this.setWidth(nbttagcompound.getFloatOr("width", 0.0F));
        this.setHeight(nbttagcompound.getFloatOr("height", 0.0F));
        this.setGlowColorOverride(nbttagcompound.getIntOr("glow_color_override", -1));
        this.setBrightnessOverride((Brightness) nbttagcompound.read("brightness", Brightness.CODEC).orElse((Object) null));
    }

    public void setTransformation(Transformation transformation) {
        this.entityData.set(Display.DATA_TRANSLATION_ID, transformation.getTranslation());
        this.entityData.set(Display.DATA_LEFT_ROTATION_ID, transformation.getLeftRotation());
        this.entityData.set(Display.DATA_SCALE_ID, transformation.getScale());
        this.entityData.set(Display.DATA_RIGHT_ROTATION_ID, transformation.getRightRotation());
    }

    @Override
    protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        nbttagcompound.store("transformation", Transformation.EXTENDED_CODEC, createTransformation(this.entityData));
        nbttagcompound.store("billboard", Display.BillboardConstraints.CODEC, this.getBillboardConstraints());
        nbttagcompound.putInt("interpolation_duration", this.getTransformationInterpolationDuration());
        nbttagcompound.putInt("teleport_duration", this.getPosRotInterpolationDuration());
        nbttagcompound.putFloat("view_range", this.getViewRange());
        nbttagcompound.putFloat("shadow_radius", this.getShadowRadius());
        nbttagcompound.putFloat("shadow_strength", this.getShadowStrength());
        nbttagcompound.putFloat("width", this.getWidth());
        nbttagcompound.putFloat("height", this.getHeight());
        nbttagcompound.putInt("glow_color_override", this.getGlowColorOverride());
        nbttagcompound.storeNullable("brightness", Brightness.CODEC, this.getBrightnessOverride());
    }

    public AxisAlignedBB getBoundingBoxForCulling() {
        return this.cullingBoundingBox;
    }

    public boolean affectedByCulling() {
        return !this.noCulling;
    }

    @Override
    public EnumPistonReaction getPistonPushReaction() {
        return EnumPistonReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Nullable
    public Display.j renderState() {
        return this.renderState;
    }

    public void setTransformationInterpolationDuration(int i) {
        this.entityData.set(Display.DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID, i);
    }

    public int getTransformationInterpolationDuration() {
        return (Integer) this.entityData.get(Display.DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID);
    }

    public void setTransformationInterpolationDelay(int i) {
        this.entityData.set(Display.DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID, i, true);
    }

    public int getTransformationInterpolationDelay() {
        return (Integer) this.entityData.get(Display.DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID);
    }

    private void setPosRotInterpolationDuration(int i) {
        this.entityData.set(Display.DATA_POS_ROT_INTERPOLATION_DURATION_ID, i);
    }

    private int getPosRotInterpolationDuration() {
        return (Integer) this.entityData.get(Display.DATA_POS_ROT_INTERPOLATION_DURATION_ID);
    }

    public void setBillboardConstraints(Display.BillboardConstraints display_billboardconstraints) {
        this.entityData.set(Display.DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, display_billboardconstraints.getId());
    }

    public Display.BillboardConstraints getBillboardConstraints() {
        return (Display.BillboardConstraints) Display.BillboardConstraints.BY_ID.apply((Byte) this.entityData.get(Display.DATA_BILLBOARD_RENDER_CONSTRAINTS_ID));
    }

    public void setBrightnessOverride(@Nullable Brightness brightness) {
        this.entityData.set(Display.DATA_BRIGHTNESS_OVERRIDE_ID, brightness != null ? brightness.pack() : -1);
    }

    @Nullable
    public Brightness getBrightnessOverride() {
        int i = (Integer) this.entityData.get(Display.DATA_BRIGHTNESS_OVERRIDE_ID);

        return i != -1 ? Brightness.unpack(i) : null;
    }

    private int getPackedBrightnessOverride() {
        return (Integer) this.entityData.get(Display.DATA_BRIGHTNESS_OVERRIDE_ID);
    }

    public void setViewRange(float f) {
        this.entityData.set(Display.DATA_VIEW_RANGE_ID, f);
    }

    public float getViewRange() {
        return (Float) this.entityData.get(Display.DATA_VIEW_RANGE_ID);
    }

    public void setShadowRadius(float f) {
        this.entityData.set(Display.DATA_SHADOW_RADIUS_ID, f);
    }

    public float getShadowRadius() {
        return (Float) this.entityData.get(Display.DATA_SHADOW_RADIUS_ID);
    }

    public void setShadowStrength(float f) {
        this.entityData.set(Display.DATA_SHADOW_STRENGTH_ID, f);
    }

    public float getShadowStrength() {
        return (Float) this.entityData.get(Display.DATA_SHADOW_STRENGTH_ID);
    }

    public void setWidth(float f) {
        this.entityData.set(Display.DATA_WIDTH_ID, f);
    }

    public float getWidth() {
        return (Float) this.entityData.get(Display.DATA_WIDTH_ID);
    }

    public void setHeight(float f) {
        this.entityData.set(Display.DATA_HEIGHT_ID, f);
    }

    public int getGlowColorOverride() {
        return (Integer) this.entityData.get(Display.DATA_GLOW_COLOR_OVERRIDE_ID);
    }

    public void setGlowColorOverride(int i) {
        this.entityData.set(Display.DATA_GLOW_COLOR_OVERRIDE_ID, i);
    }

    public float calculateInterpolationProgress(float f) {
        int i = this.interpolationDuration;

        if (i <= 0) {
            return 1.0F;
        } else {
            float f1 = (float) ((long) this.tickCount - this.interpolationStartClientTick);
            float f2 = f1 + f;
            float f3 = MathHelper.clamp(MathHelper.inverseLerp(f2, 0.0F, (float) i), 0.0F, 1.0F);

            this.lastProgress = f3;
            return f3;
        }
    }

    public float getHeight() {
        return (Float) this.entityData.get(Display.DATA_HEIGHT_ID);
    }

    @Override
    public void setPos(double d0, double d1, double d2) {
        super.setPos(d0, d1, d2);
        this.updateCulling();
    }

    private void updateCulling() {
        float f = this.getWidth();
        float f1 = this.getHeight();

        this.noCulling = f == 0.0F || f1 == 0.0F;
        float f2 = f / 2.0F;
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();

        this.cullingBoundingBox = new AxisAlignedBB(d0 - (double) f2, d1, d2 - (double) f2, d0 + (double) f2, d1 + (double) f1, d2 + (double) f2);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d0) {
        return d0 < MathHelper.square((double) this.getViewRange() * 64.0D * getViewScale());
    }

    @Override
    public int getTeamColor() {
        int i = this.getGlowColorOverride();

        return i != -1 ? i : super.getTeamColor();
    }

    private Display.j createFreshRenderState() {
        return new Display.j(Display.GenericInterpolator.constant(createTransformation(this.entityData)), this.getBillboardConstraints(), this.getPackedBrightnessOverride(), Display.FloatInterpolator.constant(this.getShadowRadius()), Display.FloatInterpolator.constant(this.getShadowStrength()), this.getGlowColorOverride());
    }

    private Display.j createInterpolatedRenderState(Display.j display_j, float f) {
        Transformation transformation = display_j.transformation.get(f);
        float f1 = display_j.shadowRadius.get(f);
        float f2 = display_j.shadowStrength.get(f);

        return new Display.j(new Display.l(transformation, createTransformation(this.entityData)), this.getBillboardConstraints(), this.getPackedBrightnessOverride(), new Display.h(f1, this.getShadowRadius()), new Display.h(f2, this.getShadowStrength()), this.getGlowColorOverride());
    }

    public static enum BillboardConstraints implements INamable {

        FIXED((byte) 0, "fixed"), VERTICAL((byte) 1, "vertical"), HORIZONTAL((byte) 2, "horizontal"), CENTER((byte) 3, "center");

        public static final Codec<Display.BillboardConstraints> CODEC = INamable.<Display.BillboardConstraints>fromEnum(Display.BillboardConstraints::values);
        public static final IntFunction<Display.BillboardConstraints> BY_ID = ByIdMap.<Display.BillboardConstraints>continuous(Display.BillboardConstraints::getId, values(), ByIdMap.a.ZERO);
        private final byte id;
        private final String name;

        private BillboardConstraints(final byte b0, final String s) {
            this.name = s;
            this.id = b0;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        byte getId() {
            return this.id;
        }
    }

    public static record j(Display.GenericInterpolator<Transformation> transformation, Display.BillboardConstraints billboardConstraints, int brightnessOverride, Display.FloatInterpolator shadowRadius, Display.FloatInterpolator shadowStrength, int glowColorOverride) {

    }

    public static class ItemDisplay extends Display {

        private static final String TAG_ITEM = "item";
        private static final String TAG_ITEM_DISPLAY = "item_display";
        private static final DataWatcherObject<ItemStack> DATA_ITEM_STACK_ID = DataWatcher.<ItemStack>defineId(Display.ItemDisplay.class, DataWatcherRegistry.ITEM_STACK);
        private static final DataWatcherObject<Byte> DATA_ITEM_DISPLAY_ID = DataWatcher.<Byte>defineId(Display.ItemDisplay.class, DataWatcherRegistry.BYTE);
        private final SlotAccess slot = SlotAccess.of(this::getItemStack, this::setItemStack);
        @Nullable
        private Display.ItemDisplay.a itemRenderState;

        public ItemDisplay(EntityTypes<?> entitytypes, World world) {
            super(entitytypes, world);
        }

        @Override
        protected void defineSynchedData(DataWatcher.a datawatcher_a) {
            super.defineSynchedData(datawatcher_a);
            datawatcher_a.define(Display.ItemDisplay.DATA_ITEM_STACK_ID, ItemStack.EMPTY);
            datawatcher_a.define(Display.ItemDisplay.DATA_ITEM_DISPLAY_ID, ItemDisplayContext.NONE.getId());
        }

        @Override
        public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
            super.onSyncedDataUpdated(datawatcherobject);
            if (Display.ItemDisplay.DATA_ITEM_STACK_ID.equals(datawatcherobject) || Display.ItemDisplay.DATA_ITEM_DISPLAY_ID.equals(datawatcherobject)) {
                this.updateRenderState = true;
            }

        }

        public ItemStack getItemStack() {
            return (ItemStack) this.entityData.get(Display.ItemDisplay.DATA_ITEM_STACK_ID);
        }

        public void setItemStack(ItemStack itemstack) {
            this.entityData.set(Display.ItemDisplay.DATA_ITEM_STACK_ID, itemstack);
        }

        public void setItemTransform(ItemDisplayContext itemdisplaycontext) {
            this.entityData.set(Display.ItemDisplay.DATA_ITEM_DISPLAY_ID, itemdisplaycontext.getId());
        }

        public ItemDisplayContext getItemTransform() {
            return (ItemDisplayContext) ItemDisplayContext.BY_ID.apply((Byte) this.entityData.get(Display.ItemDisplay.DATA_ITEM_DISPLAY_ID));
        }

        @Override
        protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
            super.readAdditionalSaveData(nbttagcompound);
            RegistryOps<NBTBase> registryops = this.registryAccess().<NBTBase>createSerializationContext(DynamicOpsNBT.INSTANCE);

            this.setItemStack((ItemStack) nbttagcompound.read("item", ItemStack.CODEC, registryops).orElse(ItemStack.EMPTY));
            this.setItemTransform((ItemDisplayContext) nbttagcompound.read("item_display", ItemDisplayContext.CODEC).orElse(ItemDisplayContext.NONE));
        }

        @Override
        protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
            super.addAdditionalSaveData(nbttagcompound);
            if (!this.getItemStack().isEmpty()) {
                nbttagcompound.store("item", ItemStack.CODEC, this.registryAccess().createSerializationContext(DynamicOpsNBT.INSTANCE), this.getItemStack());
            }

            nbttagcompound.store("item_display", ItemDisplayContext.CODEC, this.getItemTransform());
        }

        @Override
        public SlotAccess getSlot(int i) {
            return i == 0 ? this.slot : SlotAccess.NULL;
        }

        @Nullable
        public Display.ItemDisplay.a itemRenderState() {
            return this.itemRenderState;
        }

        @Override
        protected void updateRenderSubState(boolean flag, float f) {
            ItemStack itemstack = this.getItemStack();

            itemstack.setEntityRepresentation(this);
            this.itemRenderState = new Display.ItemDisplay.a(itemstack, this.getItemTransform());
        }

        public static record a(ItemStack itemStack, ItemDisplayContext itemTransform) {

        }
    }

    public static class BlockDisplay extends Display {

        public static final String TAG_BLOCK_STATE = "block_state";
        private static final DataWatcherObject<IBlockData> DATA_BLOCK_STATE_ID = DataWatcher.<IBlockData>defineId(Display.BlockDisplay.class, DataWatcherRegistry.BLOCK_STATE);
        @Nullable
        private Display.BlockDisplay.a blockRenderState;

        public BlockDisplay(EntityTypes<?> entitytypes, World world) {
            super(entitytypes, world);
        }

        @Override
        protected void defineSynchedData(DataWatcher.a datawatcher_a) {
            super.defineSynchedData(datawatcher_a);
            datawatcher_a.define(Display.BlockDisplay.DATA_BLOCK_STATE_ID, Blocks.AIR.defaultBlockState());
        }

        @Override
        public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
            super.onSyncedDataUpdated(datawatcherobject);
            if (datawatcherobject.equals(Display.BlockDisplay.DATA_BLOCK_STATE_ID)) {
                this.updateRenderState = true;
            }

        }

        public IBlockData getBlockState() {
            return (IBlockData) this.entityData.get(Display.BlockDisplay.DATA_BLOCK_STATE_ID);
        }

        public void setBlockState(IBlockData iblockdata) {
            this.entityData.set(Display.BlockDisplay.DATA_BLOCK_STATE_ID, iblockdata);
        }

        @Override
        protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
            super.readAdditionalSaveData(nbttagcompound);
            RegistryOps<NBTBase> registryops = this.registryAccess().<NBTBase>createSerializationContext(DynamicOpsNBT.INSTANCE);

            this.setBlockState((IBlockData) nbttagcompound.read("block_state", IBlockData.CODEC, registryops).orElse(Blocks.AIR.defaultBlockState()));
        }

        @Override
        protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
            super.addAdditionalSaveData(nbttagcompound);
            RegistryOps<NBTBase> registryops = this.registryAccess().<NBTBase>createSerializationContext(DynamicOpsNBT.INSTANCE);

            nbttagcompound.store("block_state", IBlockData.CODEC, registryops, this.getBlockState());
        }

        @Nullable
        public Display.BlockDisplay.a blockRenderState() {
            return this.blockRenderState;
        }

        @Override
        protected void updateRenderSubState(boolean flag, float f) {
            this.blockRenderState = new Display.BlockDisplay.a(this.getBlockState());
        }

        public static record a(IBlockData blockState) {

        }
    }

    public static class TextDisplay extends Display {

        public static final String TAG_TEXT = "text";
        private static final String TAG_LINE_WIDTH = "line_width";
        private static final String TAG_TEXT_OPACITY = "text_opacity";
        private static final String TAG_BACKGROUND_COLOR = "background";
        private static final String TAG_SHADOW = "shadow";
        private static final String TAG_SEE_THROUGH = "see_through";
        private static final String TAG_USE_DEFAULT_BACKGROUND = "default_background";
        private static final String TAG_ALIGNMENT = "alignment";
        public static final byte FLAG_SHADOW = 1;
        public static final byte FLAG_SEE_THROUGH = 2;
        public static final byte FLAG_USE_DEFAULT_BACKGROUND = 4;
        public static final byte FLAG_ALIGN_LEFT = 8;
        public static final byte FLAG_ALIGN_RIGHT = 16;
        private static final byte INITIAL_TEXT_OPACITY = -1;
        public static final int INITIAL_BACKGROUND = 1073741824;
        private static final int INITIAL_LINE_WIDTH = 200;
        private static final DataWatcherObject<IChatBaseComponent> DATA_TEXT_ID = DataWatcher.<IChatBaseComponent>defineId(Display.TextDisplay.class, DataWatcherRegistry.COMPONENT);
        public static final DataWatcherObject<Integer> DATA_LINE_WIDTH_ID = DataWatcher.<Integer>defineId(Display.TextDisplay.class, DataWatcherRegistry.INT);
        public static final DataWatcherObject<Integer> DATA_BACKGROUND_COLOR_ID = DataWatcher.<Integer>defineId(Display.TextDisplay.class, DataWatcherRegistry.INT);
        private static final DataWatcherObject<Byte> DATA_TEXT_OPACITY_ID = DataWatcher.<Byte>defineId(Display.TextDisplay.class, DataWatcherRegistry.BYTE);
        private static final DataWatcherObject<Byte> DATA_STYLE_FLAGS_ID = DataWatcher.<Byte>defineId(Display.TextDisplay.class, DataWatcherRegistry.BYTE);
        private static final IntSet TEXT_RENDER_STATE_IDS = IntSet.of(new int[]{Display.TextDisplay.DATA_TEXT_ID.id(), Display.TextDisplay.DATA_LINE_WIDTH_ID.id(), Display.TextDisplay.DATA_BACKGROUND_COLOR_ID.id(), Display.TextDisplay.DATA_TEXT_OPACITY_ID.id(), Display.TextDisplay.DATA_STYLE_FLAGS_ID.id()});
        @Nullable
        private Display.TextDisplay.CachedInfo clientDisplayCache;
        @Nullable
        private Display.TextDisplay.e textRenderState;

        public TextDisplay(EntityTypes<?> entitytypes, World world) {
            super(entitytypes, world);
        }

        @Override
        protected void defineSynchedData(DataWatcher.a datawatcher_a) {
            super.defineSynchedData(datawatcher_a);
            datawatcher_a.define(Display.TextDisplay.DATA_TEXT_ID, IChatBaseComponent.empty());
            datawatcher_a.define(Display.TextDisplay.DATA_LINE_WIDTH_ID, 200);
            datawatcher_a.define(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, 1073741824);
            datawatcher_a.define(Display.TextDisplay.DATA_TEXT_OPACITY_ID, -1);
            datawatcher_a.define(Display.TextDisplay.DATA_STYLE_FLAGS_ID, (byte) 0);
        }

        @Override
        public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
            super.onSyncedDataUpdated(datawatcherobject);
            if (Display.TextDisplay.TEXT_RENDER_STATE_IDS.contains(datawatcherobject.id())) {
                this.updateRenderState = true;
            }

        }

        public IChatBaseComponent getText() {
            return (IChatBaseComponent) this.entityData.get(Display.TextDisplay.DATA_TEXT_ID);
        }

        public void setText(IChatBaseComponent ichatbasecomponent) {
            this.entityData.set(Display.TextDisplay.DATA_TEXT_ID, ichatbasecomponent);
        }

        public int getLineWidth() {
            return (Integer) this.entityData.get(Display.TextDisplay.DATA_LINE_WIDTH_ID);
        }

        private void setLineWidth(int i) {
            this.entityData.set(Display.TextDisplay.DATA_LINE_WIDTH_ID, i);
        }

        public byte getTextOpacity() {
            return (Byte) this.entityData.get(Display.TextDisplay.DATA_TEXT_OPACITY_ID);
        }

        public void setTextOpacity(byte b0) {
            this.entityData.set(Display.TextDisplay.DATA_TEXT_OPACITY_ID, b0);
        }

        public int getBackgroundColor() {
            return (Integer) this.entityData.get(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID);
        }

        private void setBackgroundColor(int i) {
            this.entityData.set(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, i);
        }

        public byte getFlags() {
            return (Byte) this.entityData.get(Display.TextDisplay.DATA_STYLE_FLAGS_ID);
        }

        public void setFlags(byte b0) {
            this.entityData.set(Display.TextDisplay.DATA_STYLE_FLAGS_ID, b0);
        }

        private static byte loadFlag(byte b0, NBTTagCompound nbttagcompound, String s, byte b1) {
            return nbttagcompound.getBooleanOr(s, false) ? (byte) (b0 | b1) : b0;
        }

        @Override
        protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
            super.readAdditionalSaveData(nbttagcompound);
            this.setLineWidth(nbttagcompound.getIntOr("line_width", 200));
            this.setTextOpacity(nbttagcompound.getByteOr("text_opacity", (byte) -1));
            this.setBackgroundColor(nbttagcompound.getIntOr("background", 1073741824));
            byte b0 = loadFlag((byte) 0, nbttagcompound, "shadow", (byte) 1);

            b0 = loadFlag(b0, nbttagcompound, "see_through", (byte) 2);
            b0 = loadFlag(b0, nbttagcompound, "default_background", (byte) 4);
            Optional<Display.TextDisplay.Align> optional = nbttagcompound.read("alignment", Display.TextDisplay.Align.CODEC);

            if (optional.isPresent()) {
                byte b1;

                switch (((Display.TextDisplay.Align) optional.get()).ordinal()) {
                    case 0:
                        b1 = b0;
                        break;
                    case 1:
                        b1 = (byte) (b0 | 8);
                        break;
                    case 2:
                        b1 = (byte) (b0 | 16);
                        break;
                    default:
                        throw new MatchException((String) null, (Throwable) null);
                }

                b0 = b1;
            }

            this.setFlags(b0);
            NBTBase nbtbase = nbttagcompound.get("text");

            if (nbtbase != null) {
                RegistryOps<NBTBase> registryops = this.registryAccess().<NBTBase>createSerializationContext(DynamicOpsNBT.INSTANCE);

                try {
                    IChatBaseComponent ichatbasecomponent = (IChatBaseComponent) ComponentSerialization.CODEC.parse(registryops, nbtbase).getOrThrow();

                    if (ichatbasecomponent != null) {
                        World world = this.level();

                        if (world instanceof WorldServer) {
                            WorldServer worldserver = (WorldServer) world;
                            CommandListenerWrapper commandlistenerwrapper = this.createCommandSourceStackForNameResolution(worldserver).withPermission(2);
                            IChatBaseComponent ichatbasecomponent1 = ChatComponentUtils.updateForEntity(commandlistenerwrapper, ichatbasecomponent, this, 0);

                            this.setText(ichatbasecomponent1);
                            return;
                        }
                    }

                    this.setText(IChatBaseComponent.empty());
                } catch (Exception exception) {
                    Display.LOGGER.warn("Failed to parse display entity text {}", nbtbase, exception);
                }
            }

        }

        private static void storeFlag(byte b0, NBTTagCompound nbttagcompound, String s, byte b1) {
            nbttagcompound.putBoolean(s, (b0 & b1) != 0);
        }

        @Override
        protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
            super.addAdditionalSaveData(nbttagcompound);
            RegistryOps<NBTBase> registryops = this.registryAccess().<NBTBase>createSerializationContext(DynamicOpsNBT.INSTANCE);

            nbttagcompound.store("text", ComponentSerialization.CODEC, registryops, this.getText());
            nbttagcompound.putInt("line_width", this.getLineWidth());
            nbttagcompound.putInt("background", this.getBackgroundColor());
            nbttagcompound.putByte("text_opacity", this.getTextOpacity());
            byte b0 = this.getFlags();

            storeFlag(b0, nbttagcompound, "shadow", (byte) 1);
            storeFlag(b0, nbttagcompound, "see_through", (byte) 2);
            storeFlag(b0, nbttagcompound, "default_background", (byte) 4);
            nbttagcompound.store("alignment", Display.TextDisplay.Align.CODEC, getAlign(b0));
        }

        @Override
        protected void updateRenderSubState(boolean flag, float f) {
            if (flag && this.textRenderState != null) {
                this.textRenderState = this.createInterpolatedTextRenderState(this.textRenderState, f);
            } else {
                this.textRenderState = this.createFreshTextRenderState();
            }

            this.clientDisplayCache = null;
        }

        @Nullable
        public Display.TextDisplay.e textRenderState() {
            return this.textRenderState;
        }

        private Display.TextDisplay.e createFreshTextRenderState() {
            return new Display.TextDisplay.e(this.getText(), this.getLineWidth(), Display.IntInterpolator.constant(this.getTextOpacity()), Display.IntInterpolator.constant(this.getBackgroundColor()), this.getFlags());
        }

        private Display.TextDisplay.e createInterpolatedTextRenderState(Display.TextDisplay.e display_textdisplay_e, float f) {
            int i = display_textdisplay_e.backgroundColor.get(f);
            int j = display_textdisplay_e.textOpacity.get(f);

            return new Display.TextDisplay.e(this.getText(), this.getLineWidth(), new Display.i(j, this.getTextOpacity()), new Display.ColorInterpolator(i, this.getBackgroundColor()), this.getFlags());
        }

        public Display.TextDisplay.CachedInfo cacheDisplay(Display.TextDisplay.LineSplitter display_textdisplay_linesplitter) {
            if (this.clientDisplayCache == null) {
                if (this.textRenderState != null) {
                    this.clientDisplayCache = display_textdisplay_linesplitter.split(this.textRenderState.text(), this.textRenderState.lineWidth());
                } else {
                    this.clientDisplayCache = new Display.TextDisplay.CachedInfo(List.of(), 0);
                }
            }

            return this.clientDisplayCache;
        }

        public static Display.TextDisplay.Align getAlign(byte b0) {
            return (b0 & 8) != 0 ? Display.TextDisplay.Align.LEFT : ((b0 & 16) != 0 ? Display.TextDisplay.Align.RIGHT : Display.TextDisplay.Align.CENTER);
        }

        public static enum Align implements INamable {

            CENTER("center"), LEFT("left"), RIGHT("right");

            public static final Codec<Display.TextDisplay.Align> CODEC = INamable.<Display.TextDisplay.Align>fromEnum(Display.TextDisplay.Align::values);
            private final String name;

            private Align(final String s) {
                this.name = s;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }

        public static record CachedLine(FormattedString contents, int width) {

        }

        public static record CachedInfo(List<Display.TextDisplay.CachedLine> lines, int width) {

        }

        public static record e(IChatBaseComponent text, int lineWidth, Display.IntInterpolator textOpacity, Display.IntInterpolator backgroundColor, byte flags) {

        }

        @FunctionalInterface
        public interface LineSplitter {

            Display.TextDisplay.CachedInfo split(IChatBaseComponent ichatbasecomponent, int i);
        }
    }

    @FunctionalInterface
    public interface GenericInterpolator<T> {

        static <T> Display.GenericInterpolator<T> constant(T t0) {
            return (f) -> {
                return t0;
            };
        }

        T get(float f);
    }

    private static record l(Transformation previous, Transformation current) implements Display.GenericInterpolator<Transformation> {

        @Override
        public Transformation get(float f) {
            return (double) f >= 1.0D ? this.current : this.previous.slerp(this.current, f);
        }
    }

    @FunctionalInterface
    public interface IntInterpolator {

        static Display.IntInterpolator constant(int i) {
            return (f) -> {
                return i;
            };
        }

        int get(float f);
    }

    private static record i(int previous, int current) implements Display.IntInterpolator {

        @Override
        public int get(float f) {
            return MathHelper.lerpInt(f, this.previous, this.current);
        }
    }

    private static record ColorInterpolator(int previous, int current) implements Display.IntInterpolator {

        @Override
        public int get(float f) {
            return ARGB.lerp(f, this.previous, this.current);
        }
    }

    @FunctionalInterface
    public interface FloatInterpolator {

        static Display.FloatInterpolator constant(float f) {
            return (f1) -> {
                return f;
            };
        }

        float get(float f);
    }

    private static record h(float previous, float current) implements Display.FloatInterpolator {

        @Override
        public float get(float f) {
            return MathHelper.lerp(f, this.previous, this.current);
        }
    }
}
