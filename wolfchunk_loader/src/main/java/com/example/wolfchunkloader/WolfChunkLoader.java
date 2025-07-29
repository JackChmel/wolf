package com.example.wolfchunkloader;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod("wolfchunkloader")
public class WolfChunkLoader {

    private static final Map<UUID, Set<ChunkPos>> entityChunks = new HashMap<>();
    private static final Map<ChunkPos, Set<UUID>> chunkEntities = new HashMap<>();

    public WolfChunkLoader() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            updateAllEntities(level);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;

        for (ServerLevel level : Objects.requireNonNull(event.getServer()).getAllLevels()) {
            updateAllEntities(level);
        }
    }

    private void updateAllEntities(ServerLevel level) {
        Map<UUID, ChunkPos> currentPositions = new HashMap<>();
        List<UUID> seen = new ArrayList<>();

        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof Wolf || entity instanceof Cat)) continue;
            if (!entity.isTame()) continue;

            UUID id = entity.getUUID();
            ChunkPos center = new ChunkPos(entity.blockPosition());
            Set<ChunkPos> newChunks = getChunksAround(center);

            // Unload old chunks
            Set<ChunkPos> oldChunks = entityChunks.getOrDefault(id, Set.of());
            for (ChunkPos chunk : oldChunks) {
                if (!newChunks.contains(chunk)) {
                    removeEntityFromChunk(id, chunk);
                    if (!chunkEntities.containsKey(chunk) || chunkEntities.get(chunk).isEmpty()) {
                        level.setChunkForced(chunk.x, chunk.z, false);
                    }
                }
            }

            // Load new chunks
            for (ChunkPos chunk : newChunks) {
                if (!oldChunks.contains(chunk)) {
                    level.setChunkForced(chunk.x, chunk.z, true);
                    chunkEntities.computeIfAbsent(chunk, k -> new HashSet<>()).add(id);
                }
            }

            entityChunks.put(id, newChunks);
            seen.add(id);
        }

        // Cleanup removed entities
        entityChunks.keySet().removeIf(id -> {
            if (seen.contains(id)) return false;
            Set<ChunkPos> chunks = entityChunks.get(id);
            for (ChunkPos chunk : chunks) {
                removeEntityFromChunk(id, chunk);
                if (!chunkEntities.containsKey(chunk) || chunkEntities.get(chunk).isEmpty()) {
                    level.setChunkForced(chunk.x, chunk.z, false);
                }
            }
            return true;
        });
    }

    private static Set<ChunkPos> getChunksAround(ChunkPos center) {
        Set<ChunkPos> set = new HashSet<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set.add(new ChunkPos(center.x + dx, center.z + dz));
            }
        }
        return set;
    }

    private static void removeEntityFromChunk(UUID id, ChunkPos chunk) {
        Set<UUID> entities = chunkEntities.get(chunk);
        if (entities != null) {
            entities.remove(id);
            if (entities.isEmpty()) {
                chunkEntities.remove(chunk);
            }
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ChunkPos chunk : chunkEntities.keySet()) {
                level.setChunkForced(chunk.x, chunk.z, false);
            }
        }
        entityChunks.clear();
        chunkEntities.clear();
    }
}
