package net.minecraft.gametest.framework;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockButtonAbstract;
import net.minecraft.world.level.block.BlockLever;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityContainer;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;

public class GameTestHarnessHelper {

    private final GameTestHarnessInfo testInfo;
    private boolean finalCheckAdded;

    public GameTestHarnessHelper(GameTestHarnessInfo gametestharnessinfo) {
        this.testInfo = gametestharnessinfo;
    }

    public GameTestHarnessAssertion assertionException(IChatBaseComponent ichatbasecomponent) {
        return new GameTestHarnessAssertion(ichatbasecomponent, this.testInfo.getTick());
    }

    public GameTestHarnessAssertion assertionException(String s, Object... aobject) {
        return this.assertionException(IChatBaseComponent.translatableEscape(s, aobject));
    }

    public GameTestHarnessAssertionPosition assertionException(BlockPosition blockposition, IChatBaseComponent ichatbasecomponent) {
        return new GameTestHarnessAssertionPosition(ichatbasecomponent, this.absolutePos(blockposition), blockposition, this.testInfo.getTick());
    }

    public GameTestHarnessAssertionPosition assertionException(BlockPosition blockposition, String s, Object... aobject) {
        return this.assertionException(blockposition, (IChatBaseComponent) IChatBaseComponent.translatableEscape(s, aobject));
    }

    public WorldServer getLevel() {
        return this.testInfo.getLevel();
    }

    public IBlockData getBlockState(BlockPosition blockposition) {
        return this.getLevel().getBlockState(this.absolutePos(blockposition));
    }

    public <T extends TileEntity> T getBlockEntity(BlockPosition blockposition, Class<T> oclass) {
        TileEntity tileentity = this.getLevel().getBlockEntity(this.absolutePos(blockposition));

        if (tileentity == null) {
            throw this.assertionException(blockposition, "test.error.missing_block_entity");
        } else if (oclass.isInstance(tileentity)) {
            return (T) (oclass.cast(tileentity));
        } else {
            throw this.assertionException(blockposition, "test.error.wrong_block_entity", tileentity.getType().builtInRegistryHolder().getRegisteredName());
        }
    }

    public void killAllEntities() {
        this.killAllEntitiesOfClass(Entity.class);
    }

    public void killAllEntitiesOfClass(Class<? extends Entity> oclass) {
        AxisAlignedBB axisalignedbb = this.getBounds();
        List<? extends Entity> list = this.getLevel().<Entity>getEntitiesOfClass(oclass, axisalignedbb.inflate(1.0D), (entity) -> {
            return !(entity instanceof EntityHuman);
        });

        list.forEach((entity) -> {
            entity.kill(this.getLevel());
        });
    }

    public EntityItem spawnItem(Item item, Vec3D vec3d) {
        WorldServer worldserver = this.getLevel();
        Vec3D vec3d1 = this.absoluteVec(vec3d);
        EntityItem entityitem = new EntityItem(worldserver, vec3d1.x, vec3d1.y, vec3d1.z, new ItemStack(item, 1));

        entityitem.setDeltaMovement(0.0D, 0.0D, 0.0D);
        worldserver.addFreshEntity(entityitem);
        return entityitem;
    }

    public EntityItem spawnItem(Item item, float f, float f1, float f2) {
        return this.spawnItem(item, new Vec3D((double) f, (double) f1, (double) f2));
    }

    public EntityItem spawnItem(Item item, BlockPosition blockposition) {
        return this.spawnItem(item, (float) blockposition.getX(), (float) blockposition.getY(), (float) blockposition.getZ());
    }

    public <E extends Entity> E spawn(EntityTypes<E> entitytypes, BlockPosition blockposition) {
        return (E) this.spawn(entitytypes, Vec3D.atBottomCenterOf(blockposition));
    }

    public <E extends Entity> E spawn(EntityTypes<E> entitytypes, Vec3D vec3d) {
        WorldServer worldserver = this.getLevel();
        E e0 = entitytypes.create(worldserver, EntitySpawnReason.STRUCTURE);

        if (e0 == null) {
            throw this.assertionException(BlockPosition.containing(vec3d), "test.error.spawn_failure", entitytypes.builtInRegistryHolder().getRegisteredName());
        } else {
            if (e0 instanceof EntityInsentient) {
                EntityInsentient entityinsentient = (EntityInsentient) e0;

                entityinsentient.setPersistenceRequired();
            }

            Vec3D vec3d1 = this.absoluteVec(vec3d);

            e0.snapTo(vec3d1.x, vec3d1.y, vec3d1.z, e0.getYRot(), e0.getXRot());
            worldserver.addFreshEntity(e0);
            return e0;
        }
    }

    public void hurt(Entity entity, DamageSource damagesource, float f) {
        entity.hurtServer(this.getLevel(), damagesource, f);
    }

    public void kill(Entity entity) {
        entity.kill(this.getLevel());
    }

    public <E extends Entity> E findOneEntity(EntityTypes<E> entitytypes) {
        return (E) this.findClosestEntity(entitytypes, 0, 0, 0, (double) Integer.MAX_VALUE);
    }

    public <E extends Entity> E findClosestEntity(EntityTypes<E> entitytypes, int i, int j, int k, double d0) {
        List<E> list = this.<E>findEntities(entitytypes, i, j, k, d0);

        if (list.isEmpty()) {
            throw this.assertionException("test.error.expected_entity_around", entitytypes.getDescription(), i, j, k);
        } else if (list.size() > 1) {
            throw this.assertionException("test.error.too_many_entities", entitytypes.toShortString(), i, j, k, list.size());
        } else {
            Vec3D vec3d = this.absoluteVec(new Vec3D((double) i, (double) j, (double) k));

            list.sort((entity, entity1) -> {
                double d1 = entity.position().distanceTo(vec3d);
                double d2 = entity1.position().distanceTo(vec3d);

                return Double.compare(d1, d2);
            });
            return (E) (list.get(0));
        }
    }

