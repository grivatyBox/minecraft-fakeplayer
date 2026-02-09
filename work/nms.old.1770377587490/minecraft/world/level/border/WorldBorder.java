package net.minecraft.world.level.border;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.MathHelper;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.saveddata.PersistentBase;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.OperatorBoolean;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class WorldBorder extends PersistentBase {

    public static final double MAX_SIZE = (double) 5.999997E7F;
    public static final double MAX_CENTER_COORDINATE = 2.9999984E7D;
    public static final Codec<WorldBorder> CODEC = WorldBorder.c.CODEC.xmap(WorldBorder.c::toWorldBorder, WorldBorder.c::new);
    public static final SavedDataType<WorldBorder> TYPE = new SavedDataType<WorldBorder>("world_border", (persistentbase_a) -> {
        return WorldBorder.c.DEFAULT.toWorldBorder();
    }, (persistentbase_a) -> {
        return WorldBorder.CODEC;
    }, DataFixTypes.SAVED_DATA_WORLD_BORDER);
    private final List<IWorldBorderListener> listeners = Lists.newArrayList();
    double damagePerBlock = 0.2D;
    double safeZone = 5.0D;
    int warningTime = 15;
    int warningBlocks = 5;
    double centerX;
    double centerZ;
    int absoluteMaxSize = 29999984;
    WorldBorder.a extent = new WorldBorder.d((double) 5.999997E7F);

    public WorldBorder() {}

    public boolean isWithinBounds(BlockPosition blockposition) {
        return this.isWithinBounds((double) blockposition.getX(), (double) blockposition.getZ());
    }

    public boolean isWithinBounds(Vec3D vec3d) {
        return this.isWithinBounds(vec3d.x, vec3d.z);
    }

    public boolean isWithinBounds(ChunkCoordIntPair chunkcoordintpair) {
        return this.isWithinBounds((double) chunkcoordintpair.getMinBlockX(), (double) chunkcoordintpair.getMinBlockZ()) && this.isWithinBounds((double) chunkcoordintpair.getMaxBlockX(), (double) chunkcoordintpair.getMaxBlockZ());
    }

    public boolean isWithinBounds(AxisAlignedBB axisalignedbb) {
        return this.isWithinBounds(axisalignedbb.minX, axisalignedbb.minZ, axisalignedbb.maxX - (double) 1.0E-5F, axisalignedbb.maxZ - (double) 1.0E-5F);
    }

    private boolean isWithinBounds(double d0, double d1, double d2, double d3) {
        return this.isWithinBounds(d0, d1) && this.isWithinBounds(d2, d3);
    }

    public boolean isWithinBounds(double d0, double d1) {
        return this.isWithinBounds(d0, d1, 0.0D);
    }

    public boolean isWithinBounds(double d0, double d1, double d2) {
        return d0 >= this.getMinX() - d2 && d0 < this.getMaxX() + d2 && d1 >= this.getMinZ() - d2 && d1 < this.getMaxZ() + d2;
    }

    public BlockPosition clampToBounds(BlockPosition blockposition) {
        return this.clampToBounds((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ());
    }

    public BlockPosition clampToBounds(Vec3D vec3d) {
        return this.clampToBounds(vec3d.x(), vec3d.y(), vec3d.z());
    }

    public BlockPosition clampToBounds(double d0, double d1, double d2) {
        return BlockPosition.containing(this.clampVec3ToBound(d0, d1, d2));
    }

    public Vec3D clampVec3ToBound(Vec3D vec3d) {
        return this.clampVec3ToBound(vec3d.x, vec3d.y, vec3d.z);
    }

    public Vec3D clampVec3ToBound(double d0, double d1, double d2) {
        return new Vec3D(MathHelper.clamp(d0, this.getMinX(), this.getMaxX() - (double) 1.0E-5F), d1, MathHelper.clamp(d2, this.getMinZ(), this.getMaxZ() - (double) 1.0E-5F));
    }

    public double getDistanceToBorder(Entity entity) {
        return this.getDistanceToBorder(entity.getX(), entity.getZ());
    }

    public VoxelShape getCollisionShape() {
        return this.extent.getCollisionShape();
    }

    public double getDistanceToBorder(double d0, double d1) {
        double d2 = d1 - this.getMinZ();
        double d3 = this.getMaxZ() - d1;
        double d4 = d0 - this.getMinX();
        double d5 = this.getMaxX() - d0;
        double d6 = Math.min(d4, d5);

        d6 = Math.min(d6, d2);
        return Math.min(d6, d3);
    }

    public boolean isInsideCloseToBorder(Entity entity, AxisAlignedBB axisalignedbb) {
        double d0 = Math.max(MathHelper.absMax(axisalignedbb.getXsize(), axisalignedbb.getZsize()), 1.0D);

        return this.getDistanceToBorder(entity) < d0 * 2.0D && this.isWithinBounds(entity.getX(), entity.getZ(), d0);
    }

    public BorderStatus getStatus() {
        return this.extent.getStatus();
    }

    public double getMinX() {
        return this.extent.getMinX();
    }

    public double getMinZ() {
        return this.extent.getMinZ();
    }

    public double getMaxX() {
        return this.extent.getMaxX();
    }

    public double getMaxZ() {
        return this.extent.getMaxZ();
    }

    public double getCenterX() {
        return this.centerX;
    }

    public double getCenterZ() {
        return this.centerZ;
    }

    public void setCenter(double d0, double d1) {
        this.centerX = d0;
        this.centerZ = d1;
        this.extent.onCenterChange();
        this.setDirty();

        for (IWorldBorderListener iworldborderlistener : this.getListeners()) {
            iworldborderlistener.onSetCenter(this, d0, d1);
        }

    }

    public double getSize() {
        return this.extent.getSize();
    }

    public long getLerpTime() {
        return this.extent.getLerpTime();
    }

    public double getLerpTarget() {
        return this.extent.getLerpTarget();
    }

    public void setSize(double d0) {
        this.extent = new WorldBorder.d(d0);
        this.setDirty();

        for (IWorldBorderListener iworldborderlistener : this.getListeners()) {
            iworldborderlistener.onSetSize(this, d0);
        }

    }

    public void lerpSizeBetween(double d0, double d1, long i) {
        this.extent = (WorldBorder.a) (d0 == d1 ? new WorldBorder.d(d1) : new WorldBorder.b(d0, d1, i));
        this.setDirty();

        for (IWorldBorderListener iworldborderlistener : this.getListeners()) {
            iworldborderlistener.onLerpSize(this, d0, d1, i);
        }

    }

    protected List<IWorldBorderListener> getListeners() {
        return Lists.newArrayList(this.listeners);
    }

    public void addListener(IWorldBorderListener iworldborderlistener) {
        if (listeners.contains(iworldborderlistener)) return; // CraftBukkit
        this.listeners.add(iworldborderlistener);
    }

    public void removeListener(IWorldBorderListener iworldborderlistener) {
        this.listeners.remove(iworldborderlistener);
    }

    public void setAbsoluteMaxSize(int i) {
        this.absoluteMaxSize = i;
        this.extent.onAbsoluteMaxSizeChange();
    }

    public int getAbsoluteMaxSize() {
        return this.absoluteMaxSize;
    }

    public double getSafeZone() {
        return this.safeZone;
    }

    public void setSafeZone(double d0) {
        this.safeZone = d0;
        this.setDirty();

        for (IWorldBorderListener iworldborderlistener : this.getListeners()) {
            iworldborderlistener.onSetSafeZone(this, d0);
        }

    }

    public double getDamagePerBlock() {
        return this.damagePerBlock;
    }

    public void setDamagePerBlock(double d0) {
        this.damagePerBlock = d0;
        this.setDirty();

        for (IWorldBorderListener iworldborderlistener : this.getListeners()) {
            iworldborderlistener.onSetDamagePerBlock(this, d0);
        }

    }

    public double getLerpSpeed() {
        return this.extent.getLerpSpeed();
    }

    public int getWarningTime() {
        return this.warningTime;
    }

    public void setWarningTime(int i) {
        this.warningTime = i;
        this.setDirty();

        for (IWorldBorderListener iworldborderlistener : this.getListeners()) {
            iworldborderlistener.onSetWarningTime(this, i);
        }

    }

    public int getWarningBlocks() {
        return this.warningBlocks;
    }

    public void setWarningBlocks(int i) {
        this.warningBlocks = i;
        this.setDirty();

        for (IWorldBorderListener iworldborderlistener : this.getListeners()) {
            iworldborderlistener.onSetWarningBlocks(this, i);
        }

    }

    public void tick() {
        this.extent = this.extent.update();
    }

    public void applySettings(WorldBorder.c worldborder_c) {
        this.setCenter(worldborder_c.centerX(), worldborder_c.centerZ());
        this.setDamagePerBlock(worldborder_c.damagePerBlock());
        this.setSafeZone(worldborder_c.safeZone());
        this.setWarningBlocks(worldborder_c.warningBlocks());
        this.setWarningTime(worldborder_c.warningTime());
        if (worldborder_c.lerpTime() > 0L) {
            this.lerpSizeBetween(worldborder_c.size(), worldborder_c.lerpTarget(), worldborder_c.lerpTime());
        } else {
            this.setSize(worldborder_c.size());
        }

    }

    private class b implements WorldBorder.a {

        private final double from;
        private final double to;
        private final long lerpEnd;
        private final long lerpBegin;
        private final double lerpDuration;

        b(final double d0, final double d1, final long i) {
            this.from = d0;
            this.to = d1;
            this.lerpDuration = (double) i;
            this.lerpBegin = SystemUtils.getMillis();
            this.lerpEnd = this.lerpBegin + i;
        }

        @Override
        public double getMinX() {
            return MathHelper.clamp(WorldBorder.this.getCenterX() - this.getSize() / 2.0D, (double) (-WorldBorder.this.absoluteMaxSize), (double) WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getMinZ() {
            return MathHelper.clamp(WorldBorder.this.getCenterZ() - this.getSize() / 2.0D, (double) (-WorldBorder.this.absoluteMaxSize), (double) WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getMaxX() {
            return MathHelper.clamp(WorldBorder.this.getCenterX() + this.getSize() / 2.0D, (double) (-WorldBorder.this.absoluteMaxSize), (double) WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getMaxZ() {
            return MathHelper.clamp(WorldBorder.this.getCenterZ() + this.getSize() / 2.0D, (double) (-WorldBorder.this.absoluteMaxSize), (double) WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getSize() {
            double d0 = (double) (SystemUtils.getMillis() - this.lerpBegin) / this.lerpDuration;

            return d0 < 1.0D ? MathHelper.lerp(d0, this.from, this.to) : this.to;
        }

        @Override
        public double getLerpSpeed() {
            return Math.abs(this.from - this.to) / (double) (this.lerpEnd - this.lerpBegin);
        }

        @Override
        public long getLerpTime() {
            return this.lerpEnd - SystemUtils.getMillis();
        }

        @Override
        public double getLerpTarget() {
            return this.to;
        }

        @Override
        public BorderStatus getStatus() {
            return this.to < this.from ? BorderStatus.SHRINKING : BorderStatus.GROWING;
        }

        @Override
        public void onCenterChange() {}

        @Override
        public void onAbsoluteMaxSizeChange() {}

        @Override
        public WorldBorder.a update() {
            if (this.getLerpTime() <= 0L) {
                WorldBorder.this.setDirty();
                return WorldBorder.this.new d(this.to);
            } else {
                return this;
            }
        }

        @Override
        public VoxelShape getCollisionShape() {
            return VoxelShapes.join(VoxelShapes.INFINITY, VoxelShapes.box(Math.floor(this.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(this.getMinZ()), Math.ceil(this.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(this.getMaxZ())), OperatorBoolean.ONLY_FIRST);
        }
    }

    private class d implements WorldBorder.a {

        private final double size;
        private double minX;
        private double minZ;
        private double maxX;
        private double maxZ;
        private VoxelShape shape;

        public d(final double d0) {
            this.size = d0;
            this.updateBox();
        }

        @Override
        public double getMinX() {
            return this.minX;
        }

        @Override
        public double getMaxX() {
            return this.maxX;
        }

        @Override
        public double getMinZ() {
            return this.minZ;
        }

        @Override
        public double getMaxZ() {
            return this.maxZ;
        }

        @Override
        public double getSize() {
            return this.size;
        }

        @Override
        public BorderStatus getStatus() {
            return BorderStatus.STATIONARY;
        }

        @Override
        public double getLerpSpeed() {
            return 0.0D;
        }

        @Override
        public long getLerpTime() {
            return 0L;
        }

        @Override
        public double getLerpTarget() {
            return this.size;
        }

        private void updateBox() {
            this.minX = MathHelper.clamp(WorldBorder.this.getCenterX() - this.size / 2.0D, (double) (-WorldBorder.this.absoluteMaxSize), (double) WorldBorder.this.absoluteMaxSize);
            this.minZ = MathHelper.clamp(WorldBorder.this.getCenterZ() - this.size / 2.0D, (double) (-WorldBorder.this.absoluteMaxSize), (double) WorldBorder.this.absoluteMaxSize);
            this.maxX = MathHelper.clamp(WorldBorder.this.getCenterX() + this.size / 2.0D, (double) (-WorldBorder.this.absoluteMaxSize), (double) WorldBorder.this.absoluteMaxSize);
            this.maxZ = MathHelper.clamp(WorldBorder.this.getCenterZ() + this.size / 2.0D, (double) (-WorldBorder.this.absoluteMaxSize), (double) WorldBorder.this.absoluteMaxSize);
            this.shape = VoxelShapes.join(VoxelShapes.INFINITY, VoxelShapes.box(Math.floor(this.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(this.getMinZ()), Math.ceil(this.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(this.getMaxZ())), OperatorBoolean.ONLY_FIRST);
        }

        @Override
        public void onAbsoluteMaxSizeChange() {
            this.updateBox();
        }

        @Override
        public void onCenterChange() {
            this.updateBox();
        }

        @Override
        public WorldBorder.a update() {
            return this;
        }

        @Override
        public VoxelShape getCollisionShape() {
            return this.shape;
        }
    }

    public static record c(double centerX, double centerZ, double damagePerBlock, double safeZone, int warningBlocks, int warningTime, double size, long lerpTime, double lerpTarget) {

        public static final WorldBorder.c DEFAULT = new WorldBorder.c(0.0D, 0.0D, 0.2D, 5.0D, 5, 15, (double) 5.999997E7F, 0L, 0.0D);
        public static final Codec<WorldBorder.c> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.doubleRange(-2.9999984E7D, 2.9999984E7D).fieldOf("center_x").forGetter(WorldBorder.c::centerX), Codec.doubleRange(-2.9999984E7D, 2.9999984E7D).fieldOf("center_z").forGetter(WorldBorder.c::centerZ), Codec.DOUBLE.fieldOf("damage_per_block").forGetter(WorldBorder.c::damagePerBlock), Codec.DOUBLE.fieldOf("safe_zone").forGetter(WorldBorder.c::safeZone), Codec.INT.fieldOf("warning_blocks").forGetter(WorldBorder.c::warningBlocks), Codec.INT.fieldOf("warning_time").forGetter(WorldBorder.c::warningTime), Codec.DOUBLE.fieldOf("size").forGetter(WorldBorder.c::size), Codec.LONG.fieldOf("lerp_time").forGetter(WorldBorder.c::lerpTime), Codec.DOUBLE.fieldOf("lerp_target").forGetter(WorldBorder.c::lerpTarget)).apply(instance, WorldBorder.c::new);
        });

        public c(WorldBorder worldborder) {
            this(worldborder.centerX, worldborder.centerZ, worldborder.damagePerBlock, worldborder.safeZone, worldborder.warningBlocks, worldborder.warningTime, worldborder.extent.getSize(), worldborder.extent.getLerpTime(), worldborder.extent.getLerpTarget());
        }

        public WorldBorder toWorldBorder() {
            WorldBorder worldborder = new WorldBorder();

            worldborder.applySettings(this);
            return worldborder;
        }
    }

    private interface a {

        double getMinX();

        double getMaxX();

        double getMinZ();

        double getMaxZ();

        double getSize();

        double getLerpSpeed();

        long getLerpTime();

        double getLerpTarget();

        BorderStatus getStatus();

        void onAbsoluteMaxSizeChange();

        void onCenterChange();

        WorldBorder.a update();

        VoxelShape getCollisionShape();
    }
}
