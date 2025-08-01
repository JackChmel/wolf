package com.example.wolfchunkloader;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;  // ← přidáno

import java.util.*;

@Mod("wolfchunkloader")
@Mod.EventBusSubscriber
public class WolfChunkLoader {

    private static final HashMap<UUID, ChunkPos> WOLF_POSITIONS = new HashMap<>();
    private static final HashMap<UUID, Set<ChunkPos>> WOLF_CHUNKS = new HashMap<>();
    private static final HashMap<ChunkPos, Set<UUID>> CHUNK_WOLVES = new HashMap<>();

    private static int tickCounter = 0;

    public WolfChunkLoader() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        // změna: getAllLevels() → levels()
        for (ServerLevel level : server.levels()) {
            initializeForWorld(level);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;
            for (ServerLevel level : levelsFromServer()) {
                for (Entity entity : level.getAllEntities()) {
                    if (entity instanceof TamableAnimal tamable &&
                        (tamable instanceof Wolf || tamable instanceof Cat) &&
                        tamable.isTame()) {
                        onMobTick(tamable);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof TamableAnimal tamable &&
            (tamable instanceof Wolf || tamable instanceof Cat) &&
            tamable.isTame() && !entity.level().isClientSide()) {
            onMobRemoved(tamable);
        }
    }

    private static List<ServerLevel> levelsFromServer() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        // změna: StreamSupport nebo getAllLevels() nepotřebujeme, stačí levels()
        return server != null ? server.levels() : Collections.emptyList();
    }

    private static void initializeForWorld(ServerLevel level) {
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof TamableAnimal tamable &&
                tamable.isTame() &&
                (tamable instanceof Wolf || tamable instanceof Cat)) {
                Set<ChunkPos> chunks = getChunksAroundMob(tamable);
                forceLoadChunks(level, chunks);
                WOLF_CHUNKS.put(tamable.getUUID(), chunks);
            }
        }
    }

    private static Set<ChunkPos> getChunksAroundMob(TamableAnimal mob) {
        ChunkPos center = new ChunkPos(mob.blockPosition());
        Set<ChunkPos> chunkSet = new HashSet<>();
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                chunkSet.add(new ChunkPos(center.x + x, center.z + z));
            }
        }
        return chunkSet;
    }

    private static void forceLoadChunks(ServerLevel level, Set<ChunkPos> chunks) {
        for (ChunkPos chunk : chunks) {
            level.setChunkForced(chunk.x, chunk.z, true);
        }
    }

    private static void onMobTick(TamableAnimal mob) {
        UUID id = mob.getUUID();
        ChunkPos currentChunk = new ChunkPos(mob.blockPosition());
        ChunkPos lastKnownChunk = WOLF_POSITIONS.get(id);
        if (lastKnownChunk == null || !lastKnownChunk.equals(currentChunk)) {
            updateMobChunkLoading(mob, currentChunk, lastKnownChunk);
            WOLF_POSITIONS.put(id, currentChunk);
        }
    }

    private static void updateMobChunkLoading(TamableAnimal mob, ChunkPos newCenter, ChunkPos oldCenter) {
        UUID id = mob.getUUID();
        ServerLevel level = (ServerLevel) mob.level();
        Set<ChunkPos> newChunks = getChunksAroundMob(mob);
        Set<ChunkPos> oldChunks = WOLF_CHUNKS.getOrDefault(id, new HashSet<>());
        Set<ChunkPos> chunksToUnload = new HashSet<>(oldChunks);
        chunksToUnload.removeAll(newChunks);
        Set<ChunkPos> chunksToLoad = new HashSet<>(newChunks);
        chunksToLoad.removeAll(oldChunks);

        for (ChunkPos chunk : chunksToUnload) {
            removeMobFromChunk(id, chunk);
            if (!CHUNK_WOLVES.containsKey(chunk) || CHUNK_WOLVES.get(chunk).isEmpty()) {
                level.setChunkForced(chunk.x, chunk.z, false);
                CHUNK_WOLVES.remove(chunk);
            }
        }
        for (ChunkPos chunk : chunksToLoad) {
            addMobToChunk(id, chunk);
            level.setChunkForced(chunk.x, chunk.z, true);
        }
        WOLF_CHUNKS.put(id, newChunks);
    }

    private static void addMobToChunk(UUID id, ChunkPos chunk) {
        CHUNK_WOLVES.computeIfAbsent(chunk, k -> new HashSet<>()).add(id);
    }

    private static void removeMobFromChunk(UUID id, ChunkPos chunk) {
        Set<UUID> mobSet = CHUNK_WOLVES.get(chunk);
        if (mobSet != null) mobSet.remove(id);
    }

    private static void onMobRemoved(TamableAnimal mob) {
        UUID id = mob.getUUID();
        Set<ChunkPos> chunks = WOLF_CHUNKS.remove(id);
        WOLF_POSITIONS.remove(id);
        if (chunks != null) {
            ServerLevel level = (ServerLevel) mob.level();
            for (ChunkPos chunk : chunks) {
                removeMobFromChunk(id, chunk);
                if (!CHUNK_WOLVES.containsKey(chunk) || CHUNK_WOLVES.get(chunk).isEmpty()) {
                    level.setChunkForced(chunk.x, chunk.z, false);
                    CHUNK_WOLVES.remove(chunk);
                }
            }
        }
    }
}
