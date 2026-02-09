package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockChest;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityChest;
import net.minecraft.world.level.block.entity.TileEntityContainer;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyChestType;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.Vec3D;
import org.apache.commons.lang3.function.TriConsumer;

public class TransportItemsBetweenContainers extends Behavior<EntityCreature> {

    public static final int TARGET_INTERACTION_TIME = 60;
    private static final int VISITED_POSITIONS_MEMORY_TIME = 6000;
    private static final int TRANSPORTED_ITEM_MAX_STACK_SIZE = 16;
    private static final int MAX_VISITED_POSITIONS = 10;
    private static final int MAX_UNREACHABLE_POSITIONS = 50;
    private static final int PASSENGER_MOB_TARGET_SEARCH_DISTANCE = 1;
    private static final int IDLE_COOLDOWN = 140;
    private static final double CLOSE_ENOUGH_TO_START_QUEUING_DISTANCE = 3.0D;
    private static final double CLOSE_ENOUGH_TO_START_INTERACTING_WITH_TARGET_DISTANCE = 0.5D;
    private static final double CLOSE_ENOUGH_TO_START_INTERACTING_WITH_TARGET_PATH_END_DISTANCE = 1.0D;
    private static final double CLOSE_ENOUGH_TO_CONTINUE_INTERACTING_WITH_TARGET = 2.0D;
    private final float speedModifier;
    private final int horizontalSearchDistance;
    private final int verticalSearchDistance;
    private final Predicate<IBlockData> sourceBlockType;
    private final Predicate<IBlockData> destinationBlockType;
    private final Predicate<TransportItemsBetweenContainers.d> shouldQueueForTarget;
    private final Consumer<EntityCreature> onStartTravelling;
    private final Map<TransportItemsBetweenContainers.a, TransportItemsBetweenContainers.b> onTargetInteractionActions;
    @Nullable
    private TransportItemsBetweenContainers.d target = null;
    private TransportItemsBetweenContainers.c state;
    @Nullable
    private TransportItemsBetweenContainers.a interactionState;
    private int ticksSinceReachingTarget;

    public TransportItemsBetweenContainers(float f, Predicate<IBlockData> predicate, Predicate<IBlockData> predicate1, int i, int j, Map<TransportItemsBetweenContainers.a, TransportItemsBetweenContainers.b> map, Consumer<EntityCreature> consumer, Predicate<TransportItemsBetweenContainers.d> predicate2) {
        super(ImmutableMap.of(MemoryModuleType.VISITED_BLOCK_POSITIONS, MemoryStatus.REGISTERED, MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS, MemoryStatus.REGISTERED, MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT));
        this.speedModifier = f;
        this.sourceBlockType = predicate;
        this.destinationBlockType = predicate1;
        this.horizontalSearchDistance = i;
        this.verticalSearchDistance = j;
        this.onStartTravelling = consumer;
        this.shouldQueueForTarget = predicate2;
        this.onTargetInteractionActions = map;
        this.state = TransportItemsBetweenContainers.c.TRAVELLING;
    }

    protected void start(WorldServer worldserver, EntityCreature entitycreature, long i) {
        NavigationAbstract navigationabstract = entitycreature.getNavigation();

        if (navigationabstract instanceof Navigation navigation) {
            navigation.setCanPathToTargetsBelowSurface(true);
        }

    }

    protected boolean checkExtraStartConditions(WorldServer worldserver, EntityCreature entitycreature) {
        return !entitycreature.isLeashed();
    }

    protected boolean canStillUse(WorldServer worldserver, EntityCreature entitycreature, long i) {
        return entitycreature.getBrain().getMemory(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS).isEmpty() && !entitycreature.isPanicking() && !entitycreature.isLeashed();
    }

    @Override
    protected boolean timedOut(long i) {
        return false;
    }

    protected void tick(WorldServer worldserver, EntityCreature entitycreature, long i) {
        boolean flag = this.updateInvalidTarget(worldserver, entitycreature);

        if (this.target == null) {
            this.stop(worldserver, entitycreature, i);
        } else if (!flag) {
            if (this.state.equals(TransportItemsBetweenContainers.c.QUEUING)) {
                this.onQueuingForTarget(this.target, worldserver, entitycreature);
            }

            if (this.state.equals(TransportItemsBetweenContainers.c.TRAVELLING)) {
                this.onTravelToTarget(this.target, worldserver, entitycreature);
            }

            if (this.state.equals(TransportItemsBetweenContainers.c.INTERACTING)) {
                this.onReachedTarget(this.target, worldserver, entitycreature);
            }

        }
    }

