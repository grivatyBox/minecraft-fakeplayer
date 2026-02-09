package net.minecraft.world.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.protocol.game.PacketPlayOutAttachEntity;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.entity.decoration.EntityLeash;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.World;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public interface Leashable {

    String LEASH_TAG = "leash";
    double LEASH_TOO_FAR_DIST = 12.0D;
    double LEASH_ELASTIC_DIST = 6.0D;
    double MAXIMUM_ALLOWED_LEASHED_DIST = 16.0D;
    Vec3D AXIS_SPECIFIC_ELASTICITY = new Vec3D(0.8D, 0.2D, 0.8D);
    float SPRING_DAMPENING = 0.7F;
    double TORSIONAL_ELASTICITY = 10.0D;
    double STIFFNESS = 0.11D;
    List<Vec3D> ENTITY_ATTACHMENT_POINT = ImmutableList.of(new Vec3D(0.0D, 0.5D, 0.5D));
    List<Vec3D> LEASHER_ATTACHMENT_POINT = ImmutableList.of(new Vec3D(0.0D, 0.5D, 0.0D));
    List<Vec3D> SHARED_QUAD_ATTACHMENT_POINTS = ImmutableList.of(new Vec3D(-0.5D, 0.5D, 0.5D), new Vec3D(-0.5D, 0.5D, -0.5D), new Vec3D(0.5D, 0.5D, -0.5D), new Vec3D(0.5D, 0.5D, 0.5D));

    @Nullable
    Leashable.a getLeashData();

    void setLeashData(@Nullable Leashable.a leashable_a);

    default boolean isLeashed() {
        return this.getLeashData() != null && this.getLeashData().leashHolder != null;
    }

    default boolean mayBeLeashed() {
        return this.getLeashData() != null;
    }

    default boolean canHaveALeashAttachedTo(Entity entity) {
        return this == entity ? false : (this.leashDistanceTo(entity) > this.leashSnapDistance() ? false : this.canBeLeashed());
    }

    default double leashDistanceTo(Entity entity) {
        return entity.getBoundingBox().getCenter().distanceTo(((Entity) this).getBoundingBox().getCenter());
    }

    default boolean canBeLeashed() {
        return true;
    }

    default void setDelayedLeashHolderId(int i) {
        this.setLeashData(new Leashable.a(i));
        dropLeash((Entity) this, false, false);
    }

    default void readLeashData(ValueInput valueinput) {
        Leashable.a leashable_a = (Leashable.a) valueinput.read("leash", Leashable.a.CODEC).orElse((Object) null);

        if (this.getLeashData() != null && leashable_a == null) {
            this.removeLeash();
        }

        this.setLeashData(leashable_a);
    }

    default void writeLeashData(ValueOutput valueoutput, @Nullable Leashable.a leashable_a) {
        valueoutput.storeNullable("leash", Leashable.a.CODEC, leashable_a);
    }

    private static <E extends Entity & Leashable> void restoreLeashFromSave(E e0, Leashable.a leashable_a) {
        if (leashable_a.delayedLeashInfo != null) {
            World world = e0.level();

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;
                Optional<UUID> optional = leashable_a.delayedLeashInfo.left();
                Optional<BlockPosition> optional1 = leashable_a.delayedLeashInfo.right();

                if (optional.isPresent()) {
                    Entity entity = worldserver.getEntity((UUID) optional.get());

                    if (entity != null) {
                        setLeashedTo(e0, entity, true);
                        return;
                    }
                } else if (optional1.isPresent()) {
                    setLeashedTo(e0, EntityLeash.getOrCreateKnot(worldserver, (BlockPosition) optional1.get()), true);
                    return;
                }

                if (e0.tickCount > 100) {
                    e0.spawnAtLocation(worldserver, (IMaterial) Items.LEAD);
                    ((Leashable) e0).setLeashData((Leashable.a) null);
                }
            }
        }

    }

    default void dropLeash() {
        dropLeash((Entity) this, true, true);
    }

    default void removeLeash() {
        dropLeash((Entity) this, true, false);
    }

    default void onLeashRemoved() {}

    private static <E extends Entity & Leashable> void dropLeash(E e0, boolean flag, boolean flag1) {
        Leashable.a leashable_a = ((Leashable) e0).getLeashData();

        if (leashable_a != null && leashable_a.leashHolder != null) {
            ((Leashable) e0).setLeashData((Leashable.a) null);
            ((Leashable) e0).onLeashRemoved();
            World world = e0.level();

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                if (flag1) {
                    e0.spawnAtLocation(worldserver, (IMaterial) Items.LEAD);
                }

                if (flag) {
                    worldserver.getChunkSource().sendToTrackingPlayers(e0, new PacketPlayOutAttachEntity(e0, (Entity) null));
                }

                leashable_a.leashHolder.notifyLeasheeRemoved(e0);
            }
        }

    }

    static <E extends Entity & Leashable> void tickLeash(WorldServer worldserver, E e0) {
        Leashable.a leashable_a = ((Leashable) e0).getLeashData();

        if (leashable_a != null && leashable_a.delayedLeashInfo != null) {
            restoreLeashFromSave(e0, leashable_a);
        }

        if (leashable_a != null && leashable_a.leashHolder != null) {
            if (!e0.canInteractWithLevel() || !leashable_a.leashHolder.canInteractWithLevel()) {
                if (worldserver.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    ((Leashable) e0).dropLeash();
                } else {
                    ((Leashable) e0).removeLeash();
                }
            }

            Entity entity = ((Leashable) e0).getLeashHolder();

            if (entity != null && entity.level() == e0.level()) {
                double d0 = ((Leashable) e0).leashDistanceTo(entity);

                ((Leashable) e0).whenLeashedTo(entity);
                if (d0 > ((Leashable) e0).leashSnapDistance()) {
                    worldserver.playSound((Entity) null, entity.getX(), entity.getY(), entity.getZ(), SoundEffects.LEAD_BREAK, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                    ((Leashable) e0).leashTooFarBehaviour();
                } else if (d0 > ((Leashable) e0).leashElasticDistance() - (double) entity.getBbWidth() - (double) e0.getBbWidth() && ((Leashable) e0).checkElasticInteractions(entity, leashable_a)) {
                    ((Leashable) e0).onElasticLeashPull();
                } else {
                    ((Leashable) e0).closeRangeLeashBehaviour(entity);
                }

                e0.setYRot((float) ((double) e0.getYRot() - leashable_a.angularMomentum));
                leashable_a.angularMomentum *= (double) angularFriction(e0);
            }

        }
    }

    default void onElasticLeashPull() {
        Entity entity = (Entity) this;

        entity.checkFallDistanceAccumulation();
    }

    default double leashSnapDistance() {
        return 12.0D;
    }

    default double leashElasticDistance() {
        return 6.0D;
    }

    static <E extends Entity & Leashable> float angularFriction(E e0) {
        return e0.onGround() ? e0.level().getBlockState(e0.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.91F : (e0.isInLiquid() ? 0.8F : 0.91F);
    }

    default void whenLeashedTo(Entity entity) {
        entity.notifyLeashHolder(this);
    }

    default void leashTooFarBehaviour() {
        this.dropLeash();
    }

    default void closeRangeLeashBehaviour(Entity entity) {}

    default boolean checkElasticInteractions(Entity entity, Leashable.a leashable_a) {
        boolean flag = entity.supportQuadLeashAsHolder() && this.supportQuadLeash();
        List<Leashable.b> list = computeElasticInteraction((Entity) this, entity, flag ? Leashable.SHARED_QUAD_ATTACHMENT_POINTS : Leashable.ENTITY_ATTACHMENT_POINT, flag ? Leashable.SHARED_QUAD_ATTACHMENT_POINTS : Leashable.LEASHER_ATTACHMENT_POINT);

        if (list.isEmpty()) {
            return false;
        } else {
            Leashable.b leashable_b = Leashable.b.accumulate(list).scale(flag ? 0.25D : 1.0D);

            leashable_a.angularMomentum += 10.0D * leashable_b.torque();
            Vec3D vec3d = getHolderMovement(entity).subtract(((Entity) this).getKnownMovement());

            ((Entity) this).addDeltaMovement(leashable_b.force().multiply(Leashable.AXIS_SPECIFIC_ELASTICITY).add(vec3d.scale(0.11D)));
            return true;
        }
    }

    private static Vec3D getHolderMovement(Entity entity) {
        if (entity instanceof EntityInsentient entityinsentient) {
            if (entityinsentient.isNoAi()) {
                return Vec3D.ZERO;
            }
        }

        return entity.getKnownMovement();
    }

    private static <E extends Entity & Leashable> List<Leashable.b> computeElasticInteraction(E e0, Entity entity, List<Vec3D> list, List<Vec3D> list1) {
        double d0 = ((Leashable) e0).leashElasticDistance();
        Vec3D vec3d = getHolderMovement(e0);
        float f = e0.getYRot() * ((float) Math.PI / 180F);
        Vec3D vec3d1 = new Vec3D((double) e0.getBbWidth(), (double) e0.getBbHeight(), (double) e0.getBbWidth());
        float f1 = entity.getYRot() * ((float) Math.PI / 180F);
        Vec3D vec3d2 = new Vec3D((double) entity.getBbWidth(), (double) entity.getBbHeight(), (double) entity.getBbWidth());
        List<Leashable.b> list2 = new ArrayList();

        for (int i = 0; i < list.size(); ++i) {
            Vec3D vec3d3 = ((Vec3D) list.get(i)).multiply(vec3d1).yRot(-f);
            Vec3D vec3d4 = e0.position().add(vec3d3);
            Vec3D vec3d5 = ((Vec3D) list1.get(i)).multiply(vec3d2).yRot(-f1);
            Vec3D vec3d6 = entity.position().add(vec3d5);
            Optional optional = computeDampenedSpringInteraction(vec3d6, vec3d4, d0, vec3d, vec3d3);

            Objects.requireNonNull(list2);
            optional.ifPresent(list2::add);
        }

        return list2;
    }

    private static Optional<Leashable.b> computeDampenedSpringInteraction(Vec3D vec3d, Vec3D vec3d1, double d0, Vec3D vec3d2, Vec3D vec3d3) {
        double d1 = vec3d1.distanceTo(vec3d);

        if (d1 < d0) {
            return Optional.empty();
        } else {
            Vec3D vec3d4 = vec3d.subtract(vec3d1).normalize().scale(d1 - d0);
            double d2 = Leashable.b.torqueFromForce(vec3d3, vec3d4);
            boolean flag = vec3d2.dot(vec3d4) >= 0.0D;

            if (flag) {
                vec3d4 = vec3d4.scale((double) 0.3F);
            }

            return Optional.of(new Leashable.b(vec3d4, d2));
        }
    }

    default boolean supportQuadLeash() {
        return false;
    }

    default Vec3D[] getQuadLeashOffsets() {
        return createQuadLeashOffsets((Entity) this, 0.0D, 0.5D, 0.5D, 0.5D);
    }

    static Vec3D[] createQuadLeashOffsets(Entity entity, double d0, double d1, double d2, double d3) {
        float f = entity.getBbWidth();
        double d4 = d0 * (double) f;
        double d5 = d1 * (double) f;
        double d6 = d2 * (double) f;
        double d7 = d3 * (double) entity.getBbHeight();

        return new Vec3D[]{new Vec3D(-d6, d7, d5 + d4), new Vec3D(-d6, d7, -d5 + d4), new Vec3D(d6, d7, -d5 + d4), new Vec3D(d6, d7, d5 + d4)};
    }

    default Vec3D getLeashOffset(float f) {
        return this.getLeashOffset();
    }

    default Vec3D getLeashOffset() {
        Entity entity = (Entity) this;

        return new Vec3D(0.0D, (double) entity.getEyeHeight(), (double) (entity.getBbWidth() * 0.4F));
    }

    default void setLeashedTo(Entity entity, boolean flag) {
        if (this != entity) {
            setLeashedTo((Entity) this, entity, flag);
        }
    }

    private static <E extends Entity & Leashable> void setLeashedTo(E e0, Entity entity, boolean flag) {
        Leashable.a leashable_a = ((Leashable) e0).getLeashData();

        if (leashable_a == null) {
            leashable_a = new Leashable.a(entity);
            ((Leashable) e0).setLeashData(leashable_a);
        } else {
            Entity entity1 = leashable_a.leashHolder;

            leashable_a.setLeashHolder(entity);
            if (entity1 != null && entity1 != entity) {
                entity1.notifyLeasheeRemoved(e0);
            }
        }

        if (flag) {
            World world = e0.level();

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                worldserver.getChunkSource().sendToTrackingPlayers(e0, new PacketPlayOutAttachEntity(e0, entity));
            }
        }

        if (e0.isPassenger()) {
            e0.stopRiding();
        }

    }

    @Nullable
    default Entity getLeashHolder() {
        return getLeashHolder((Entity) this);
    }

    @Nullable
    private static <E extends Entity & Leashable> Entity getLeashHolder(E e0) {
        Leashable.a leashable_a = ((Leashable) e0).getLeashData();

        if (leashable_a == null) {
            return null;
        } else {
            if (leashable_a.delayedLeashHolderId != 0 && e0.level().isClientSide()) {
                Entity entity = e0.level().getEntity(leashable_a.delayedLeashHolderId);

                if (entity instanceof Entity) {
                    leashable_a.setLeashHolder(entity);
                }
            }

            return leashable_a.leashHolder;
        }
    }

    static List<Leashable> leashableLeashedTo(Entity entity) {
        return leashableInArea(entity, (leashable) -> {
            return leashable.getLeashHolder() == entity;
        });
    }

    static List<Leashable> leashableInArea(Entity entity, Predicate<Leashable> predicate) {
        return leashableInArea(entity.level(), entity.getBoundingBox().getCenter(), predicate);
    }

    static List<Leashable> leashableInArea(World world, Vec3D vec3d, Predicate<Leashable> predicate) {
        double d0 = 32.0D;
        AxisAlignedBB axisalignedbb = AxisAlignedBB.ofSize(vec3d, 32.0D, 32.0D, 32.0D);
        Stream stream = world.getEntitiesOfClass(Entity.class, axisalignedbb, (entity) -> {
            boolean flag;

            if (entity instanceof Leashable leashable) {
                if (predicate.test(leashable)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }).stream();

        Objects.requireNonNull(Leashable.class);
        return stream.map(Leashable.class::cast).toList();
    }

    public static final class a {

        public static final Codec<Leashable.a> CODEC = Codec.xor(UUIDUtil.CODEC.fieldOf("UUID").codec(), BlockPosition.CODEC).xmap(Leashable.a::new, (leashable_a) -> {
            Entity entity = leashable_a.leashHolder;

            if (entity instanceof EntityLeash entityleash) {
                return Either.right(entityleash.getPos());
            } else {
                return leashable_a.leashHolder != null ? Either.left(leashable_a.leashHolder.getUUID()) : (Either) Objects.requireNonNull(leashable_a.delayedLeashInfo, "Invalid LeashData had no attachment");
            }
        });
        int delayedLeashHolderId;
        @Nullable
        public Entity leashHolder;
        @Nullable
        public Either<UUID, BlockPosition> delayedLeashInfo;
        public double angularMomentum;

        private a(Either<UUID, BlockPosition> either) {
            this.delayedLeashInfo = either;
        }

        a(Entity entity) {
            this.leashHolder = entity;
        }

        a(int i) {
            this.delayedLeashHolderId = i;
        }

        public void setLeashHolder(Entity entity) {
            this.leashHolder = entity;
            this.delayedLeashInfo = null;
            this.delayedLeashHolderId = 0;
        }
    }

    public static record b(Vec3D force, double torque) {

        static Leashable.b ZERO = new Leashable.b(Vec3D.ZERO, 0.0D);

        static double torqueFromForce(Vec3D vec3d, Vec3D vec3d1) {
            return vec3d.z * vec3d1.x - vec3d.x * vec3d1.z;
        }

        static Leashable.b accumulate(List<Leashable.b> list) {
            if (list.isEmpty()) {
                return Leashable.b.ZERO;
            } else {
                double d0 = 0.0D;
                double d1 = 0.0D;
                double d2 = 0.0D;
                double d3 = 0.0D;

                for (Leashable.b leashable_b : list) {
                    Vec3D vec3d = leashable_b.force;

                    d0 += vec3d.x;
                    d1 += vec3d.y;
                    d2 += vec3d.z;
                    d3 += leashable_b.torque;
                }

                return new Leashable.b(new Vec3D(d0, d1, d2), d3);
            }
        }

        public Leashable.b scale(double d0) {
            return new Leashable.b(this.force.scale(d0), this.torque * d0);
        }
    }
}
