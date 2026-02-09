package net.minecraft.util.debug;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDebugBlockValuePacket;
import net.minecraft.network.protocol.game.ClientboundDebugChunkValuePacket;
import net.minecraft.network.protocol.game.ClientboundDebugEntityValuePacket;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.VillagePlace;
import net.minecraft.world.entity.ai.village.poi.VillagePlaceRecord;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.ChunkCoordIntPair;

public abstract class TrackingDebugSynchronizer<T> {

    protected final DebugSubscription<T> subscription;
    private final Set<UUID> subscribedPlayers = new ObjectOpenHashSet();

    public TrackingDebugSynchronizer(DebugSubscription<T> debugsubscription) {
        this.subscription = debugsubscription;
    }

    public final void tick(WorldServer worldserver) {
        for (EntityPlayer entityplayer : worldserver.players()) {
            boolean flag = this.subscribedPlayers.contains(entityplayer.getUUID());
            boolean flag1 = entityplayer.debugSubscriptions().contains(this.subscription);

            if (flag1 != flag) {
                if (flag1) {
                    this.addSubscriber(entityplayer);
                } else {
                    this.subscribedPlayers.remove(entityplayer.getUUID());
                }
            }
        }

        this.subscribedPlayers.removeIf((uuid) -> {
            return worldserver.getPlayerByUUID(uuid) == null;
        });
        if (!this.subscribedPlayers.isEmpty()) {
            this.pollAndSendUpdates(worldserver);
        }

    }

    private void addSubscriber(EntityPlayer entityplayer) {
        this.subscribedPlayers.add(entityplayer.getUUID());
        entityplayer.getChunkTrackingView().forEach((chunkcoordintpair) -> {
            if (!entityplayer.connection.chunkSender.isPending(chunkcoordintpair.toLong())) {
                this.startTrackingChunk(entityplayer, chunkcoordintpair);
            }

        });
        entityplayer.level().getChunkSource().chunkMap.forEachEntityTrackedBy(entityplayer, (entity) -> {
            this.startTrackingEntity(entityplayer, entity);
        });
    }

    protected final void sendToPlayersTrackingChunk(WorldServer worldserver, ChunkCoordIntPair chunkcoordintpair, Packet<? super PacketListenerPlayOut> packet) {
        PlayerChunkMap playerchunkmap = worldserver.getChunkSource().chunkMap;

        for (UUID uuid : this.subscribedPlayers) {
            EntityHuman entityhuman = worldserver.getPlayerByUUID(uuid);

            if (entityhuman instanceof EntityPlayer entityplayer) {
                if (playerchunkmap.isChunkTracked(entityplayer, chunkcoordintpair.x, chunkcoordintpair.z)) {
                    entityplayer.connection.send(packet);
                }
            }
        }

    }

    protected final void sendToPlayersTrackingEntity(WorldServer worldserver, Entity entity, Packet<? super PacketListenerPlayOut> packet) {
        PlayerChunkMap playerchunkmap = worldserver.getChunkSource().chunkMap;

        playerchunkmap.sendToTrackingPlayersFiltered(entity, packet, (entityplayer) -> {
            return this.subscribedPlayers.contains(entityplayer.getUUID());
        });
    }

    public final void startTrackingChunk(EntityPlayer entityplayer, ChunkCoordIntPair chunkcoordintpair) {
        if (this.subscribedPlayers.contains(entityplayer.getUUID())) {
            this.sendInitialChunk(entityplayer, chunkcoordintpair);
        }

    }

    public final void startTrackingEntity(EntityPlayer entityplayer, Entity entity) {
        if (this.subscribedPlayers.contains(entityplayer.getUUID())) {
            this.sendInitialEntity(entityplayer, entity);
        }

    }

    protected void clear() {}

    protected void pollAndSendUpdates(WorldServer worldserver) {}

    protected void sendInitialChunk(EntityPlayer entityplayer, ChunkCoordIntPair chunkcoordintpair) {}

    protected void sendInitialEntity(EntityPlayer entityplayer, Entity entity) {}

    public static class b<T> extends TrackingDebugSynchronizer<T> {

        private final Map<ChunkCoordIntPair, TrackingDebugSynchronizer.c<T>> chunkSources = new HashMap();
        private final Map<BlockPosition, TrackingDebugSynchronizer.c<T>> blockEntitySources = new HashMap();
        private final Map<UUID, TrackingDebugSynchronizer.c<T>> entitySources = new HashMap();

        public b(DebugSubscription<T> debugsubscription) {
            super(debugsubscription);
        }

        @Override
        protected void clear() {
            this.chunkSources.clear();
            this.blockEntitySources.clear();
            this.entitySources.clear();
        }

        @Override
        protected void pollAndSendUpdates(WorldServer worldserver) {
            for (Map.Entry<ChunkCoordIntPair, TrackingDebugSynchronizer.c<T>> map_entry : this.chunkSources.entrySet()) {
                DebugSubscription.b<T> debugsubscription_b = ((TrackingDebugSynchronizer.c) map_entry.getValue()).pollUpdate(this.subscription);

                if (debugsubscription_b != null) {
                    ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair) map_entry.getKey();

                    this.sendToPlayersTrackingChunk(worldserver, chunkcoordintpair, new ClientboundDebugChunkValuePacket(chunkcoordintpair, debugsubscription_b));
                }
            }

