package com.wzz.external_command;

import com.mojang.logging.LogUtils;
import com.wzz.external_command.command.ExCMD;
import com.wzz.external_command.gui.ExUI;
import com.wzz.external_command.util.OSHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ModMain.MODID)
public class ModMain {
    public static final String MODID = "external_command";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ModMain() {
        MinecraftForge.EVENT_BUS.register(this);
        System.setProperty("java.awt.headless", "false");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        if (OSHelper.isAndroid()) {
            LOGGER.warn("Check Is Android Stop Command Register");
            return;
        }
        new ExCMD().register(event.getDispatcher());
        LOGGER.info("Excmd Command Was Registered");
    }

    @SubscribeEvent
    public void onServerStart(ServerStartedEvent event) {
        if (OSHelper.isAndroid()) {
            LOGGER.warn("Check Is Android Stop Open GUI");
            return;
        }
        ExUI.run(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        if (OSHelper.isAndroid()) {
            return;
        }
        ExUI.stop();
    }
}