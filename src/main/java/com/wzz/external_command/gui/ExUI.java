package com.wzz.external_command.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.wzz.external_command.util.ExRun;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class ExUI extends JFrame {
    private static JTextArea outputArea;
    private static JTextArea inputArea;
    private static JFrame jFrame;
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public static void stop() {
        if (jFrame != null) {
            SwingUtilities.invokeLater(() -> {
                jFrame.dispose();
                jFrame = null;
            });
        }
    }

    private static String translate(String key) {
        return Component.translatable(key).getString();
    }

    private static String translate(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }

    public static void run(MinecraftServer server) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new MetalLookAndFeel());
            } catch (Exception ignored) {
            }

            jFrame = new JFrame(translate("exui.window.title"));
//            jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//
//            jFrame.addWindowListener(new WindowAdapter() {
//                @Override
//                public void windowClosed(WindowEvent e) {
//                    jFrame = null;
//                }
//            });
            jFrame.setSize(800, 600);
            jFrame.setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            outputArea = new JTextArea();
            outputArea.setEditable(false);
            outputArea.setBackground(Color.BLACK);
            outputArea.setForeground(Color.GREEN);
            outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            outputArea.setText(translate("exui.console.header") + "\n" +
                    getCurrentTime() + " " + translate("exui.console.initialized") + "\n" +
                    getCurrentTime() + " " + translate("exui.console.ready") + "\n\n");

            JScrollPane outputScrollPane = new JScrollPane(outputArea);
            outputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            outputScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLoweredBevelBorder(),
                    translate("exui.panel.console_output"),
                    TitledBorder.LEFT,
                    TitledBorder.TOP));
            outputScrollPane.setPreferredSize(new Dimension(780, 350));

            JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
            inputPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createRaisedBevelBorder(),
                    translate("exui.panel.command_input"),
                    TitledBorder.LEFT,
                    TitledBorder.TOP));

            inputArea = new JTextArea(3, 50);
            inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            inputArea.setLineWrap(true);
            inputArea.setWrapStyleWord(true);

            JScrollPane inputScrollPane = new JScrollPane(inputArea);
            inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton runButton = new JButton(translate("exui.button.execute"));
            JButton clearOutputButton = new JButton(translate("exui.button.clear_output"));
            JButton clearInputButton = new JButton(translate("exui.button.clear_input"));

            runButton.setPreferredSize(new Dimension(140, 30));
            clearOutputButton.setPreferredSize(new Dimension(120, 30));
            clearInputButton.setPreferredSize(new Dimension(120, 30));

            runButton.setBackground(new Color(34, 139, 34));
            runButton.setForeground(Color.WHITE);
            runButton.setFocusPainted(false);

            clearOutputButton.setBackground(new Color(255, 140, 0));
            clearOutputButton.setForeground(Color.WHITE);
            clearOutputButton.setFocusPainted(false);

            clearInputButton.setBackground(new Color(70, 130, 180));
            clearInputButton.setForeground(Color.WHITE);
            clearInputButton.setFocusPainted(false);

            buttonPanel.add(clearInputButton);
            buttonPanel.add(clearOutputButton);
            buttonPanel.add(runButton);

            inputPanel.add(inputScrollPane, BorderLayout.CENTER);
            inputPanel.add(buttonPanel, BorderLayout.SOUTH);

            JPanel statusPanel = new JPanel(new BorderLayout());
            JLabel statusLabel = new JLabel(translate("exui.status.ready"));
            statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            statusPanel.add(statusLabel, BorderLayout.WEST);
            statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());

            mainPanel.add(outputScrollPane, BorderLayout.CENTER);
            mainPanel.add(inputPanel, BorderLayout.SOUTH);

            jFrame.add(mainPanel, BorderLayout.CENTER);
            jFrame.add(statusPanel, BorderLayout.SOUTH);

            jFrame.setVisible(true);

            runButton.addActionListener(e -> executeCommand(server, statusLabel));

            clearOutputButton.addActionListener(e -> {
                outputArea.setText(translate("exui.console.cleared") + "\n" +
                        getCurrentTime() + " " + translate("exui.console.cleared_by_user") + "\n\n");
                appendToOutput(translate("exui.log.system"), translate("exui.message.output_cleared"));
            });

            clearInputButton.addActionListener(e -> {
                inputArea.setText("");
                inputArea.requestFocus();
            });

            // 支持Enter键执行命令 (Ctrl+Enter)
            inputArea.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                        executeCommand(server, statusLabel);
                        e.consume();
                    }
                }
            });

            // 初始焦点设置到输入区域
            inputArea.requestFocus();

            appendToOutput(translate("exui.log.system"), translate("exui.message.interface_loaded"));
        });
    }

    private static void executeCommand(MinecraftServer server, JLabel statusLabel) {
        String inputText = inputArea.getText().trim();
        if (inputText.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    translate("exui.error.command_empty"),
                    translate("exui.error.input_error"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 按行分割命令
        String[] commands = inputText.split("\n");
        int totalCommands = 0;

        // 计算非空命令数量
        for (String cmd : commands) {
            if (!cmd.trim().isEmpty()) totalCommands++;
        }

        if (totalCommands == 0) return;

        int successCount = 0;
        int errorCount = 0;

        if (totalCommands > 1) {
            appendToOutput(translate("exui.log.system"), translate("exui.message.batch_start", totalCommands));
        }
        statusLabel.setText(translate("exui.status.executing"));

        // 逐个执行命令
        int commandIndex = 0;
        for (String command : commands) {
            command = command.trim();
            if (command.isEmpty()) continue; // 跳过空行

            commandIndex++;
            if (totalCommands > 1) {
                appendToOutput(translate("exui.log.user"), translate("exui.message.executing_single", commandIndex, command));
            } else {
                appendToOutput(translate("exui.log.user"), translate("exui.message.executing", command));
            }

            try {
                // 捕获系统输出来获取命令结果
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                java.io.PrintStream originalOut = System.out;
                java.io.PrintStream originalErr = System.err;

                try {
                    System.setOut(new java.io.PrintStream(baos));
                    System.setErr(new java.io.PrintStream(baos));
                    ExRun.run(command, server);
                    String output = baos.toString().trim();
                    if (!output.isEmpty()) {
                        appendToOutput(translate("exui.log.server"), output);
                    }
                    successCount++;
                    if (totalCommands > 1) {
                        appendToOutput(translate("exui.log.system"), translate("exui.message.command_single_success", commandIndex));
                    }
//                    else {
//                        appendToOutput(translate("exui.log.system"), translate("exui.message.command_success"));
//                    }

                } finally {
                    System.setOut(originalOut);
                    System.setErr(originalErr);
                }

            } catch (Exception ex) {
                errorCount++;
                if (totalCommands > 1) {
                    appendToOutput(translate("exui.log.error"), translate("exui.message.command_single_failed", commandIndex));
                }
                appendToOutput(translate("exui.log.error"), translate("exui.message.command_exception", commandIndex, ex.getMessage()));
            }
            if (commandIndex < totalCommands && totalCommands > 1) {
                appendToOutput("", "");
            }
        }
        if (totalCommands > 1) {
            appendToOutput(translate("exui.log.system"), translate("exui.message.batch_summary", totalCommands, successCount, errorCount));
        }
        if (errorCount == 0) {
            statusLabel.setText(translate("exui.status.completed"));
        } else if (successCount == 0) {
            statusLabel.setText(translate("exui.status.error"));
        } else {
            statusLabel.setText(translate("exui.status.partial_success"));
        }
        inputArea.setText("");
        Timer timer = new Timer(3000, e -> statusLabel.setText(translate("exui.status.ready")));
        timer.setRepeats(false);
        timer.start();
    }

    public static void appendToOutput(String type, String message) {
        if (outputArea != null) {
            SwingUtilities.invokeLater(() -> {
                String timestamp = getCurrentTime();
                String formattedMessage = String.format("[%s] %s: %s\n", timestamp, type, message);
                outputArea.append(formattedMessage);
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
            });
        }
    }

    private static String getCurrentTime() {
        return timeFormat.format(new Date());
    }

    public static void logInfo(String message) {
        appendToOutput("Info", message);
    }

    public static void logError(String message) {
        appendToOutput("Error", message);
    }

    public static void logWarning(String message) {
        appendToOutput("Warning", message);
    }

    public static void logServer(String message) {
        appendToOutput("Server", message);
    }
}