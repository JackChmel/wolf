package com.example.wolfchunkloader;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkLoader;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.Ticket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("wolfchunkloader")
public class WolfChunkLoader {
    public static final TicketType<String> WOLF_CHUNK = TicketType.create("wolf_chunk", String::compareTo);

    @SubscribeEvent
    public void onServerTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) return;

        ServerLevel level = (ServerLevel) event.level;

        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof Wolf wolf && wolf.isTame())) continue;

            ChunkPos chunkPos = new ChunkPos(wolf.blockPosition());
            level.getChunkSource().addRegionTicket(WOLF_CHUNK, chunkPos, 2, "wolf");
        }
    }
}
