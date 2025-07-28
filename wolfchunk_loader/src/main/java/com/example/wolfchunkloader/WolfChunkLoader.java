package com.example.wolfchunkloader;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod("wolfchunkloader")
public class WolfChunkLoader {

    public static final TicketType<ChunkPos> WOLF_LOADER_TICKET = TicketType.create("wolf_loader", ChunkPos::hashCode);

    public WolfChunkLoader() {}

    @SubscribeEvent
    public void onServerTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isClientSide()) return;

        ServerLevel level = (ServerLevel) event.world;

        if (level.getGameTime() % 20 != 0) return;

        AABB area = new AABB(
                level.getWorldBorder().getMinX(), 0, level.getWorldBorder().getMinZ(),
                level.getWorldBorder().getMaxX(), level.getMaxBuildHeight(), level.getWorldBorder().getMaxZ()
        );

        List<Wolf> wolves = level.getEntitiesOfClass(Wolf.class, area);
        List<Cat> cats = level.getEntitiesOfClass(Cat.class, area);

        for (Wolf wolf : wolves) {
            if (wolf.isTame()) {
                ChunkPos pos = new ChunkPos(wolf.blockPosition());
                level.getChunkSource().addRegionTicket(WOLF_LOADER_TICKET, pos, 2, pos);
            }
        }

        for (Cat cat : cats) {
            if (cat.isTame()) {
                ChunkPos pos = new ChunkPos(cat.blockPosition());
                level.getChunkSource().addRegionTicket(WOLF_LOADER_TICKET, pos, 2, pos);
            }
        }
    }
}
