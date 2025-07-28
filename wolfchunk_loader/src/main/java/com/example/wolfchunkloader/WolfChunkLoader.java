package com.example.wolfchunkloader;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod("wolfchunkloader")
@Mod.EventBusSubscriber
public class WolfChunkLoader {

    public WolfChunkLoader() {
        System.out.println("WolfChunkLoader mod byl úspěšně načten!");
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = event.getServer();

        for (ServerLevel level : server.getAllLevels()) {
            // Zruší všechny dříve načtené chunky (forced)
            Set<Long> forcedChunks = new HashSet<>(level.getForcedChunks());
            for (Long chunkLong : forcedChunks) {
                ChunkPos pos = new ChunkPos(chunkLong);
                level.setChunkForced(pos.x, pos.z, false);
            }

            // Pro každého ochočeného vlka načti chunky v rozsahu 3x3
            for (Wolf wolf : level.getEntitiesOfClass(Wolf.class, level.getWorldBorder().getCollisionShape().bounds())) {
                if (wolf.isTame()) {
                    ChunkPos center = new ChunkPos(wolf.blockPosition());
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            ChunkPos chunkPos = new ChunkPos(center.x + dx, center.z + dz);
                            level.setChunkForced(chunkPos.x, chunkPos.z, true);
                        }
                    }
                }
            }
        }
    }
}
