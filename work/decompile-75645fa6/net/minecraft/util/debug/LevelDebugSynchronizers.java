package net.minecraft.util.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDebugBlockValuePacket;
import net.minecraft.network.protocol.game.ClientboundDebugEntityValuePacket;
import net.minecraft.network.protocol.game.ClientboundDebugEventPacket;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.VillagePlaceRecord;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.chunk.Chunk;

public class LevelDebugSynchronizers {

    private final WorldServer level;
    private final List<TrackingDebugSynchronizer<?>> allSynchronizers = new ArrayList();
    private final Map<DebugSubscription<?>, TrackingDebugSynchronizer.b<?>> sourceSynchronizers = new HashMap();
    private final TrackingDebugSynchronizer.a poiSynchronizer = new TrackingDebugSynchronizer.a();
    private final TrackingDebugSynchronizer.d villageSectionSynchronizer = new TrackingDebugSynchronizer.d();
    private boolean sleeping = true;
    private Set<DebugSubscription<?>> enabledSubscriptions = Set.of();

    public LevelDebugSynchronizers(WorldServer worldserver) {
        this.level = worldserver;

        for (DebugSubscription<?> debugsubscription : BuiltInRegistries.DEBUG_SUBSCRIPTION) {
            if (debugsubscription.valueStreamCodec() != null) {
                this.sourceSynchronizers.put(debugsubscription, new TrackingDebugSynchronizer.b(debugsubscription));
            }
        }

        this.allSynchronizers.addAll(this.sourceSynchronizers.values());
        this.allSynchronizers.add(this.poiSynchronizer);
        this.allSynchronizers.add(this.villageSectionSynchronizer);
    }

    public void tick(ServerDebugSubscribers serverdebugsubscribers) {
        this.enabledSubscriptions = serverdebugsubscribers.enabledSubscriptions();
        boolean flag = this.enabledSubscriptions.isEmpty();

        if (this.sleeping != flag) {
            if (flag) {
                for (TrackingDebugSynchronizer<?> trackingdebugsynchronizer : this.allSynchronizers) {
                    trackingdebugsynchronizer.clear();
                }
            } else {
                this.wakeUp();
            }

            this.sleeping = flag;
        }

        if (!this.sleeping) {
            for (TrackingDebugSynchronizer<?> trackingdebugsynchronizer1 : this.allSynchronizers) {
                trackingdebugsynchronizer1.tick(this.level);
            }
        }

    }

    private void wakeUp() {
        PlayerChunkMap playerchunkmap = this.level.getChunkSource().chunkMap;

        playerchunkmap.forEachReadyToSendChunk(this::registerChunk);

        for (Entity entity : this.level.getAllEntities()) {
            if (playerchunkmap.isTrackedByAnyPlayer(entity)) {
                this.registerEntity(entity);
            }
        }

    }

    <T> TrackingDebugSynchronizer.b<T> getSourceSynchronizer(DebugSubscription<T> debugsubscription) {
        return (TrackingDebugSynchronizer.b) this.sourceSynchronizers.get(debugsubscription);
    }

    public void registerChunk(final Chunk chunk) {
        if (!this.sleeping) {
            chunk.registerDebugValues(this.level, new DebugValueSource.a() {
                @Override
                public <T> void register(DebugSubscription<T> debugsubscription, DebugValueSource.b<T> debugvaluesource_b) {
                    LevelDebugSynchronizers.this.getSourceSynchronizer(debugsubscription).registerChunk(chunk.getPos(), debugvaluesource_b);
                }
            });
            chunk.getBlockEntities().values().forEach(this::registerBlockEntity);
        }
    }

    public void dropChunk(ChunkCoordIntPair chunkcoordintpair) {
        if (!this.sleeping) {
            for (TrackingDebugSynchronizer.b<?> trackingdebugsynchronizer_b : this.sourceSynchronizers.values()) {
                trackingdebugsynchronizer_b.dropChunk(chunkcoordintpair);
            }

        }
    }

    public void registerBlockEntity(final TileEntity tileentity) {
        if (!this.sleeping) {
            tileentity.registerDebugValues(this.level, new DebugValueSource.a() {
                @Override
                public <T> void register(DebugSubscription<T> debugsubscription, DebugValueSource.b<T> debugvaluesource_b) {
                    LevelDebugSynchronizers.this.getSourceSynchronizer(debugsubscription).registerBlockEntity(tileentity.getBlockPos(), debugvaluesource_b);
                }
            });
        }
    }

    public void dropBlockEntity(BlockPosition blockposition) {
        if (!this.sleeping) {
            for (TrackingDebugSynchronizer.b<?> trackingdebugsynchronizer_b : this.sourceSynchronizers.values()) {
                trackingdebugsynchronizer_b.dropBlockEntity(this.level, blockposition);
            }

        }
    }

