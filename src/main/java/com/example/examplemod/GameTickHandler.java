package com.example.examplemod;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GameTickHandler {
    // 监听服务器的每一帧 (Tick)
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        // 只在每帧结束时执行一次，防止双重触发
        if (event.phase == TickEvent.Phase.END) {
            GameLogic.onTick();
        }
    }
}