    public <E extends Entity> List<E> findEntities(EntityTypes<E> entitytypes, int i, int j, int k, double d0) {
        return this.<E>findEntities(entitytypes, Vec3D.atBottomCenterOf(new BlockPosition(i, j, k)), d0);
    }

    public <E extends Entity> List<E> findEntities(EntityTypes<E> entitytypes, Vec3D vec3d, double d0) {
        WorldServer worldserver = this.getLevel();
        Vec3D vec3d1 = this.absoluteVec(vec3d);
        AxisAlignedBB axisalignedbb = this.testInfo.getStructureBounds();
        AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(vec3d1.add(-d0, -d0, -d0), vec3d1.add(d0, d0, d0));

        return worldserver.getEntities(entitytypes, axisalignedbb, (entity) -> {
            return entity.getBoundingBox().intersects(axisalignedbb1) && entity.isAlive();
        });
    }

    public <E extends Entity> E spawn(EntityTypes<E> entitytypes, int i, int j, int k) {
        return (E) this.spawn(entitytypes, new BlockPosition(i, j, k));
    }

    public <E extends Entity> E spawn(EntityTypes<E> entitytypes, float f, float f1, float f2) {
        return (E) this.spawn(entitytypes, new Vec3D((double) f, (double) f1, (double) f2));
    }

    public <E extends EntityInsentient> E spawnWithNoFreeWill(EntityTypes<E> entitytypes, BlockPosition blockposition) {
        E e0 = (E) (this.spawn(entitytypes, blockposition));

        e0.removeFreeWill();
        return e0;
    }

    public <E extends EntityInsentient> E spawnWithNoFreeWill(EntityTypes<E> entitytypes, int i, int j, int k) {
        return (E) this.spawnWithNoFreeWill(entitytypes, new BlockPosition(i, j, k));
    }

    public <E extends EntityInsentient> E spawnWithNoFreeWill(EntityTypes<E> entitytypes, Vec3D vec3d) {
        E e0 = (E) (this.spawn(entitytypes, vec3d));

        e0.removeFreeWill();
        return e0;
    }

    public <E extends EntityInsentient> E spawnWithNoFreeWill(EntityTypes<E> entitytypes, float f, float f1, float f2) {
        return (E) this.spawnWithNoFreeWill(entitytypes, new Vec3D((double) f, (double) f1, (double) f2));
    }

    public void moveTo(EntityInsentient entityinsentient, float f, float f1, float f2) {
        Vec3D vec3d = this.absoluteVec(new Vec3D((double) f, (double) f1, (double) f2));

        entityinsentient.snapTo(vec3d.x, vec3d.y, vec3d.z, entityinsentient.getYRot(), entityinsentient.getXRot());
    }

    public GameTestHarnessSequence walkTo(EntityInsentient entityinsentient, BlockPosition blockposition, float f) {
        return this.startSequence().thenExecuteAfter(2, () -> {
            PathEntity pathentity = entityinsentient.getNavigation().createPath(this.absolutePos(blockposition), 0);

            entityinsentient.getNavigation().moveTo(pathentity, (double) f);
        });
    }

    public void pressButton(int i, int j, int k) {
        this.pressButton(new BlockPosition(i, j, k));
    }

    public void pressButton(BlockPosition blockposition) {
        this.assertBlockTag(TagsBlock.BUTTONS, blockposition);
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        IBlockData iblockdata = this.getLevel().getBlockState(blockposition1);
        BlockButtonAbstract blockbuttonabstract = (BlockButtonAbstract) iblockdata.getBlock();

        blockbuttonabstract.press(iblockdata, this.getLevel(), blockposition1, (EntityHuman) null);
    }

    public void useBlock(BlockPosition blockposition) {
        this.useBlock(blockposition, this.makeMockPlayer(EnumGamemode.CREATIVE));
    }