            for (Map.Entry<BlockPosition, TrackingDebugSynchronizer.c<T>> map_entry1 : this.blockEntitySources.entrySet()) {
                DebugSubscription.b<T> debugsubscription_b1 = ((TrackingDebugSynchronizer.c) map_entry1.getValue()).pollUpdate(this.subscription);

                if (debugsubscription_b1 != null) {
                    BlockPosition blockposition = (BlockPosition) map_entry1.getKey();
                    ChunkCoordIntPair chunkcoordintpair1 = new ChunkCoordIntPair(blockposition);

                    this.sendToPlayersTrackingChunk(worldserver, chunkcoordintpair1, new ClientboundDebugBlockValuePacket(blockposition, debugsubscription_b1));
                }
            }

            for (Map.Entry<UUID, TrackingDebugSynchronizer.c<T>> map_entry2 : this.entitySources.entrySet()) {
                DebugSubscription.b<T> debugsubscription_b2 = ((TrackingDebugSynchronizer.c) map_entry2.getValue()).pollUpdate(this.subscription);

                if (debugsubscription_b2 != null) {
                    Entity entity = (Entity) Objects.requireNonNull(worldserver.getEntity((UUID) map_entry2.getKey()));

                    this.sendToPlayersTrackingEntity(worldserver, entity, new ClientboundDebugEntityValuePacket(entity.getId(), debugsubscription_b2));
                }
            }

        }

        public void registerChunk(ChunkCoordIntPair chunkcoordintpair, DebugValueSource.b<T> debugvaluesource_b) {
            this.chunkSources.put(chunkcoordintpair, new TrackingDebugSynchronizer.c(debugvaluesource_b));
        }

        public void registerBlockEntity(BlockPosition blockposition, DebugValueSource.b<T> debugvaluesource_b) {
            this.blockEntitySources.put(blockposition, new TrackingDebugSynchronizer.c(debugvaluesource_b));
        }

        public void registerEntity(UUID uuid, DebugValueSource.b<T> debugvaluesource_b) {
            this.entitySources.put(uuid, new TrackingDebugSynchronizer.c(debugvaluesource_b));
        }

        public void dropChunk(ChunkCoordIntPair chunkcoordintpair) {
            this.chunkSources.remove(chunkcoordintpair);
            Set set = this.blockEntitySources.keySet();

            Objects.requireNonNull(chunkcoordintpair);
            set.removeIf(chunkcoordintpair::contains);
        }

        public void dropBlockEntity(WorldServer worldserver, BlockPosition blockposition) {
            TrackingDebugSynchronizer.c<T> trackingdebugsynchronizer_c = (TrackingDebugSynchronizer.c) this.blockEntitySources.remove(blockposition);

            if (trackingdebugsynchronizer_c != null) {
                ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(blockposition);

                this.sendToPlayersTrackingChunk(worldserver, chunkcoordintpair, new ClientboundDebugBlockValuePacket(blockposition, this.subscription.emptyUpdate()));
            }

        }

        public void dropEntity(Entity entity) {
            this.entitySources.remove(entity.getUUID());
        }

        @Override
        protected void sendInitialChunk(EntityPlayer entityplayer, ChunkCoordIntPair chunkcoordintpair) {
            TrackingDebugSynchronizer.c<T> trackingdebugsynchronizer_c = (TrackingDebugSynchronizer.c) this.chunkSources.get(chunkcoordintpair);

            if (trackingdebugsynchronizer_c != null && trackingdebugsynchronizer_c.lastSyncedValue != null) {
                entityplayer.connection.send(new ClientboundDebugChunkValuePacket(chunkcoordintpair, this.subscription.packUpdate(trackingdebugsynchronizer_c.lastSyncedValue)));
            }

            for (Map.Entry<BlockPosition, TrackingDebugSynchronizer.c<T>> map_entry : this.blockEntitySources.entrySet()) {
                T t0 = ((TrackingDebugSynchronizer.c) map_entry.getValue()).lastSyncedValue;

                if (t0 != null) {
                    BlockPosition blockposition = (BlockPosition) map_entry.getKey();

                    if (chunkcoordintpair.contains(blockposition)) {
                        entityplayer.connection.send(new ClientboundDebugBlockValuePacket(blockposition, this.subscription.packUpdate(t0)));
                    }
                }
            }

        }

