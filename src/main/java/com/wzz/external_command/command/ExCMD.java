package com.wzz.external_command.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.wzz.external_command.ModMain;
import com.wzz.external_command.gui.ExUI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ExCMD {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("excmd")
                .requires(source -> source.hasPermission(4))
                .executes(context -> {
                    try {
                        ExUI.run(context.getSource().getServer());
                    } catch (Exception exception) {
                        context.getSource().sendFailure(Component.literal("ExUI error occurred"));
                        ModMain.LOGGER.error("ExUI was error", exception);
                    }
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}