    public void useBlock(BlockPosition blockposition, EntityHuman entityhuman) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);

        this.useBlock(blockposition, entityhuman, new MovingObjectPositionBlock(Vec3D.atCenterOf(blockposition1), EnumDirection.NORTH, blockposition1, true));
    }

    public void useBlock(BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        IBlockData iblockdata = this.getLevel().getBlockState(blockposition1);
        EnumHand enumhand = EnumHand.MAIN_HAND;
        EnumInteractionResult enuminteractionresult = iblockdata.useItemOn(entityhuman.getItemInHand(enumhand), this.getLevel(), entityhuman, enumhand, movingobjectpositionblock);

        if (!enuminteractionresult.consumesAction()) {
            if (!(enuminteractionresult instanceof EnumInteractionResult.f) || !iblockdata.useWithoutItem(this.getLevel(), entityhuman, movingobjectpositionblock).consumesAction()) {
                ItemActionContext itemactioncontext = new ItemActionContext(entityhuman, enumhand, movingobjectpositionblock);

                entityhuman.getItemInHand(enumhand).useOn(itemactioncontext);
            }
        }
    }

    public EntityLiving makeAboutToDrown(EntityLiving entityliving) {
        entityliving.setAirSupply(0);
        entityliving.setHealth(0.25F);
        return entityliving;
    }

    public EntityLiving withLowHealth(EntityLiving entityliving) {
        entityliving.setHealth(0.25F);
        return entityliving;
    }

    public EntityHuman makeMockPlayer(final EnumGamemode enumgamemode) {
        return new EntityHuman(this.getLevel(), BlockPosition.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
            @Nonnull
            @Override
            public EnumGamemode gameMode() {
                return enumgamemode;
            }

            @Override
            public boolean isClientAuthoritative() {
                return false;
            }
        };
    }

    /** @deprecated */
    @Deprecated(forRemoval = true)
    public EntityPlayer makeMockServerPlayerInLevel() {
        CommonListenerCookie commonlistenercookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        EntityPlayer entityplayer = new EntityPlayer(this.getLevel().getServer(), this.getLevel(), commonlistenercookie.gameProfile(), commonlistenercookie.clientInformation()) {
            @Override
            public EnumGamemode gameMode() {
                return EnumGamemode.CREATIVE;
            }
        };
        NetworkManager networkmanager = new NetworkManager(EnumProtocolDirection.SERVERBOUND);

        new EmbeddedChannel(new ChannelHandler[]{networkmanager});
        this.getLevel().getServer().getPlayerList().placeNewPlayer(networkmanager, entityplayer, commonlistenercookie);
        return entityplayer;
    }

    public void pullLever(int i, int j, int k) {
        this.pullLever(new BlockPosition(i, j, k));
    }

    public void pullLever(BlockPosition blockposition) {
        this.assertBlockPresent(Blocks.LEVER, blockposition);
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        IBlockData iblockdata = this.getLevel().getBlockState(blockposition1);
        BlockLever blocklever = (BlockLever) iblockdata.getBlock();

        blocklever.pull(iblockdata, this.getLevel(), blockposition1, (EntityHuman) null);
    }

    public void pulseRedstone(BlockPosition blockposition, long i) {
        this.setBlock(blockposition, Blocks.REDSTONE_BLOCK);
        this.runAfterDelay(i, () -> {
            this.setBlock(blockposition, Blocks.AIR);
        });
    }

    public void destroyBlock(BlockPosition blockposition) {
        this.getLevel().destroyBlock(this.absolutePos(blockposition), false, (Entity) null);
    }

    public void setBlock(int i, int j, int k, Block block) {
        this.setBlock(new BlockPosition(i, j, k), block);
    }

    public void setBlock(int i, int j, int k, IBlockData iblockdata) {
        this.setBlock(new BlockPosition(i, j, k), iblockdata);
    }

    public void setBlock(BlockPosition blockposition, Block block) {
        this.setBlock(blockposition, block.defaultBlockState());
    }

    public void setBlock(BlockPosition blockposition, IBlockData iblockdata) {
        this.getLevel().setBlock(this.absolutePos(blockposition), iblockdata, 3);
    }

    public void setNight() {
        this.setDayTime(13000);
    }

    public void setDayTime(int i) {
        this.getLevel().setDayTime((long) i);
    }

    public void assertBlockPresent(Block block, int i, int j, int k) {
        this.assertBlockPresent(block, new BlockPosition(i, j, k));
    }

    public void assertBlockPresent(Block block, BlockPosition blockposition) {
        IBlockData iblockdata = this.getBlockState(blockposition);

        this.assertBlock(blockposition, (block1) -> {
            return iblockdata.is(block);
        }, (block1) -> {
            return IChatBaseComponent.translatable("test.error.expected_block", block.getName(), block1.getName());
        });
    }

    public void assertBlockNotPresent(Block block, int i, int j, int k) {
        this.assertBlockNotPresent(block, new BlockPosition(i, j, k));
    }

    public void assertBlockNotPresent(Block block, BlockPosition blockposition) {
        this.assertBlock(blockposition, (block1) -> {
            return !this.getBlockState(blockposition).is(block);
        }, (block1) -> {
            return IChatBaseComponent.translatable("test.error.unexpected_block", block.getName());
        });
    }

    public void assertBlockTag(TagKey<Block> tagkey, BlockPosition blockposition) {
        this.assertBlockState(blockposition, (iblockdata) -> {
            return iblockdata.is(tagkey);
        }, (iblockdata) -> {
            return IChatBaseComponent.translatable("test.error.expected_block_tag", tagkey.location(), iblockdata.getBlock().getName());
        });
    }

    public void succeedWhenBlockPresent(Block block, int i, int j, int k) {
        this.succeedWhenBlockPresent(block, new BlockPosition(i, j, k));
    }

    public void succeedWhenBlockPresent(Block block, BlockPosition blockposition) {
        this.succeedWhen(() -> {
            this.assertBlockPresent(block, blockposition);
        });
    }

    public void assertBlock(BlockPosition blockposition, Predicate<Block> predicate, Function<Block, IChatBaseComponent> function) {
        this.assertBlockState(blockposition, (iblockdata) -> {
            return predicate.test(iblockdata.getBlock());
        }, (iblockdata) -> {
            return (IChatBaseComponent) function.apply(iblockdata.getBlock());
        });
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPosition blockposition, IBlockState<T> iblockstate, T t0) {
        IBlockData iblockdata = this.getBlockState(blockposition);
        boolean flag = iblockdata.hasProperty(iblockstate);

        if (!flag) {
            throw this.assertionException(blockposition, "test.error.block_property_missing", iblockstate.getName(), t0);
        } else if (!iblockdata.getValue(iblockstate).equals(t0)) {
            throw this.assertionException(blockposition, "test.error.block_property_mismatch", iblockstate.getName(), t0, iblockdata.getValue(iblockstate));
        }
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPosition blockposition, IBlockState<T> iblockstate, Predicate<T> predicate, IChatBaseComponent ichatbasecomponent) {
        this.assertBlockState(blockposition, (iblockdata) -> {
            if (!iblockdata.hasProperty(iblockstate)) {
                return false;
            } else {
                T t0 = (T) iblockdata.getValue(iblockstate);

                return predicate.test(t0);
            }
        }, (iblockdata) -> {
            return ichatbasecomponent;
        });
    }

    public void assertBlockState(BlockPosition blockposition, IBlockData iblockdata) {
        IBlockData iblockdata1 = this.getBlockState(blockposition);

        if (!iblockdata1.equals(iblockdata)) {
            throw this.assertionException(blockposition, "test.error.state_not_equal", iblockdata, iblockdata1);
        }
    }

    public void assertBlockState(BlockPosition blockposition, Predicate<IBlockData> predicate, Function<IBlockData, IChatBaseComponent> function) {
        IBlockData iblockdata = this.getBlockState(blockposition);

        if (!predicate.test(iblockdata)) {
            throw this.assertionException(blockposition, (IChatBaseComponent) function.apply(iblockdata));
        }
    }

    public <T extends TileEntity> void assertBlockEntityData(BlockPosition blockposition, Class<T> oclass, Predicate<T> predicate, Supplier<IChatBaseComponent> supplier) {
        T t0 = this.getBlockEntity(blockposition, oclass);

        if (!predicate.test(t0)) {
            throw this.assertionException(blockposition, (IChatBaseComponent) supplier.get());
        }
    }

    public void assertRedstoneSignal(BlockPosition blockposition, EnumDirection enumdirection, IntPredicate intpredicate, Supplier<IChatBaseComponent> supplier) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        WorldServer worldserver = this.getLevel();
        IBlockData iblockdata = worldserver.getBlockState(blockposition1);
        int i = iblockdata.getSignal(worldserver, blockposition1, enumdirection);

        if (!intpredicate.test(i)) {
            throw this.assertionException(blockposition, (IChatBaseComponent) supplier.get());
        }
    }

    public void assertEntityPresent(EntityTypes<?> entitytypes) {
        List<? extends Entity> list = this.getLevel().getEntities(entitytypes, this.getBounds(), Entity::isAlive);

        if (list.isEmpty()) {
            throw this.assertionException("test.error.expected_entity_in_test", entitytypes.getDescription());
        }
    }

    public void assertEntityPresent(EntityTypes<?> entitytypes, int i, int j, int k) {
        this.assertEntityPresent(entitytypes, new BlockPosition(i, j, k));
    }

    public void assertEntityPresent(EntityTypes<?> entitytypes, BlockPosition blockposition) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<? extends Entity> list = this.getLevel().getEntities(entitytypes, new AxisAlignedBB(blockposition1), Entity::isAlive);

        if (list.isEmpty()) {
            throw this.assertionException(blockposition, "test.error.expected_entity", entitytypes.getDescription());
        }
    }

    public void assertEntityPresent(EntityTypes<?> entitytypes, AxisAlignedBB axisalignedbb) {
        AxisAlignedBB axisalignedbb1 = this.absoluteAABB(axisalignedbb);
        List<? extends Entity> list = this.getLevel().getEntities(entitytypes, axisalignedbb1, Entity::isAlive);

        if (list.isEmpty()) {
            throw this.assertionException(BlockPosition.containing(axisalignedbb.getCenter()), "test.error.expected_entity", entitytypes.getDescription());
        }
    }

    public void assertEntitiesPresent(EntityTypes<?> entitytypes, int i) {
        List<? extends Entity> list = this.getLevel().getEntities(entitytypes, this.getBounds(), Entity::isAlive);

        if (list.size() != i) {
            throw this.assertionException("test.error.expected_entity_count", i, entitytypes.getDescription(), list.size());
        }
    }

    public void assertEntitiesPresent(EntityTypes<?> entitytypes, BlockPosition blockposition, int i, double d0) {
        this.absolutePos(blockposition);
        List<? extends Entity> list = this.<Entity>getEntities(entitytypes, blockposition, d0);

        if (list.size() != i) {
            throw this.assertionException(blockposition, "test.error.expected_entity_count", i, entitytypes.getDescription(), list.size());
        }
    }

    public void assertEntityPresent(EntityTypes<?> entitytypes, BlockPosition blockposition, double d0) {
        List<? extends Entity> list = this.<Entity>getEntities(entitytypes, blockposition, d0);

        if (list.isEmpty()) {
            this.absolutePos(blockposition);
            throw this.assertionException(blockposition, "test.error.expected_entity", entitytypes.getDescription());
        }
    }

    public <T extends Entity> List<T> getEntities(EntityTypes<T> entitytypes, BlockPosition blockposition, double d0) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);

        return this.getLevel().getEntities(entitytypes, (new AxisAlignedBB(blockposition1)).inflate(d0), Entity::isAlive);
    }

    public <T extends Entity> List<T> getEntities(EntityTypes<T> entitytypes) {
        return this.getLevel().getEntities(entitytypes, this.getBounds(), Entity::isAlive);
    }

    public void assertEntityInstancePresent(Entity entity, int i, int j, int k) {
        this.assertEntityInstancePresent(entity, new BlockPosition(i, j, k));
    }

    public void assertEntityInstancePresent(Entity entity, BlockPosition blockposition) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<? extends Entity> list = this.getLevel().getEntities(entity.getType(), new AxisAlignedBB(blockposition1), Entity::isAlive);

        list.stream().filter((entity1) -> {
            return entity1 == entity;
        }).findFirst().orElseThrow(() -> {
            return this.assertionException(blockposition, "test.error.expected_entity", entity.getType().getDescription());
        });
    }

    public void assertItemEntityCountIs(Item item, BlockPosition blockposition, double d0, int i) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<EntityItem> list = this.getLevel().getEntities(EntityTypes.ITEM, (new AxisAlignedBB(blockposition1)).inflate(d0), Entity::isAlive);
        int j = 0;

        for (EntityItem entityitem : list) {
            ItemStack itemstack = entityitem.getItem();

            if (itemstack.is(item)) {
                j += itemstack.getCount();
            }
        }

        if (j != i) {
            throw this.assertionException(blockposition, "test.error.expected_items_count", i, item.getName(), j);
        }
    }

    public void assertItemEntityPresent(Item item, BlockPosition blockposition, double d0) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);

        for (Entity entity : this.getLevel().getEntities(EntityTypes.ITEM, (new AxisAlignedBB(blockposition1)).inflate(d0), Entity::isAlive)) {
            EntityItem entityitem = (EntityItem) entity;

            if (entityitem.getItem().getItem().equals(item)) {
                return;
            }
        }

        throw this.assertionException(blockposition, "test.error.expected_item", item.getName());
    }

    public void assertItemEntityNotPresent(Item item, BlockPosition blockposition, double d0) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);

        for (Entity entity : this.getLevel().getEntities(EntityTypes.ITEM, (new AxisAlignedBB(blockposition1)).inflate(d0), Entity::isAlive)) {
            EntityItem entityitem = (EntityItem) entity;

            if (entityitem.getItem().getItem().equals(item)) {
                throw this.assertionException(blockposition, "test.error.unexpected_item", item.getName());
            }
        }

    }

    public void assertItemEntityPresent(Item item) {
        for (Entity entity : this.getLevel().getEntities(EntityTypes.ITEM, this.getBounds(), Entity::isAlive)) {
            EntityItem entityitem = (EntityItem) entity;

            if (entityitem.getItem().getItem().equals(item)) {
                return;
            }
        }

        throw this.assertionException("test.error.expected_item", item.getName());
    }

    public void assertItemEntityNotPresent(Item item) {
        for (Entity entity : this.getLevel().getEntities(EntityTypes.ITEM, this.getBounds(), Entity::isAlive)) {
            EntityItem entityitem = (EntityItem) entity;

            if (entityitem.getItem().getItem().equals(item)) {
                throw this.assertionException("test.error.unexpected_item", item.getName());
            }
        }

    }

    public void assertEntityNotPresent(EntityTypes<?> entitytypes) {
        List<? extends Entity> list = this.getLevel().getEntities(entitytypes, this.getBounds(), Entity::isAlive);

        if (!list.isEmpty()) {
            throw this.assertionException(((Entity) list.getFirst()).blockPosition(), "test.error.unexpected_entity", entitytypes.getDescription());
        }
    }

    public void assertEntityNotPresent(EntityTypes<?> entitytypes, int i, int j, int k) {
        this.assertEntityNotPresent(entitytypes, new BlockPosition(i, j, k));
    }

    public void assertEntityNotPresent(EntityTypes<?> entitytypes, BlockPosition blockposition) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<? extends Entity> list = this.getLevel().getEntities(entitytypes, new AxisAlignedBB(blockposition1), Entity::isAlive);

        if (!list.isEmpty()) {
            throw this.assertionException(blockposition, "test.error.unexpected_entity", entitytypes.getDescription());
        }
    }

    public void assertEntityNotPresent(EntityTypes<?> entitytypes, AxisAlignedBB axisalignedbb) {
        AxisAlignedBB axisalignedbb1 = this.absoluteAABB(axisalignedbb);
        List<? extends Entity> list = this.getLevel().getEntities(entitytypes, axisalignedbb1, Entity::isAlive);

        if (!list.isEmpty()) {
            throw this.assertionException(((Entity) list.getFirst()).blockPosition(), "test.error.unexpected_entity", entitytypes.getDescription());
        }
    }

    public void assertEntityTouching(EntityTypes<?> entitytypes, double d0, double d1, double d2) {
        Vec3D vec3d = new Vec3D(d0, d1, d2);
        Vec3D vec3d1 = this.absoluteVec(vec3d);
        Predicate<? super Entity> predicate = (entity) -> {
            return entity.getBoundingBox().intersects(vec3d1, vec3d1);
        };
        List<? extends Entity> list = this.getLevel().getEntities(entitytypes, this.getBounds(), predicate);

        if (list.isEmpty()) {
            throw this.assertionException("test.error.expected_entity_touching", entitytypes.getDescription(), vec3d1.x(), vec3d1.y(), vec3d1.z(), d0, d1, d2);
        }
    }

    public void assertEntityNotTouching(EntityTypes<?> entitytypes, double d0, double d1, double d2) {
        Vec3D vec3d = new Vec3D(d0, d1, d2);
        Vec3D vec3d1 = this.absoluteVec(vec3d);
        Predicate<? super Entity> predicate = (entity) -> {
            return !entity.getBoundingBox().intersects(vec3d1, vec3d1);
        };
        List<? extends Entity> list = this.getLevel().getEntities(entitytypes, this.getBounds(), predicate);

        if (list.isEmpty()) {
            throw this.assertionException("test.error.expected_entity_not_touching", entitytypes.getDescription(), vec3d1.x(), vec3d1.y(), vec3d1.z(), d0, d1, d2);
        }
    }

    public <E extends Entity, T> void assertEntityData(BlockPosition blockposition, EntityTypes<E> entitytypes, Predicate<E> predicate) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<E> list = this.getLevel().getEntities(entitytypes, new AxisAlignedBB(blockposition1), Entity::isAlive);

        if (list.isEmpty()) {
            throw this.assertionException(blockposition, "test.error.expected_entity", entitytypes.getDescription());
        } else {
            for (E e0 : list) {
                if (!predicate.test(e0)) {
                    throw this.assertionException(e0.blockPosition(), "test.error.expected_entity_data_predicate", e0.getName());
                }
            }

        }
    }

    public <E extends Entity, T> void assertEntityData(BlockPosition blockposition, EntityTypes<E> entitytypes, Function<? super E, T> function, @Nullable T t0) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<E> list = this.getLevel().getEntities(entitytypes, new AxisAlignedBB(blockposition1), Entity::isAlive);

        if (list.isEmpty()) {
            throw this.assertionException(blockposition, "test.error.expected_entity", entitytypes.getDescription());
        } else {
            for (E e0 : list) {
                T t1 = (T) function.apply(e0);

                if (!Objects.equals(t1, t0)) {
                    throw this.assertionException(blockposition, "test.error.expected_entity_data", t0, t1);
                }
            }

        }
    }

    public <E extends EntityLiving> void assertEntityIsHolding(BlockPosition blockposition, EntityTypes<E> entitytypes, Item item) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<E> list = this.getLevel().getEntities(entitytypes, new AxisAlignedBB(blockposition1), Entity::isAlive);

        if (list.isEmpty()) {
            throw this.assertionException(blockposition, "test.error.expected_entity", entitytypes.getDescription());
        } else {
            for (E e0 : list) {
                if (e0.isHolding(item)) {
                    return;
                }
            }

            throw this.assertionException(blockposition, "test.error.expected_entity_holding", item.getName());
        }
    }

    public <E extends Entity & InventoryCarrier> void assertEntityInventoryContains(BlockPosition blockposition, EntityTypes<E> entitytypes, Item item) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<E> list = this.getLevel().getEntities(entitytypes, new AxisAlignedBB(blockposition1), (object) -> {
            return ((Entity) object).isAlive();
        });

        if (list.isEmpty()) {
            throw this.assertionException(blockposition, "test.error.expected_entity", entitytypes.getDescription());
        } else {
            for (E e0 : list) {
                if (((InventoryCarrier) e0).getInventory().hasAnyMatching((itemstack) -> {
                    return itemstack.is(item);
                })) {
                    return;
                }
            }

            throw this.assertionException(blockposition, "test.error.expected_entity_having", item.getName());
        }
    }

    public void assertContainerEmpty(BlockPosition blockposition) {
        TileEntityContainer tileentitycontainer = (TileEntityContainer) this.getBlockEntity(blockposition, TileEntityContainer.class);

        if (!tileentitycontainer.isEmpty()) {
            throw this.assertionException(blockposition, "test.error.expected_empty_container");
        }
    }

    public void assertContainerContainsSingle(BlockPosition blockposition, Item item) {
        TileEntityContainer tileentitycontainer = (TileEntityContainer) this.getBlockEntity(blockposition, TileEntityContainer.class);

        if (tileentitycontainer.countItem(item) != 1) {
            throw this.assertionException(blockposition, "test.error.expected_container_contents_single", item.getName());
        }
    }

    public void assertContainerContains(BlockPosition blockposition, Item item) {
        TileEntityContainer tileentitycontainer = (TileEntityContainer) this.getBlockEntity(blockposition, TileEntityContainer.class);

        if (tileentitycontainer.countItem(item) == 0) {
            throw this.assertionException(blockposition, "test.error.expected_container_contents", item.getName());
        }
    }

    public void assertSameBlockStates(StructureBoundingBox structureboundingbox, BlockPosition blockposition) {
        BlockPosition.betweenClosedStream(structureboundingbox).forEach((blockposition1) -> {
            BlockPosition blockposition2 = blockposition.offset(blockposition1.getX() - structureboundingbox.minX(), blockposition1.getY() - structureboundingbox.minY(), blockposition1.getZ() - structureboundingbox.minZ());

            this.assertSameBlockState(blockposition1, blockposition2);
        });
    }

    public void assertSameBlockState(BlockPosition blockposition, BlockPosition blockposition1) {
        IBlockData iblockdata = this.getBlockState(blockposition);
        IBlockData iblockdata1 = this.getBlockState(blockposition1);

        if (iblockdata != iblockdata1) {
            throw this.assertionException(blockposition, "test.error.state_not_equal", iblockdata1, iblockdata);
        }
    }

    public void assertAtTickTimeContainerContains(long i, BlockPosition blockposition, Item item) {
        this.runAtTickTime(i, () -> {
            this.assertContainerContainsSingle(blockposition, item);
        });
    }

    public void assertAtTickTimeContainerEmpty(long i, BlockPosition blockposition) {
        this.runAtTickTime(i, () -> {
            this.assertContainerEmpty(blockposition);
        });
    }

    public <E extends Entity, T> void succeedWhenEntityData(BlockPosition blockposition, EntityTypes<E> entitytypes, Function<E, T> function, T t0) {
        this.succeedWhen(() -> {
            this.assertEntityData(blockposition, entitytypes, function, t0);
        });
    }

    public void assertEntityPosition(Entity entity, AxisAlignedBB axisalignedbb, IChatBaseComponent ichatbasecomponent) {
        if (!axisalignedbb.contains(this.relativeVec(entity.position()))) {
            throw this.assertionException(ichatbasecomponent);
        }
    }

    public <E extends Entity> void assertEntityProperty(E e0, Predicate<E> predicate, IChatBaseComponent ichatbasecomponent) {
        if (!predicate.test(e0)) {
            throw this.assertionException(e0.blockPosition(), "test.error.entity_property", e0.getName(), ichatbasecomponent);
        }
    }

    public <E extends Entity, T> void assertEntityProperty(E e0, Function<E, T> function, T t0, IChatBaseComponent ichatbasecomponent) {
        T t1 = (T) function.apply(e0);

        if (!t1.equals(t0)) {
            throw this.assertionException(e0.blockPosition(), "test.error.entity_property_details", e0.getName(), ichatbasecomponent, t1, t0);
        }
    }

    public void assertLivingEntityHasMobEffect(EntityLiving entityliving, Holder<MobEffectList> holder, int i) {
        MobEffect mobeffect = entityliving.getEffect(holder);

        if (mobeffect == null || mobeffect.getAmplifier() != i) {
            throw this.assertionException("test.error.expected_entity_effect", entityliving.getName(), PotionContents.getPotionDescription(holder, i));
        }
    }

    public void succeedWhenEntityPresent(EntityTypes<?> entitytypes, int i, int j, int k) {
        this.succeedWhenEntityPresent(entitytypes, new BlockPosition(i, j, k));
    }

    public void succeedWhenEntityPresent(EntityTypes<?> entitytypes, BlockPosition blockposition) {
        this.succeedWhen(() -> {
            this.assertEntityPresent(entitytypes, blockposition);
        });
    }

    public void succeedWhenEntityNotPresent(EntityTypes<?> entitytypes, int i, int j, int k) {
        this.succeedWhenEntityNotPresent(entitytypes, new BlockPosition(i, j, k));
    }

    public void succeedWhenEntityNotPresent(EntityTypes<?> entitytypes, BlockPosition blockposition) {
        this.succeedWhen(() -> {
            this.assertEntityNotPresent(entitytypes, blockposition);
        });
    }

    public void succeed() {
        this.testInfo.succeed();
    }

    private void ensureSingleFinalCheck() {
        if (this.finalCheckAdded) {
            throw new IllegalStateException("This test already has final clause");
        } else {
            this.finalCheckAdded = true;
        }
    }

    public void succeedIf(Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(0L, runnable).thenSucceed();
    }

    public void succeedWhen(Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(runnable).thenSucceed();
    }

    public void succeedOnTickWhen(int i, Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil((long) i, runnable).thenSucceed();
    }

    public void runAtTickTime(long i, Runnable runnable) {
        this.testInfo.setRunAtTickTime(i, runnable);
    }

    public void runAfterDelay(long i, Runnable runnable) {
        this.runAtTickTime((long) this.testInfo.getTick() + i, runnable);
    }

    public void randomTick(BlockPosition blockposition) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        WorldServer worldserver = this.getLevel();

        worldserver.getBlockState(blockposition1).randomTick(worldserver, blockposition1, worldserver.random);
    }

    public void tickBlock(BlockPosition blockposition) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        WorldServer worldserver = this.getLevel();

        worldserver.getBlockState(blockposition1).tick(worldserver, blockposition1, worldserver.random);
    }

    public void tickPrecipitation(BlockPosition blockposition) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        WorldServer worldserver = this.getLevel();

        worldserver.tickPrecipitation(blockposition1);
    }

    public void tickPrecipitation() {
        AxisAlignedBB axisalignedbb = this.getRelativeBounds();
        int i = (int) Math.floor(axisalignedbb.maxX);
        int j = (int) Math.floor(axisalignedbb.maxZ);
        int k = (int) Math.floor(axisalignedbb.maxY);

        for (int l = (int) Math.floor(axisalignedbb.minX); l < i; ++l) {
            for (int i1 = (int) Math.floor(axisalignedbb.minZ); i1 < j; ++i1) {
                this.tickPrecipitation(new BlockPosition(l, k, i1));
            }
        }

    }

    public int getHeight(HeightMap.Type heightmap_type, int i, int j) {
        BlockPosition blockposition = this.absolutePos(new BlockPosition(i, 0, j));

        return this.relativePos(this.getLevel().getHeightmapPos(heightmap_type, blockposition)).getY();
    }

    public void fail(IChatBaseComponent ichatbasecomponent, BlockPosition blockposition) {
        throw this.assertionException(blockposition, ichatbasecomponent);
    }

    public void fail(IChatBaseComponent ichatbasecomponent, Entity entity) {
        throw this.assertionException(entity.blockPosition(), ichatbasecomponent);
    }

    public void fail(IChatBaseComponent ichatbasecomponent) {
        throw this.assertionException(ichatbasecomponent);
    }

    public void failIf(Runnable runnable) {
        this.testInfo.createSequence().thenWaitUntil(runnable).thenFail(() -> {
            return this.assertionException("test.error.fail");
        });
    }

    public void failIfEver(Runnable runnable) {
        LongStream.range((long) this.testInfo.getTick(), (long) this.testInfo.getTimeoutTicks()).forEach((i) -> {
            GameTestHarnessInfo gametestharnessinfo = this.testInfo;

            Objects.requireNonNull(runnable);
            gametestharnessinfo.setRunAtTickTime(i, runnable::run);
        });
    }

    public GameTestHarnessSequence startSequence() {
        return this.testInfo.createSequence();
    }

    public BlockPosition absolutePos(BlockPosition blockposition) {
        BlockPosition blockposition1 = this.testInfo.getTestOrigin();
        BlockPosition blockposition2 = blockposition1.offset(blockposition);

        return DefinedStructure.transform(blockposition2, EnumBlockMirror.NONE, this.testInfo.getRotation(), blockposition1);
    }

    public BlockPosition relativePos(BlockPosition blockposition) {
        BlockPosition blockposition1 = this.testInfo.getTestOrigin();
        EnumBlockRotation enumblockrotation = this.testInfo.getRotation().getRotated(EnumBlockRotation.CLOCKWISE_180);
        BlockPosition blockposition2 = DefinedStructure.transform(blockposition, EnumBlockMirror.NONE, enumblockrotation, blockposition1);

        return blockposition2.subtract(blockposition1);
    }

    public AxisAlignedBB absoluteAABB(AxisAlignedBB axisalignedbb) {
        Vec3D vec3d = this.absoluteVec(axisalignedbb.getMinPosition());
        Vec3D vec3d1 = this.absoluteVec(axisalignedbb.getMaxPosition());

        return new AxisAlignedBB(vec3d, vec3d1);
    }

    public AxisAlignedBB relativeAABB(AxisAlignedBB axisalignedbb) {
        Vec3D vec3d = this.relativeVec(axisalignedbb.getMinPosition());
        Vec3D vec3d1 = this.relativeVec(axisalignedbb.getMaxPosition());

        return new AxisAlignedBB(vec3d, vec3d1);
    }

    public Vec3D absoluteVec(Vec3D vec3d) {
        Vec3D vec3d1 = Vec3D.atLowerCornerOf(this.testInfo.getTestOrigin());

        return DefinedStructure.transform(vec3d1.add(vec3d), EnumBlockMirror.NONE, this.testInfo.getRotation(), this.testInfo.getTestOrigin());
    }

    public Vec3D relativeVec(Vec3D vec3d) {
        Vec3D vec3d1 = Vec3D.atLowerCornerOf(this.testInfo.getTestOrigin());

        return DefinedStructure.transform(vec3d.subtract(vec3d1), EnumBlockMirror.NONE, this.testInfo.getRotation(), this.testInfo.getTestOrigin());
    }

    public EnumBlockRotation getTestRotation() {
        return this.testInfo.getRotation();
    }

    public void assertTrue(boolean flag, IChatBaseComponent ichatbasecomponent) {
        if (!flag) {
            throw this.assertionException(ichatbasecomponent);
        }
    }

    public <N> void assertValueEqual(N n0, N n1, IChatBaseComponent ichatbasecomponent) {
        if (!n0.equals(n1)) {
            throw this.assertionException("test.error.value_not_equal", ichatbasecomponent, n0, n1);
        }
    }

    public void assertFalse(boolean flag, IChatBaseComponent ichatbasecomponent) {
        this.assertTrue(!flag, ichatbasecomponent);
    }

    public long getTick() {
        return (long) this.testInfo.getTick();
    }

    public AxisAlignedBB getBounds() {
        return this.testInfo.getStructureBounds();
    }

    private AxisAlignedBB getRelativeBounds() {
        AxisAlignedBB axisalignedbb = this.testInfo.getStructureBounds();
        EnumBlockRotation enumblockrotation = this.testInfo.getRotation();

        switch (enumblockrotation) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                return new AxisAlignedBB(0.0D, 0.0D, 0.0D, axisalignedbb.getZsize(), axisalignedbb.getYsize(), axisalignedbb.getXsize());
            default:
                return new AxisAlignedBB(0.0D, 0.0D, 0.0D, axisalignedbb.getXsize(), axisalignedbb.getYsize(), axisalignedbb.getZsize());
        }
    }

    public void forEveryBlockInStructure(Consumer<BlockPosition> consumer) {
        AxisAlignedBB axisalignedbb = this.getRelativeBounds().contract(1.0D, 1.0D, 1.0D);

        BlockPosition.MutableBlockPosition.betweenClosedStream(axisalignedbb).forEach(consumer);
    }

    public void onEachTick(Runnable runnable) {
        LongStream.range((long) this.testInfo.getTick(), (long) this.testInfo.getTimeoutTicks()).forEach((i) -> {
            GameTestHarnessInfo gametestharnessinfo = this.testInfo;

            Objects.requireNonNull(runnable);
            gametestharnessinfo.setRunAtTickTime(i, runnable::run);
        });
    }

    public void placeAt(EntityHuman entityhuman, ItemStack itemstack, BlockPosition blockposition, EnumDirection enumdirection) {
        BlockPosition blockposition1 = this.absolutePos(blockposition.relative(enumdirection));
        MovingObjectPositionBlock movingobjectpositionblock = new MovingObjectPositionBlock(Vec3D.atCenterOf(blockposition1), enumdirection, blockposition1, false);
        ItemActionContext itemactioncontext = new ItemActionContext(entityhuman, EnumHand.MAIN_HAND, movingobjectpositionblock);

        itemstack.useOn(itemactioncontext);
    }

    public void setBiome(ResourceKey<BiomeBase> resourcekey) {
        AxisAlignedBB axisalignedbb = this.getBounds();
        BlockPosition blockposition = BlockPosition.containing(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ);
        BlockPosition blockposition1 = BlockPosition.containing(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ);
        Either<Integer, CommandSyntaxException> either = FillBiomeCommand.fill(this.getLevel(), blockposition, blockposition1, this.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(resourcekey));

        if (either.right().isPresent()) {
            throw this.assertionException("test.error.set_biome");
        }
    }
}
