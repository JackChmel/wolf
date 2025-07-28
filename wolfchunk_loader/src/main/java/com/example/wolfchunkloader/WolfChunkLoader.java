package com.example.wolfchunkloader;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("wolfchunkloader")
public class WolfChunkLoader {
    private int tickCounter = 0;

    public WolfChunkLoader() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isClientSide()) return;

        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;

        if (!(event.world instanceof ServerLevel serverLevel)) return;

        ServerChunkCache chunkSource = serverLevel.getChunkSource();

        for (Entity entity : serverLevel.getAllEntities()) {
            boolean shouldLoad = false;

            if (entity instanceof Wolf wolf && wolf.isTame()) {
                shouldLoad = true;
            }

            if (entity instanceof Cat cat && cat.isTame()) {
                shouldLoad = true;
            }

            if (shouldLoad) {
                ChunkPos center = new ChunkPos(entity.blockPosition());
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        chunkSource.chunkMap.forceChunk(center.x + dx, center.z + dz, true);
                    }
                }
            }
        }
    }
}