    public void registerEntity(final Entity entity) {
        if (!this.sleeping) {
            entity.registerDebugValues(this.level, new DebugValueSource.a() {
                @Override
                public <T> void register(DebugSubscription<T> debugsubscription, DebugValueSource.b<T> debugvaluesource_b) {
                    LevelDebugSynchronizers.this.getSourceSynchronizer(debugsubscription).registerEntity(entity.getUUID(), debugvaluesource_b);
                }
            });
        }
    }

    public void dropEntity(Entity entity) {
        if (!this.sleeping) {
            for (TrackingDebugSynchronizer.b<?> trackingdebugsynchronizer_b : this.sourceSynchronizers.values()) {
                trackingdebugsynchronizer_b.dropEntity(entity);
            }

        }
    }

    public void startTrackingChunk(EntityPlayer entityplayer, ChunkCoordIntPair chunkcoordintpair) {
        if (!this.sleeping) {
            for (TrackingDebugSynchronizer<?> trackingdebugsynchronizer : this.allSynchronizers) {
                trackingdebugsynchronizer.startTrackingChunk(entityplayer, chunkcoordintpair);
            }

        }
    }

    public void startTrackingEntity(EntityPlayer entityplayer, Entity entity) {
        if (!this.sleeping) {
            for (TrackingDebugSynchronizer<?> trackingdebugsynchronizer : this.allSynchronizers) {
                trackingdebugsynchronizer.startTrackingEntity(entityplayer, entity);
            }

        }
    }

    public void registerPoi(VillagePlaceRecord villageplacerecord) {
        if (!this.sleeping) {
            this.poiSynchronizer.onPoiAdded(this.level, villageplacerecord);
            this.villageSectionSynchronizer.onPoiAdded(this.level, villageplacerecord);
        }
    }

    public void updatePoi(BlockPosition blockposition) {
        if (!this.sleeping) {
            this.poiSynchronizer.onPoiTicketCountChanged(this.level, blockposition);
        }
    }

    public void dropPoi(BlockPosition blockposition) {
        if (!this.sleeping) {
            this.poiSynchronizer.onPoiRemoved(this.level, blockposition);
            this.villageSectionSynchronizer.onPoiRemoved(this.level, blockposition);
        }
    }

    public boolean hasAnySubscriberFor(DebugSubscription<?> debugsubscription) {
        return this.enabledSubscriptions.contains(debugsubscription);
    }

    public <T> void sendBlockValue(BlockPosition blockposition, DebugSubscription<T> debugsubscription, T t0) {
        if (this.hasAnySubscriberFor(debugsubscription)) {
            this.broadcastToTracking(new ChunkCoordIntPair(blockposition), debugsubscription, new ClientboundDebugBlockValuePacket(blockposition, debugsubscription.packUpdate(t0)));
        }

    }

    public <T> void clearBlockValue(BlockPosition blockposition, DebugSubscription<T> debugsubscription) {
        if (this.hasAnySubscriberFor(debugsubscription)) {
            this.broadcastToTracking(new ChunkCoordIntPair(blockposition), debugsubscription, new ClientboundDebugBlockValuePacket(blockposition, debugsubscription.emptyUpdate()));
        }

    }

    public <T> void sendEntityValue(Entity entity, DebugSubscription<T> debugsubscription, T t0) {
        if (this.hasAnySubscriberFor(debugsubscription)) {
            this.broadcastToTracking(entity, debugsubscription, new ClientboundDebugEntityValuePacket(entity.getId(), debugsubscription.packUpdate(t0)));
        }

    }

    public <T> void clearEntityValue(Entity entity, DebugSubscription<T> debugsubscription) {
        if (this.hasAnySubscriberFor(debugsubscription)) {
            this.broadcastToTracking(entity, debugsubscription, new ClientboundDebugEntityValuePacket(entity.getId(), debugsubscription.emptyUpdate()));
        }

    }

    public <T> void broadcastEventToTracking(BlockPosition blockposition, DebugSubscription<T> debugsubscription, T t0) {
        if (this.hasAnySubscriberFor(debugsubscription)) {
            this.broadcastToTracking(new ChunkCoordIntPair(blockposition), debugsubscription, new ClientboundDebugEventPacket(debugsubscription.packEvent(t0)));
        }

    }

    private void broadcastToTracking(ChunkCoordIntPair chunkcoordintpair, DebugSubscription<?> debugsubscription, Packet<? super PacketListenerPlayOut> packet) {
        PlayerChunkMap playerchunkmap = this.level.getChunkSource().chunkMap;

        for (EntityPlayer entityplayer : playerchunkmap.getPlayers(chunkcoordintpair, false)) {
            if (entityplayer.debugSubscriptions().contains(debugsubscription)) {
                entityplayer.connection.send(packet);
            }
        }

    }

    private void broadcastToTracking(Entity entity, DebugSubscription<?> debugsubscription, Packet<? super PacketListenerPlayOut> packet) {
        PlayerChunkMap playerchunkmap = this.level.getChunkSource().chunkMap;

        playerchunkmap.sendToTrackingPlayersFiltered(entity, packet, (entityplayer) -> {
            return entityplayer.debugSubscriptions().contains(debugsubscription);
        });
    }
}
