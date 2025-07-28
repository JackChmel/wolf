package com.example.wolfchunkloader;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.cat.Cat;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.chunk.LevelChunk;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;

@Mod("wolfchunkloader")
public class WolfChunkLoader {
    private static final TicketType<ChunkPos> WOLF_LOADER_TICKET = TicketType.create("wolf_loader", Comparator.comparingLong(ChunkPos::toLong));
    private int tickCounter = 0;

    public WolfChunkLoader() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if ((entity instanceof Wolf wolf && wolf.isTame()) || (entity instanceof Cat cat && cat.isTame())) {
                    ChunkPos pos = new ChunkPos(entity.blockPosition());
                    DistanceManager distanceManager = level.getChunkSource().chunkMap.getDistanceManager();

                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            ChunkPos chunk = new ChunkPos(pos.x + dx, pos.z + dz);
                            distanceManager.addTicket(WOLF_LOADER_TICKET, chunk, 2, chunk);
                        }
                    }
                }
            }
        }
    }
}
