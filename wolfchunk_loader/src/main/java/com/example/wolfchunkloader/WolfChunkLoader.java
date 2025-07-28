package com.example.wolfchunkloader;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.HashSet;
import java.util.Set;

@Mod("wolfchunkloader")
public class WolfChunkLoader {

    public WolfChunkLoader() {
        if (FMLLoader.getDist().isDedicatedServer() || FMLLoader.getDist().isClient()) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            Set<ChunkPos> forcedChunks = new HashSet<>();

            for (Entity entity : level.getEntities().getAll()) {
                if ((entity instanceof Wolf wolf && wolf.isTame()) || (entity instanceof Cat cat && cat.isTame())) {
                    ChunkPos pos = new ChunkPos(entity.blockPosition());
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            forcedChunks.add(new ChunkPos(pos.x + dx, pos.z + dz));
                        }
                    }
                }
            }

            // Nejprve zrušíme všechny předešlé forced chunky
            for (ChunkPos pos : level.getForcedChunks()) {
                level.setChunkForced(pos.x, pos.z, false);
            }

            // A nastavíme nové chunky
            for (ChunkPos pos : forcedChunks) {
                level.setChunkForced(pos.x, pos.z, true);
            }
        }
    }
}