    private boolean updateInvalidTarget(WorldServer worldserver, EntityCreature entitycreature) {
        if (!this.hasValidTarget(worldserver, entitycreature)) {
            this.stopTargetingCurrentTarget(entitycreature);
            Optional<TransportItemsBetweenContainers.d> optional = this.getTransportTarget(worldserver, entitycreature);

            if (optional.isPresent()) {
                this.target = (TransportItemsBetweenContainers.d) optional.get();
                this.onStartTravelling(entitycreature);
                this.setVisitedBlockPos(entitycreature, worldserver, this.target.pos);
                return true;
            } else {
                this.enterCooldownAfterNoMatchingTargetFound(entitycreature);
                return true;
            }
        } else {
            return false;
        }
    }

    private void onQueuingForTarget(TransportItemsBetweenContainers.d transportitemsbetweencontainers_d, World world, EntityCreature entitycreature) {
        if (!this.isAnotherMobInteractingWithTarget(transportitemsbetweencontainers_d, world)) {
            this.resumeTravelling(entitycreature);
        }

    }

    protected void onTravelToTarget(TransportItemsBetweenContainers.d transportitemsbetweencontainers_d, World world, EntityCreature entitycreature) {
        if (this.isWithinTargetDistance(3.0D, transportitemsbetweencontainers_d, world, entitycreature, this.getCenterPos(entitycreature)) && this.isAnotherMobInteractingWithTarget(transportitemsbetweencontainers_d, world)) {
            this.startQueuing(entitycreature);
        } else if (this.isWithinTargetDistance(getInteractionRange(entitycreature), transportitemsbetweencontainers_d, world, entitycreature, this.getCenterPos(entitycreature))) {
            this.startOnReachedTargetInteraction(transportitemsbetweencontainers_d, entitycreature);
        } else {
            this.walkTowardsTarget(entitycreature);
        }

    }

    private Vec3D getCenterPos(EntityCreature entitycreature) {
        return this.setMiddleYPosition(entitycreature, entitycreature.position());
    }

    protected void onReachedTarget(TransportItemsBetweenContainers.d transportitemsbetweencontainers_d, World world, EntityCreature entitycreature) {
        if (!this.isWithinTargetDistance(2.0D, transportitemsbetweencontainers_d, world, entitycreature, this.getCenterPos(entitycreature))) {
            this.onStartTravelling(entitycreature);
        } else {
            ++this.ticksSinceReachingTarget;
            this.onTargetInteraction(transportitemsbetweencontainers_d, entitycreature);
            if (this.ticksSinceReachingTarget >= 60) {
                this.doReachedTargetInteraction(entitycreature, transportitemsbetweencontainers_d.container, this::pickUpItems, (entitycreature1, iinventory) -> {
                    this.stopTargetingCurrentTarget(entitycreature);
                }, this::putDownItem, (entitycreature1, iinventory) -> {
                    this.stopTargetingCurrentTarget(entitycreature);
                });
                this.onStartTravelling(entitycreature);
            }
        }

    }

    private void startQueuing(EntityCreature entitycreature) {
        this.stopInPlace(entitycreature);
        this.setTransportingState(TransportItemsBetweenContainers.c.QUEUING);
    }

    private void resumeTravelling(EntityCreature entitycreature) {
        this.setTransportingState(TransportItemsBetweenContainers.c.TRAVELLING);
        this.walkTowardsTarget(entitycreature);
    }

    private void walkTowardsTarget(EntityCreature entitycreature) {
        if (this.target != null) {
            BehaviorUtil.setWalkAndLookTargetMemories(entitycreature, this.target.pos, this.speedModifier, 0);
        }

    }

    private void startOnReachedTargetInteraction(TransportItemsBetweenContainers.d transportitemsbetweencontainers_d, EntityCreature entitycreature) {
        this.doReachedTargetInteraction(entitycreature, transportitemsbetweencontainers_d.container, this.onReachedInteraction(TransportItemsBetweenContainers.a.PICKUP_ITEM), this.onReachedInteraction(TransportItemsBetweenContainers.a.PICKUP_NO_ITEM), this.onReachedInteraction(TransportItemsBetweenContainers.a.PLACE_ITEM), this.onReachedInteraction(TransportItemsBetweenContainers.a.PLACE_NO_ITEM));
        this.setTransportingState(TransportItemsBetweenContainers.c.INTERACTING);
    }

