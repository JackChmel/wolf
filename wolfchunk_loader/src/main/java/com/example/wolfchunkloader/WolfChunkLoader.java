package com.example.wolfchunkloader;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod("wolfchunkloader")
public class WolfChunkLoader {

    private static int tickCounter = 0;

    public WolfChunkLoader() {
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("WolfChunkLoader mod byl úspěšně načten!");
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < 400) return; // Každých 400 ticků = 20 sekund
        tickCounter = 0;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if ((entity instanceof Wolf wolf && wolf.isTame()) ||
                    (entity instanceof Cat cat && cat.isTame())) {
                    
                    ChunkPos center = new ChunkPos(entity.blockPosition());
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            ChunkPos chunk = new ChunkPos(center.x + dx, center.z + dz);
                            level.setChunkForced(chunk.x, chunk.z, true);
                        }
                    }
                }
            }
        }
    }
}
