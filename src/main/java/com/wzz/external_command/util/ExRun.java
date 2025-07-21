package com.wzz.external_command.util;

import com.wzz.external_command.gui.ExUI;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class ExRun {

    public static void run(String command, MinecraftServer server) {
        runWithResult(command, server, null);
    }

    public static boolean runWithResult(String command, MinecraftServer server, CommandResultCapture capture) {
        try {
            CommandSourceStack commandSource = createCommandSource(server);
            CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
            ParseResults<CommandSourceStack> parseResults = dispatcher.parse(command, commandSource);
            if (parseResults.getReader().canRead()) {
                String error = "Unknown or incomplete command, see below for error";
                if (capture != null) {
                    capture.onError(error);
                    capture.onError(command + "<--[HERE]");
                }
                ExUI.appendToOutput("错误", error);
                ExUI.appendToOutput("错误", command + "<--[此处]");
                return false;
            }
            OutputCapture outputCapture = new OutputCapture();
            CommandSourceStack captureSource = createCaptureCommandSource(server, outputCapture);
            ParseResults<CommandSourceStack> captureParseResults = dispatcher.parse(command, captureSource);
            int result = dispatcher.execute(captureParseResults);
            if (result > 0) {
                if (capture != null && outputCapture.hasOutput()) {
                    capture.onOutput(outputCapture.getOutput());
                }
                if (outputCapture.hasOutput()) {
                    ExUI.appendToOutput("服务器", outputCapture.getOutput());
                }
                return true;
            } else {
                String error = "Command returned result: " + result;
                if (capture != null) {
                    capture.onError(error);
                }
                ExUI.appendToOutput("错误", error);
                return false;
            }
            
        } catch (CommandSyntaxException e) {
            String error = "Command syntax error: " + e.getMessage();
            if (capture != null) {
                capture.onError(error);
            }
            ExUI.appendToOutput("错误", error);
            return false;
            
        } catch (Exception e) {
            String error = "Command execution error: " + e.getMessage();
            if (capture != null) {
                capture.onError(error);
            }
            ExUI.appendToOutput("错误", error);
            e.printStackTrace();
            return false;
        }
    }

    private static CommandSourceStack createCommandSource(MinecraftServer server) {
        return new CommandSourceStack(
            CommandSource.NULL,
            Vec3.ZERO,
            Vec2.ZERO,
            server.overworld(),
            4,
            "ExternalCommand",
            Component.literal("External Command"),
            server,
            null
        );
    }

    private static CommandSourceStack createCaptureCommandSource(MinecraftServer server, OutputCapture capture) {
        return new CommandSourceStack(
            new CaptureCommandSource(capture),
            Vec3.ZERO,
            Vec2.ZERO,
            server.overworld(),
            4,
            "ExternalCommand",
            Component.literal("External Command"),
            server,
            null
        );
    }

    private static class OutputCapture {
        private StringBuilder output = new StringBuilder();
        
        public void capture(Component message) {
            if (output.length() > 0) {
                output.append("\n");
            }
            output.append(message.getString());
        }
        
        public String getOutput() {
            return output.toString();
        }
        
        public boolean hasOutput() {
            return output.length() > 0;
        }
    }

    private static class CaptureCommandSource implements CommandSource {
        private final OutputCapture capture;
        
        public CaptureCommandSource(OutputCapture capture) {
            this.capture = capture;
        }
        
        @Override
        public void sendSystemMessage(Component component) {
            capture.capture(component);
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }
        
        @Override
        public boolean acceptsFailure() {
            return true;
        }
        
        @Override
        public boolean shouldInformAdmins() {
            return false;
        }
    }

    public interface CommandResultCapture {
        void onOutput(String output);
        void onError(String error);
    }
}