    private void onStartTravelling(EntityCreature entitycreature) {
        this.onStartTravelling.accept(entitycreature);
        this.setTransportingState(TransportItemsBetweenContainers.c.TRAVELLING);
        this.interactionState = null;
        this.ticksSinceReachingTarget = 0;
    }

    private BiConsumer<EntityCreature, IInventory> onReachedInteraction(TransportItemsBetweenContainers.a transportitemsbetweencontainers_a) {
        return (entitycreature, iinventory) -> {
            this.setInteractionState(transportitemsbetweencontainers_a);
        };
    }

    private void setTransportingState(TransportItemsBetweenContainers.c transportitemsbetweencontainers_c) {
        this.state = transportitemsbetweencontainers_c;
    }

    private void setInteractionState(TransportItemsBetweenContainers.a transportitemsbetweencontainers_a) {
        this.interactionState = transportitemsbetweencontainers_a;
    }

    private void onTargetInteraction(TransportItemsBetweenContainers.d transportitemsbetweencontainers_d, EntityCreature entitycreature) {
        entitycreature.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BehaviorTarget(transportitemsbetweencontainers_d.pos));
        this.stopInPlace(entitycreature);
        if (this.interactionState != null) {
            Optional.ofNullable((TransportItemsBetweenContainers.b) this.onTargetInteractionActions.get(this.interactionState)).ifPresent((transportitemsbetweencontainers_b) -> {
                transportitemsbetweencontainers_b.accept(entitycreature, transportitemsbetweencontainers_d, this.ticksSinceReachingTarget);
            });
        }

    }

    private void doReachedTargetInteraction(EntityCreature entitycreature, IInventory iinventory, BiConsumer<EntityCreature, IInventory> biconsumer, BiConsumer<EntityCreature, IInventory> biconsumer1, BiConsumer<EntityCreature, IInventory> biconsumer2, BiConsumer<EntityCreature, IInventory> biconsumer3) {
        if (isPickingUpItems(entitycreature)) {
            if (matchesGettingItemsRequirement(iinventory)) {
                biconsumer.accept(entitycreature, iinventory);
            } else {
                biconsumer1.accept(entitycreature, iinventory);
            }
        } else if (matchesLeavingItemsRequirement(entitycreature, iinventory)) {
            biconsumer2.accept(entitycreature, iinventory);
        } else {
            biconsumer3.accept(entitycreature, iinventory);
        }

    }

    private Optional<TransportItemsBetweenContainers.d> getTransportTarget(WorldServer worldserver, EntityCreature entitycreature) {
        AxisAlignedBB axisalignedbb = this.getTargetSearchArea(entitycreature);
        Set<GlobalPos> set = getVisitedPositions(entitycreature);
        Set<GlobalPos> set1 = getUnreachablePositions(entitycreature);
        List<ChunkCoordIntPair> list = ChunkCoordIntPair.rangeClosed(new ChunkCoordIntPair(entitycreature.blockPosition()), Math.floorDiv(this.getHorizontalSearchDistance(entitycreature), 16) + 1).toList();
        TransportItemsBetweenContainers.d transportitemsbetweencontainers_d = null;
        double d0 = (double) Float.MAX_VALUE;

        for (ChunkCoordIntPair chunkcoordintpair : list) {
            Chunk chunk = worldserver.getChunkSource().getChunkNow(chunkcoordintpair.x, chunkcoordintpair.z);

            if (chunk != null) {
                for (TileEntity tileentity : chunk.getBlockEntities().values()) {
                    if (tileentity instanceof TileEntityChest) {
                        TileEntityChest tileentitychest = (TileEntityChest) tileentity;
                        double d1 = tileentitychest.getBlockPos().distToCenterSqr(entitycreature.position());

                        if (d1 < d0) {
                            TransportItemsBetweenContainers.d transportitemsbetweencontainers_d1 = this.isTargetValidToPick(entitycreature, worldserver, tileentitychest, set, set1, axisalignedbb);

                            if (transportitemsbetweencontainers_d1 != null) {
                                transportitemsbetweencontainers_d = transportitemsbetweencontainers_d1;
                                d0 = d1;
                            }
                        }
                    }
                }
            }
        }

        return transportitemsbetweencontainers_d == null ? Optional.empty() : Optional.of(transportitemsbetweencontainers_d);
    }

    @Nullable
    private TransportItemsBetweenContainers.d isTargetValidToPick(EntityCreature entitycreature, World world, TileEntity tileentity, Set<GlobalPos> set, Set<GlobalPos> set1, AxisAlignedBB axisalignedbb) {
        BlockPosition blockposition = tileentity.getBlockPos();
        boolean flag = axisalignedbb.contains((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ());

        if (!flag) {
            return null;
        } else {
            TransportItemsBetweenContainers.d transportitemsbetweencontainers_d = TransportItemsBetweenContainers.d.tryCreatePossibleTarget(tileentity, world);

            if (transportitemsbetweencontainers_d == null) {
                return null;
            } else {
                boolean flag1 = this.isWantedBlock(entitycreature, transportitemsbetweencontainers_d.state) && !this.isPositionAlreadyVisited(set, set1, transportitemsbetweencontainers_d, world) && !this.isContainerLocked(transportitemsbetweencontainers_d);

                return flag1 ? transportitemsbetweencontainers_d : null;
            }
        }
    }

    private boolean isContainerLocked(TransportItemsBetweenContainers.d transportitemsbetweencontainers_d) {
        TileEntity tileentity = transportitemsbetweencontainers_d.blockEntity;
        boolean flag;

        if (tileentity instanceof TileEntityContainer tileentitycontainer) {
            if (tileentitycontainer.isLocked()) {
                flag = true;
                return flag;
            }
        }

        flag = false;
        return flag;
    }

    private boolean hasValidTarget(World world, EntityCreature entitycreature) {
        boolean flag = this.target != null && this.isWantedBlock(entitycreature, this.target.state) && this.targetHasNotChanged(world, this.target);

        if (flag && !this.isTargetBlocked(world, this.target)) {
            if (!this.state.equals(TransportItemsBetweenContainers.c.TRAVELLING)) {
                return true;
            }

            if (this.hasValidTravellingPath(world, this.target, entitycreature)) {
                return true;
            }

            this.markVisitedBlockPosAsUnreachable(entitycreature, world, this.target.pos);
        }

        return false;
    }

    private boolean hasValidTravellingPath(World world, TransportItemsBetweenContainers.d transportitemsbetweencontainers_d, EntityCreature entitycreature) {
        PathEntity pathentity = entitycreature.getNavigation().getPath() == null ? entitycreature.getNavigation().createPath(transportitemsbetweencontainers_d.pos, 0) : entitycreature.getNavigation().getPath();
        Vec3D vec3d = this.getPositionToReachTargetFrom(pathentity, entitycreature);
        boolean flag = this.isWithinTargetDistance(getInteractionRange(entitycreature), transportitemsbetweencontainers_d, world, entitycreature, vec3d);
        boolean flag1 = pathentity == null && !flag;

        return flag1 || this.targetIsReachableFromPosition(world, flag, vec3d, transportitemsbetweencontainers_d, entitycreature);
    }

    private Vec3D getPositionToReachTargetFrom(@Nullable PathEntity pathentity, EntityCreature entitycreature) {
        boolean flag = pathentity == null || pathentity.getEndNode() == null;
        Vec3D vec3d = flag ? entitycreature.position() : pathentity.getEndNode().asBlockPos().getBottomCenter();

        return this.setMiddleYPosition(entitycreature, vec3d);
    }

    private Vec3D setMiddleYPosition(EntityCreature entitycreature, Vec3D vec3d) {
        return vec3d.add(0.0D, entitycreature.getBoundingBox().getYsize() / 2.0D, 0.0D);
    }

    private boolean isTargetBlocked(World world, TransportItemsBetweenContainers.d transportitemsbetweencontainers_d) {
        return BlockChest.isChestBlockedAt(world, transportitemsbetweencontainers_d.pos);
    }

    private boolean targetHasNotChanged(World world, TransportItemsBetweenContainers.d transportitemsbetweencontainers_d) {
        return transportitemsbetweencontainers_d.blockEntity.equals(world.getBlockEntity(transportitemsbetweencontainers_d.pos));
    }

    private Stream<TransportItemsBetweenContainers.d> getConnectedTargets(TransportItemsBetweenContainers.d transportitemsbetweencontainers_d, World world) {
        if (transportitemsbetweencontainers_d.state.getValueOrElse(BlockChest.TYPE, BlockPropertyChestType.SINGLE) != BlockPropertyChestType.SINGLE) {
            TransportItemsBetweenContainers.d transportitemsbetweencontainers_d1 = TransportItemsBetweenContainers.d.tryCreatePossibleTarget(BlockChest.getConnectedBlockPos(transportitemsbetweencontainers_d.pos, transportitemsbetweencontainers_d.state), world);

            return transportitemsbetweencontainers_d1 != null ? Stream.of(transportitemsbetweencontainers_d, transportitemsbetweencontainers_d1) : Stream.of(transportitemsbetweencontainers_d);
        } else {
            return Stream.of(transportitemsbetweencontainers_d);
        }
    }

    private AxisAlignedBB getTargetSearchArea(EntityCreature entitycreature) {
        int i = this.getHorizontalSearchDistance(entitycreature);

        return (new AxisAlignedBB(entitycreature.blockPosition())).inflate((double) i, (double) this.getVerticalSearchDistance(entitycreature), (double) i);
    }

    private int getHorizontalSearchDistance(EntityCreature entitycreature) {
        return entitycreature.isPassenger() ? 1 : this.horizontalSearchDistance;
    }

    private int getVerticalSearchDistance(EntityCreature entitycreature) {
        return entitycreature.isPassenger() ? 1 : this.verticalSearchDistance;
    }

    private static Set<GlobalPos> getVisitedPositions(EntityCreature entitycreature) {
        return (Set) entitycreature.getBrain().getMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS).orElse(Set.of());
    }

    private static Set<GlobalPos> getUnreachablePositions(EntityCreature entitycreature) {
        return (Set) entitycreature.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS).orElse(Set.of());
    }

    private boolean isPositionAlreadyVisited(Set<GlobalPos> set, Set<GlobalPos> set1, TransportItemsBetweenContainers.d transportitemsbetweencontainers_d, World world) {
        return this.getConnectedTargets(transportitemsbetweencontainers_d, world).map((transportitemsbetweencontainers_d1) -> {
            return new GlobalPos(world.dimension(), transportitemsbetweencontainers_d1.pos);
        }).anyMatch((globalpos) -> {
            return set.contains(globalpos) || set1.contains(globalpos);
        });
    }

    private static boolean hasFinishedPath(EntityCreature entitycreature) {
        return entitycreature.getNavigation().getPath() != null && entitycreature.getNavigation().getPath().isDone();
    }

    protected void setVisitedBlockPos(EntityCreature entitycreature, World world, BlockPosition blockposition) {
        Set<GlobalPos> set = new HashSet(getVisitedPositions(entitycreature));

        set.add(new GlobalPos(world.dimension(), blockposition));
        if (set.size() > 10) {
            this.enterCooldownAfterNoMatchingTargetFound(entitycreature);
        } else {
            entitycreature.getBrain().setMemoryWithExpiry(MemoryModuleType.VISITED_BLOCK_POSITIONS, set, 6000L);
        }

    }

    protected void markVisitedBlockPosAsUnreachable(EntityCreature entitycreature, World world, BlockPosition blockposition) {
        Set<GlobalPos> set = new HashSet(getVisitedPositions(entitycreature));

        set.remove(new GlobalPos(world.dimension(), blockposition));
        Set<GlobalPos> set1 = new HashSet(getUnreachablePositions(entitycreature));

        set1.add(new GlobalPos(world.dimension(), blockposition));
        if (set1.size() > 50) {
            this.enterCooldownAfterNoMatchingTargetFound(entitycreature);
        } else {
            entitycreature.getBrain().setMemoryWithExpiry(MemoryModuleType.VISITED_BLOCK_POSITIONS, set, 6000L);
            entitycreature.getBrain().setMemoryWithExpiry(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS, set1, 6000L);
        }

    }

    private boolean isWantedBlock(EntityCreature entitycreature, IBlockData iblockdata) {
        return isPickingUpItems(entitycreature) ? this.sourceBlockType.test(iblockdata) : this.destinationBlockType.test(iblockdata);
    }

    private static double getInteractionRange(EntityCreature entitycreature) {
        return hasFinishedPath(entitycreature) ? 1.0D : 0.5D;
    }

    private boolean isWithinTargetDistance(double d0, TransportItemsBetweenContainers.d transportitemsbetweencontainers_d, World world, EntityCreature entitycreature, Vec3D vec3d) {
        AxisAlignedBB axisalignedbb = entitycreature.getBoundingBox();
        AxisAlignedBB axisalignedbb1 = AxisAlignedBB.ofSize(vec3d, axisalignedbb.getXsize(), axisalignedbb.getYsize(), axisalignedbb.getZsize());

        return transportitemsbetweencontainers_d.state.getCollisionShape(world, transportitemsbetweencontainers_d.pos).bounds().inflate(d0, 0.5D, d0).move(transportitemsbetweencontainers_d.pos).intersects(axisalignedbb1);
    }

    private boolean targetIsReachableFromPosition(World world, boolean flag, Vec3D vec3d, TransportItemsBetweenContainers.d transportitemsbetweencontainers_d, EntityCreature entitycreature) {
        return flag && this.canSeeAnyTargetSide(transportitemsbetweencontainers_d, world, entitycreature, vec3d);
    }

    private boolean canSeeAnyTargetSide(TransportItemsBetweenContainers.d transportitemsbetweencontainers_d, World world, EntityCreature entitycreature, Vec3D vec3d) {
        Vec3D vec3d1 = transportitemsbetweencontainers_d.pos.getCenter();

        return EnumDirection.stream().map((enumdirection) -> {
            return vec3d1.add(0.5D * (double) enumdirection.getStepX(), 0.5D * (double) enumdirection.getStepY(), 0.5D * (double) enumdirection.getStepZ());
        }).map((vec3d2) -> {
            return world.clip(new RayTrace(vec3d, vec3d2, RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.NONE, entitycreature));
        }).anyMatch((movingobjectpositionblock) -> {
            return movingobjectpositionblock.getType() == MovingObjectPosition.EnumMovingObjectType.BLOCK && movingobjectpositionblock.getBlockPos().equals(transportitemsbetweencontainers_d.pos);
        });
    }

    private boolean isAnotherMobInteractingWithTarget(TransportItemsBetweenContainers.d transportitemsbetweencontainers_d, World world) {
        return this.getConnectedTargets(transportitemsbetweencontainers_d, world).anyMatch(this.shouldQueueForTarget);
    }

    private static boolean isPickingUpItems(EntityCreature entitycreature) {
        return entitycreature.getMainHandItem().isEmpty();
    }

    private static boolean matchesGettingItemsRequirement(IInventory iinventory) {
        return !iinventory.isEmpty();
    }

    private static boolean matchesLeavingItemsRequirement(EntityCreature entitycreature, IInventory iinventory) {
        return iinventory.isEmpty() || hasItemMatchingHandItem(entitycreature, iinventory);
    }

    private static boolean hasItemMatchingHandItem(EntityCreature entitycreature, IInventory iinventory) {
        ItemStack itemstack = entitycreature.getMainHandItem();

        for (ItemStack itemstack1 : iinventory) {
            if (ItemStack.isSameItem(itemstack1, itemstack)) {
                return true;
            }
        }

        return false;
    }

    private void pickUpItems(EntityCreature entitycreature, IInventory iinventory) {
        entitycreature.setItemSlot(EnumItemSlot.MAINHAND, pickupItemFromContainer(iinventory));
        entitycreature.setGuaranteedDrop(EnumItemSlot.MAINHAND);
        iinventory.setChanged();
        this.clearMemoriesAfterMatchingTargetFound(entitycreature);
    }

    private void putDownItem(EntityCreature entitycreature, IInventory iinventory) {
        ItemStack itemstack = addItemsToContainer(entitycreature, iinventory);

        iinventory.setChanged();
        entitycreature.setItemSlot(EnumItemSlot.MAINHAND, itemstack);
        if (itemstack.isEmpty()) {
            this.clearMemoriesAfterMatchingTargetFound(entitycreature);
        } else {
            this.stopTargetingCurrentTarget(entitycreature);
        }

    }

    private static ItemStack pickupItemFromContainer(IInventory iinventory) {
        int i = 0;

        for (ItemStack itemstack : iinventory) {
            if (!itemstack.isEmpty()) {
                int j = Math.min(itemstack.getCount(), 16);

                return iinventory.removeItem(i, j);
            }

            ++i;
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack addItemsToContainer(EntityCreature entitycreature, IInventory iinventory) {
        int i = 0;
        ItemStack itemstack = entitycreature.getMainHandItem();

        for (ItemStack itemstack1 : iinventory) {
            if (itemstack1.isEmpty()) {
                iinventory.setItem(i, itemstack);
                return ItemStack.EMPTY;
            }

            if (ItemStack.isSameItemSameComponents(itemstack1, itemstack) && itemstack1.getCount() < itemstack1.getMaxStackSize()) {
                int j = itemstack1.getMaxStackSize() - itemstack1.getCount();
                int k = Math.min(j, itemstack.getCount());

                itemstack1.setCount(itemstack1.getCount() + k);
                itemstack.setCount(itemstack.getCount() - j);
                iinventory.setItem(i, itemstack1);
                if (itemstack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }

            ++i;
        }

        return itemstack;
    }

    protected void stopTargetingCurrentTarget(EntityCreature entitycreature) {
        this.ticksSinceReachingTarget = 0;
        this.target = null;
        entitycreature.getNavigation().stop();
        entitycreature.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    protected void clearMemoriesAfterMatchingTargetFound(EntityCreature entitycreature) {
        this.stopTargetingCurrentTarget(entitycreature);
        entitycreature.getBrain().eraseMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS);
        entitycreature.getBrain().eraseMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS);
    }

    private void enterCooldownAfterNoMatchingTargetFound(EntityCreature entitycreature) {
        this.stopTargetingCurrentTarget(entitycreature);
        entitycreature.getBrain().setMemory(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, 140);
        entitycreature.getBrain().eraseMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS);
        entitycreature.getBrain().eraseMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS);
    }

    protected void stop(WorldServer worldserver, EntityCreature entitycreature, long i) {
        this.onStartTravelling(entitycreature);
        NavigationAbstract navigationabstract = entitycreature.getNavigation();

        if (navigationabstract instanceof Navigation navigation) {
            navigation.setCanPathToTargetsBelowSurface(false);
        }

    }

    private void stopInPlace(EntityCreature entitycreature) {
        entitycreature.getNavigation().stop();
        entitycreature.setXxa(0.0F);
        entitycreature.setYya(0.0F);
        entitycreature.setSpeed(0.0F);
        entitycreature.setDeltaMovement(0.0D, entitycreature.getDeltaMovement().y, 0.0D);
    }

    public static enum c {

        TRAVELLING, QUEUING, INTERACTING;

        private c() {}
    }

    public static enum a {

        PICKUP_ITEM, PICKUP_NO_ITEM, PLACE_ITEM, PLACE_NO_ITEM;

        private a() {}
    }

    public static record d(BlockPosition pos, IInventory container, TileEntity blockEntity, IBlockData state) {

        @Nullable
        public static TransportItemsBetweenContainers.d tryCreatePossibleTarget(TileEntity tileentity, World world) {
            BlockPosition blockposition = tileentity.getBlockPos();
            IBlockData iblockdata = tileentity.getBlockState();
            IInventory iinventory = getBlockEntityContainer(tileentity, iblockdata, world, blockposition);

            return iinventory != null ? new TransportItemsBetweenContainers.d(blockposition, iinventory, tileentity, iblockdata) : null;
        }

        @Nullable
        public static TransportItemsBetweenContainers.d tryCreatePossibleTarget(BlockPosition blockposition, World world) {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            return tileentity == null ? null : tryCreatePossibleTarget(tileentity, world);
        }

        @Nullable
        private static IInventory getBlockEntityContainer(TileEntity tileentity, IBlockData iblockdata, World world, BlockPosition blockposition) {
            Block block = iblockdata.getBlock();

            if (block instanceof BlockChest blockchest) {
                return BlockChest.getContainer(blockchest, iblockdata, world, blockposition, false);
            } else if (tileentity instanceof IInventory iinventory) {
                return iinventory;
            } else {
                return null;
            }
        }
    }

    @FunctionalInterface
    public interface b extends TriConsumer<EntityCreature, TransportItemsBetweenContainers.d, Integer> {}
}