        @Override
        protected void sendInitialEntity(EntityPlayer entityplayer, Entity entity) {
            TrackingDebugSynchronizer.c<T> trackingdebugsynchronizer_c = (TrackingDebugSynchronizer.c) this.entitySources.get(entity.getUUID());

            if (trackingdebugsynchronizer_c != null && trackingdebugsynchronizer_c.lastSyncedValue != null) {
                entityplayer.connection.send(new ClientboundDebugEntityValuePacket(entity.getId(), this.subscription.packUpdate(trackingdebugsynchronizer_c.lastSyncedValue)));
            }

        }
    }

    private static class c<T> {

        private final DebugValueSource.b<T> getter;
        @Nullable
        T lastSyncedValue;

        c(DebugValueSource.b<T> debugvaluesource_b) {
            this.getter = debugvaluesource_b;
        }

        @Nullable
        public DebugSubscription.b<T> pollUpdate(DebugSubscription<T> debugsubscription) {
            T t0 = this.getter.get();

            if (!Objects.equals(t0, this.lastSyncedValue)) {
                this.lastSyncedValue = t0;
                return debugsubscription.packUpdate(t0);
            } else {
                return null;
            }
        }
    }

    public static class a extends TrackingDebugSynchronizer<DebugPoiInfo> {

        public a() {
            super(DebugSubscriptions.POIS);
        }

        @Override
        protected void sendInitialChunk(EntityPlayer entityplayer, ChunkCoordIntPair chunkcoordintpair) {
            WorldServer worldserver = entityplayer.level();
            VillagePlace villageplace = worldserver.getPoiManager();

            villageplace.getInChunk((holder) -> {
                return true;
            }, chunkcoordintpair, VillagePlace.Occupancy.ANY).forEach((villageplacerecord) -> {
                entityplayer.connection.send(new ClientboundDebugBlockValuePacket(villageplacerecord.getPos(), this.subscription.packUpdate(new DebugPoiInfo(villageplacerecord))));
            });
        }

        public void onPoiAdded(WorldServer worldserver, VillagePlaceRecord villageplacerecord) {
            this.sendToPlayersTrackingChunk(worldserver, new ChunkCoordIntPair(villageplacerecord.getPos()), new ClientboundDebugBlockValuePacket(villageplacerecord.getPos(), this.subscription.packUpdate(new DebugPoiInfo(villageplacerecord))));
        }

        public void onPoiRemoved(WorldServer worldserver, BlockPosition blockposition) {
            this.sendToPlayersTrackingChunk(worldserver, new ChunkCoordIntPair(blockposition), new ClientboundDebugBlockValuePacket(blockposition, this.subscription.emptyUpdate()));
        }

        public void onPoiTicketCountChanged(WorldServer worldserver, BlockPosition blockposition) {
            this.sendToPlayersTrackingChunk(worldserver, new ChunkCoordIntPair(blockposition), new ClientboundDebugBlockValuePacket(blockposition, this.subscription.packUpdate(worldserver.getPoiManager().getDebugPoiInfo(blockposition))));
        }
    }

    public static class d extends TrackingDebugSynchronizer<Unit> {

        public d() {
            super(DebugSubscriptions.VILLAGE_SECTIONS);
        }

        @Override
        protected void sendInitialChunk(EntityPlayer entityplayer, ChunkCoordIntPair chunkcoordintpair) {
            WorldServer worldserver = entityplayer.level();
            VillagePlace villageplace = worldserver.getPoiManager();

            villageplace.getInChunk((holder) -> {
                return true;
            }, chunkcoordintpair, VillagePlace.Occupancy.ANY).forEach((villageplacerecord) -> {
                SectionPosition sectionposition = SectionPosition.of(villageplacerecord.getPos());

                forEachVillageSectionUpdate(worldserver, sectionposition, (sectionposition1, obool) -> {
                    BlockPosition blockposition = sectionposition1.center();

                    entityplayer.connection.send(new ClientboundDebugBlockValuePacket(blockposition, this.subscription.packUpdate(obool ? Unit.INSTANCE : null)));
                });
            });
        }

        public void onPoiAdded(WorldServer worldserver, VillagePlaceRecord villageplacerecord) {
            this.sendVillageSectionsPacket(worldserver, villageplacerecord.getPos());
        }

        public void onPoiRemoved(WorldServer worldserver, BlockPosition blockposition) {
            this.sendVillageSectionsPacket(worldserver, blockposition);
        }

        private void sendVillageSectionsPacket(WorldServer worldserver, BlockPosition blockposition) {
            forEachVillageSectionUpdate(worldserver, SectionPosition.of(blockposition), (sectionposition, obool) -> {
                BlockPosition blockposition1 = sectionposition.center();

                if (obool) {
                    this.sendToPlayersTrackingChunk(worldserver, new ChunkCoordIntPair(blockposition1), new ClientboundDebugBlockValuePacket(blockposition1, this.subscription.packUpdate(Unit.INSTANCE)));
                } else {
                    this.sendToPlayersTrackingChunk(worldserver, new ChunkCoordIntPair(blockposition1), new ClientboundDebugBlockValuePacket(blockposition1, this.subscription.emptyUpdate()));
                }

            });
        }

        private static void forEachVillageSectionUpdate(WorldServer worldserver, SectionPosition sectionposition, BiConsumer<SectionPosition, Boolean> biconsumer) {
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    for (int k = -1; k <= 1; ++k) {
                        SectionPosition sectionposition1 = sectionposition.offset(j, k, i);

                        if (worldserver.isVillage(sectionposition1.center())) {
                            biconsumer.accept(sectionposition1, true);
                        } else {
                            biconsumer.accept(sectionposition1, false);
                        }
                    }
                }
            }

        }
    }
}
