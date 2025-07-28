package com.example.wolfchunkloader;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;

@Mod("wolfchunkloader")
public class WolfChunkLoader {
    public WolfChunkLoader() {
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("[WolfChunkLoader] Mod byl načten!");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(ServerLevel.OVERWORLD);
        if (level == null) return;

        List<Wolf> wolves = level.getEntitiesOfClass(Wolf.class, level.getWorldBorder().getBoundingBox());
        List<Cat> cats = level.getEntitiesOfClass(Cat.class, level.getWorldBorder().getBoundingBox());

        for (Wolf wolf : wolves) {
            if (wolf.isTame()) {
                loadChunkArea(level, wolf.chunkPosition());
            }
        }

        for (Cat cat : cats) {
            if (cat.isTame()) {
                loadChunkArea(level, cat.chunkPosition());
            }
        }
    }

    private void loadChunkArea(ServerLevel level, ChunkPos center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos pos = new ChunkPos(center.x + dx, center.z + dz);
                level.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.POST_TELEPORT, pos, 2, pos);
            }
        }
    }
}

