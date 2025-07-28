package com.example.wolfchunkloader;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.HashSet;
import java.util.Set;

@Mod("wolfchunkloader")
@EventBusSubscriber
public class WolfChunkLoader {

    public WolfChunkLoader() {
        System.out.println("WolfChunkLoader mod byl úspěšně načten!");
    }

    // Každý server tick kontroluj chunky
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = event.getServer();

        for (ServerLevel level : server.getAllLevels()) {
            // Zruší předchozí forced chunky
            level.getForcedChunks().clear();

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
