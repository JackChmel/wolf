package com.example.wolfchunkloader;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("wolfchunkloader")
public class WolfChunkLoader {
    private static int tickCounter = 0;

    public WolfChunkLoader() {
        // Mod constructor (prázdný nebo logování)
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isClientSide()) return;

        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;

        Level level = event.world;
        if (!(level instanceof ServerLevel serverLevel)) return;

        ServerChunkCache chunkSource = serverLevel.getChunkSource();

        for (Entity entity : serverLevel.getAllEntities()) {
            if ((entity instanceof Wolf wolf && wolf.isTame()) || (entity instanceof Cat cat && cat.isTame())) {
                ChunkPos centerPos = new ChunkPos(entity.blockPosition());
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        chunkSource.chunkMap.forceChunk(centerPos.x + dx, centerPos.z + dz, true);
                    }
                }
            }
        }
    }
}
