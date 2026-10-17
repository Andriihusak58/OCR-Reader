import com.formdev.flatlaf.FlatDarkLaf;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/** Панель керування ботами: режим вводу, цілі, запуск/зупинка, лог і статус. */
public class BotGui {

    private static final int STOP_KEY = NativeKeyEvent.VC_F9;
    private static final String STOP_KEY_NAME = "F9";

    private static final String MODE_ROBOT = "Robot (PC screen)";
    private static final String MODE_ADB = "ADB (LDPlayer)";

    private static final int MAX_LOG_LINES = 2000;

    private static final Color GREEN = new Color(0x4CAF50);
    private static final Color AMBER = new Color(0xFFB300);
    private static final Color RED = new Color(0xE53935);
    private static final Color GRAY = new Color(0x9E9E9E);

    private final Preferences prefs = Preferences.userNodeForPackage(BotGui.class);

    private volatile Main mainBot;
    private volatile Builder builderBot;
    private volatile boolean running;
    private volatile boolean stopRequested;
    private volatile Thread botThread;

    private JFrame frame;

    private JComboBox<String> modeBox;
    private JTextField adbPathField;
    private JTextField serialField;
    private JButton browseButton;
    private JButton testButton;
    private JLabel connLabel;

    private JTextField mainGoldField;
    private JTextField mainElixirField;
    private JTextField enemyLootField;
    private JTextField builderGoldField;
    private JTextField builderElixirField;
    private JTextField mainMaxAttacksField;
    private JTextField builderMaxAttacksField;
    private JComboBox<String> rewardBox;
    private JLabel attacksLabel;
    private volatile String stopReason;

    private final TelegramNotifier telegram = new TelegramNotifier();
    private JCheckBox tgEnabled;
    private JPasswordField tgTokenField;
    private JTextField tgChatField;
    private JButton tgConnectButton;
    private JButton tgTestButton;
    private JLabel tgLabel;

    private JButton startMain;
    private JButton startBuilder;
    private JButton stopButton;

    private JLabel statusLabel;
    private JCheckBox alwaysOnTop;
    private JTextArea logArea;

    public static void main(String[] args) {
        // Java 24+ offers post-quantum key shares in the TLS hello; some networks/firewalls drop such a
        // handshake ("Remote host terminated the handshake"). GitHub works fine with the classic groups.
        System.setProperty("jdk.tls.namedGroups", "x25519,secp256r1,secp384r1,secp521r1");
        setupLookAndFeel();
        SwingUtilities.invokeLater(() -> new BotGui().create());
    }

    private static void setupLookAndFeel() {
        try {
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            FlatDarkLaf.setup();
        } catch (Throwable t) {
            // FlatLaf недоступний (наприклад, Maven не перезавантажено) -> стандартний вигляд Swing
        }
    }

    // ------------------------------------------------------------------ build

    private void create() {
        frame = new JFrame("BotOCR " + UpdateChecker.VERSION + " - Clash of Clans bot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            java.net.URL icon = BotGui.class.getResource("/icon.png");
            if (icon != null) frame.setIconImage(new ImageIcon(icon).getImage());
        } catch (Exception ignored) {
        }

        buildComponents();

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel upper = new JPanel(new BorderLayout(0, 10));
        upper.add(buildConnectionPanel(), BorderLayout.NORTH);
        upper.add(buildTelegramPanel(), BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.add(upper, BorderLayout.NORTH);
        top.add(buildBotsPanel(), BorderLayout.CENTER);

        root.add(top, BorderLayout.NORTH);
        root.add(buildLogPanel(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        frame.setContentPane(root);

        installLogCapture();
        loadSettings();
        updateControls();
        setStatus("Idle", GRAY);
        registerHotkey();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveSettings();
                stopAll();
                try {
                    GlobalScreen.unregisterNativeHook();
                } catch (Exception ignored) {
                }
            }
        });

        frame.setSize(880, 800);
        frame.setMinimumSize(new Dimension(780, 700));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        System.out.println("Ready. Press " + STOP_KEY_NAME + " at any time to stop the bot.");
        Updater.deleteOldBackup();
        checkForUpdates();
    }

    private void checkForUpdates() {
        Thread t = new Thread(() -> {
            UpdateChecker.Update update = UpdateChecker.check();
            if (update == null) return;
            System.out.println("New version " + update.version() + " is available: " + update.url());
            SwingUtilities.invokeLater(() -> offerUpdate(update));
        }, "update-check");
        t.setDaemon(true);
        t.start();
    }

    private void offerUpdate(UpdateChecker.Update update) {
        boolean auto = update.jarUrl() != null && Updater.canSelfUpdate();
        String message = "A new version " + update.version() + " is available (you have " + UpdateChecker.VERSION + ").";
        Object[] options = auto
                ? new Object[]{"Update now", "Open download page", "Later"}
                : new Object[]{"Open download page", "Later"};
        int choice = JOptionPane.showOptionDialog(frame, message, "Update available",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        String picked = choice >= 0 ? String.valueOf(options[choice]) : "Later";
        if (picked.equals("Open download page")) {
            try {
                Desktop.getDesktop().browse(java.net.URI.create(update.url()));
            } catch (Exception ignored) {
            }
        } else if (picked.equals("Update now")) {
            installUpdate(update);
        }
    }

    private void installUpdate(UpdateChecker.Update update) {
        stopAll();
        setStatus("Updating...", GRAY);
        Thread t = new Thread(() -> {
            try {
                java.nio.file.Path appDir = Updater.runningJar().toAbsolutePath().getParent();
                Updater.stage(appDir, update.jarUrl(), update.imagesUrl(), Updater.ALLOWED_PREFIX, System.out::println);
                Updater.launchSwap(appDir);
                SwingUtilities.invokeLater(() -> {
                    saveSettings();
                    try {
                        GlobalScreen.unregisterNativeHook();
                    } catch (Exception ignored) {
                    }
                    System.exit(0);
                });
            } catch (Exception e) {
                System.out.println("Update failed: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    setStatus("Idle", GRAY);
                    JOptionPane.showMessageDialog(frame,
                            "Update failed: " + e.getMessage() + "\nYou can download the new version manually:\n" + update.url(),
                            "Update", JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "self-update");
        t.setDaemon(true);
        t.start();
    }

    private void buildComponents() {
        modeBox = new JComboBox<>(new String[]{MODE_ROBOT, MODE_ADB});
        modeBox.addActionListener(e -> updateControls());

        adbPathField = new JTextField(24);
        adbPathField.putClientProperty("JTextField.placeholderText", "adb  or  C:\\LDPlayer\\LDPlayer14\\adb.exe");
        serialField = new JTextField(14);

        browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseAdb());
        testButton = new JButton("Test connection");
        testButton.addActionListener(e -> testConnection());
        connLabel = new JLabel(" ");

        mainGoldField = new JTextField(10);
        mainElixirField = new JTextField(10);
        enemyLootField = new JTextField(10);
        builderGoldField = new JTextField(10);
        builderElixirField = new JTextField(10);
        mainMaxAttacksField = new JTextField(10);
        builderMaxAttacksField = new JTextField(10);
        mainMaxAttacksField.putClientProperty("JTextField.placeholderText", "0 = no limit");
        builderMaxAttacksField.putClientProperty("JTextField.placeholderText", "0 = no limit");
        rewardBox = new JComboBox<>(new String[]{"Left card", "Middle card", "Right card"});
        rewardBox.setSelectedIndex(RewardPicker.RIGHT);
        attacksLabel = new JLabel("Attacks: 0");

        tgEnabled = new JCheckBox("Send a message when the bot stops");
        tgTokenField = new JPasswordField(24);
        tgTokenField.putClientProperty("JTextField.placeholderText", "token from @BotFather");
        tgChatField = new JTextField(12);
        tgChatField.putClientProperty("JTextField.placeholderText", "filled in by Connect");
        tgConnectButton = new JButton("Connect");
        tgConnectButton.setToolTipText("Click, then send /start to your bot in Telegram");
        tgConnectButton.addActionListener(e -> telegramConnect());
        tgTestButton = new JButton("Send test");
        tgTestButton.addActionListener(e -> telegramTest());
        tgLabel = new JLabel(" ");

        startMain = new JButton("Start Main Bot");
        startMain.setBackground(new Color(0x2E7D32));
        startMain.setForeground(Color.WHITE);
        startMain.addActionListener(e -> startBot(true));

        startBuilder = new JButton("Start Builder Bot");
        startBuilder.setBackground(new Color(0x2E7D32));
        startBuilder.setForeground(Color.WHITE);
        startBuilder.addActionListener(e -> startBot(false));

        stopButton = new JButton("Stop (" + STOP_KEY_NAME + ")");
        stopButton.setBackground(new Color(0xC62828));
        stopButton.setForeground(Color.WHITE);
        stopButton.addActionListener(e -> stopAll());

        statusLabel = new JLabel();
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));

        alwaysOnTop = new JCheckBox("Always on top");
        alwaysOnTop.addActionListener(e -> frame.setAlwaysOnTop(alwaysOnTop.isSelected()));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
    }

    private JPanel buildConnectionPanel() {
        JPanel p = titledPanel("Connection");
        GridBagConstraints c = baseConstraints();

        addRow(p, c, 0, "Input mode", modeBox, null);
        addRow(p, c, 1, "ADB path", adbPathField, browseButton);
        addRow(p, c, 2, "ADB serial", serialField, testButton);

        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 2;
        p.add(connLabel, c);
        return p;
    }

    private JPanel buildTelegramPanel() {
        JPanel p = titledPanel("Telegram notifications");
        GridBagConstraints c = baseConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        p.add(tgEnabled, c);
        c.gridwidth = 1;

        addRow(p, c, 1, "Bot token", tgTokenField, tgConnectButton);
        addRow(p, c, 2, "Chat ID", tgChatField, tgTestButton);

        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 2;
        p.add(tgLabel, c);
        return p;
    }

    private JPanel buildBotsPanel() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));

        JPanel main = titledPanel("Main base");
        GridBagConstraints c = baseConstraints();
        addRow(main, c, 0, "Target gold", mainGoldField, null);
        addRow(main, c, 1, "Target elixir", mainElixirField, null);
        addRow(main, c, 2, "Enemy min loot", enemyLootField, null);
        addRow(main, c, 3, "Stop after attacks", mainMaxAttacksField, null);
        addRow(main, c, 4, "Reward at 33%/66%", rewardBox, null);
        addButtonRow(main, c, 5, startMain);

        JPanel builder = titledPanel("Builder base");
        c = baseConstraints();
        addRow(builder, c, 0, "Target gold", builderGoldField, null);
        addRow(builder, c, 1, "Target elixir", builderElixirField, null);
        addRow(builder, c, 2, "Stop after attacks", builderMaxAttacksField, null);
        c.gridy = 3;
        c.weighty = 1; // розтягнути, щоб кнопка була внизу, як у Main
        builder.add(Box.createVerticalGlue(), c);
        c.weighty = 0;
        addButtonRow(builder, c, 4, startBuilder);

        row.add(main);
        row.add(builder);
        return row;
    }

    private JPanel buildLogPanel() {
        JPanel p = titledPanel("Log");
        p.setLayout(new BorderLayout(0, 6));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setPreferredSize(new Dimension(100, 200));
        p.add(scroll, BorderLayout.CENTER);

        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> logArea.setText(""));
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        south.add(clear);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.add(statusLabel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.add(attacksLabel);
        right.add(alwaysOnTop);
        right.add(new JLabel("Stop hotkey: " + STOP_KEY_NAME));
        right.add(stopButton);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private static JPanel titledPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                new EmptyBorder(6, 8, 8, 8)));
        return p;
    }

    private static GridBagConstraints baseConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        return c;
    }

    private static void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent field, JComponent extra) {
        c.gridy = row;
        c.gridwidth = 1;

        c.gridx = 0;
        c.weightx = 0;
        p.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 1;
        p.add(field, c);

        if (extra != null) {
            c.gridx = 2;
            c.weightx = 0;
            p.add(extra, c);
        }
    }

    private static void addButtonRow(JPanel p, GridBagConstraints c, int row, JButton button) {
        c.gridy = row;
        c.gridx = 0;
        c.gridwidth = 3;
        c.weightx = 1;
        c.insets = new Insets(10, 4, 4, 4);
        p.add(button, c);
        c.insets = new Insets(4, 4, 4, 4);
        c.gridwidth = 1;
    }

    // ---------------------------------------------------------------- actions

    private void startBot(boolean main) {
        final BotConfig config;
        try {
            config = readConfig();
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), RED);
            System.out.println("Cannot start: " + ex.getMessage());
            return;
        }

        final String name = main ? "Main bot" : "Builder bot";
        final boolean notify = tgEnabled.isSelected();
        final String tgToken = new String(tgTokenField.getPassword()).trim();
        final String tgChat = tgChatField.getText().trim();
        if (notify && (tgToken.isEmpty() || tgChat.isEmpty())) {
            System.out.println("Telegram notifications are on, but the token or chat ID is empty - press Connect first");
        }

        stopReason = null;
        attacksLabel.setText("Attacks: 0");
        final Watchdog watchdog = new Watchdog(config.stuckTimeoutMinutes * 60_000L);
        final int stuckMinutes = config.stuckTimeoutMinutes;
        config.events = new BotEvents() {
            @Override
            public void progress() {
                watchdog.touch();
            }

            @Override
            public void attackFinished(int totalAttacks) {
                watchdog.touch();
                SwingUtilities.invokeLater(() -> attacksLabel.setText("Attacks: " + totalAttacks));
            }

            @Override
            public void stopped(String reason, int totalAttacks) {
                watchdog.stop();
                if (watchdog.triggered()) {
                    reason = "No progress for " + stuckMinutes + " min - the bot seems stuck (check the game screen)";
                }
                stopReason = reason;
                System.out.println("Finished: " + reason + " (attacks: " + totalAttacks + ")");
                if (notify && !tgToken.isEmpty() && !tgChat.isEmpty() && !reason.startsWith("Stopped by user")) {
                    sendTelegram(tgToken, tgChat,
                            "BotOCR: " + name + " stopped\nReason: " + reason + "\nAttacks: " + totalAttacks);
                }
            }
        };

        saveSettings();
        final ContextFactory factory = contextFactory();

        running = true;
        stopRequested = false;
        updateControls();
        setStatus("Running: " + name, GREEN);
        System.out.println("Starting " + name + " (" + modeBox.getSelectedItem() + ")");

        Thread thread = new Thread(() -> {
            boolean failed = false;
            try {
                if (main) {
                    Main bot = new Main(config, factory);
                    mainBot = bot;
                    bot.run();
                } else {
                    Builder bot = new Builder(config, factory);
                    builderBot = bot;
                    bot.run();
                }
            } catch (InterruptedException ex) {
                // зупинка користувачем: нормальне завершення
            } catch (Exception ex) {
                // після запиту зупинки "виняток" майже завжди наслідок переривання (наприклад, ADB)
                if (!stopRequested) {
                    failed = true;
                    ex.printStackTrace();
                }
            } finally {
                mainBot = null;
                builderBot = null;
                botThread = null;
            }

            final boolean error = failed;
            SwingUtilities.invokeLater(() -> {
                running = false;
                updateControls();
                if (error) {
                    setStatus("Stopped with an error - see log", RED);
                } else if (stopReason != null && !stopReason.startsWith("Stopped by user")) {
                    setStatus("Finished: " + stopReason, stopReason.startsWith("No progress") ? RED : GRAY);
                } else {
                    setStatus("Idle", GRAY);
                }
                System.out.println(name + " finished");
            });
        }, "bot-thread");
        thread.setDaemon(true);
        botThread = thread;
        thread.start();
        watchdog.start(() -> SwingUtilities.invokeLater(this::stopAll));
    }

    private void stopAll() {
        if (!running) {
            return;
        }

        stopRequested = true;

        Main m = mainBot;
        Builder b = builderBot;
        if (m != null) {
            m.stop();
        }
        if (b != null) {
            b.stop();
        }

        // перериваємо потік бота, щоб зупинка не чекала кінця довгої паузи, скріншота чи жесту
        Thread t = botThread;
        if (t != null) {
            t.interrupt();
        }

        System.out.println("Stop requested (" + STOP_KEY_NAME + ")");
        SwingUtilities.invokeLater(() -> {
            if (running) {
                setStatus("Stopping...", AMBER);
            }
        });
    }

    private ContextFactory contextFactory() {
        final boolean adb = MODE_ADB.equals(modeBox.getSelectedItem());
        final String adbPath = adbPathField.getText().trim().isEmpty() ? "adb" : adbPathField.getText().trim();
        final String serial = serialField.getText().trim();
        if (!adb) {
            return GameContext::robot;
        }
        return () -> {
            String device = AdbLocator.resolveSerial(adbPath, serial);
            GameContext ctx = GameContext.adb(adbPath, device);
            Region bounds = ctx.screen.bounds();
            if (bounds.width != 1920 || bounds.height != 1080) {
                throw new IllegalStateException("Emulator resolution is " + bounds.width + "x" + bounds.height
                        + ", but 1920x1080 is required (LDPlayer > Settings > Game settings > Tablet 1920x1080)");
            }
            return ctx;
        };
    }

    private void browseAdb() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select adb executable");
        File current = new File(adbPathField.getText().trim());
        if (current.getParentFile() != null && current.getParentFile().isDirectory()) {
            chooser.setCurrentDirectory(current.getParentFile());
        }
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            adbPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void testConnection() {
        final String adbPath = adbPathField.getText().trim().isEmpty() ? "adb" : adbPathField.getText().trim();
        final String typedSerial = serialField.getText().trim();

        testButton.setEnabled(false);
        showConnection("Checking...", GRAY, null);

        Thread thread = new Thread(() -> {
            String text;
            Color color;
            String tooltip = null;
            try {
                // if the typed serial is not connected but adb sees another device, use that one
                String serial = typedSerial;
                String picked = AdbLocator.resolveSerial(adbPath, serial);
                if (!picked.equals(serial)) {
                    serial = picked;
                    final String shown = picked;
                    SwingUtilities.invokeLater(() -> serialField.setText(shown));
                }
                Region bounds = new AdbScreenSource(new Adb(adbPath, serial)).bounds();
                boolean ok = bounds.width == 1920 && bounds.height == 1080;
                text = (ok ? "Connected: " : "Connected, but 1920x1080 is expected: ")
                        + bounds.width + "x" + bounds.height;
                color = ok ? GREEN : AMBER;
            } catch (Exception ex) {
                String message = String.valueOf(ex.getMessage());
                text = "Failed: " + (message.length() > 90 ? message.substring(0, 90) + "..." : message);
                tooltip = message;
                color = RED;
            }

            final String t = text;
            final Color col = color;
            final String tip = tooltip;
            SwingUtilities.invokeLater(() -> {
                showConnection(t, col, tip);
                updateControls();
            });
        }, "adb-test");
        thread.setDaemon(true);
        thread.start();
    }

    // --------------------------------------------------------------- telegram

    private void sendTelegram(String token, String chatId, String text) {
        Thread t = new Thread(() -> {
            try {
                telegram.sendMessage(token, chatId, text);
                System.out.println("Telegram notification sent");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                System.out.println("Telegram notification failed: " + ex.getMessage());
            }
        }, "telegram-send");
        t.setDaemon(true);
        t.start();
    }

    private void telegramConnect() {
        final String token = new String(tgTokenField.getPassword()).trim();
        if (token.isEmpty()) {
            showTelegram("Paste the bot token from @BotFather first", RED);
            return;
        }

        tgConnectButton.setEnabled(false);
        tgTestButton.setEnabled(false);
        showTelegram("Checking token...", GRAY);

        Thread t = new Thread(() -> {
            String text;
            Color color;
            try {
                String bot = telegram.checkToken(token);
                SwingUtilities.invokeLater(() -> showTelegram(
                        "Open @" + bot + " in Telegram and send /start (waiting up to 60 s)...", AMBER));

                String chat = telegram.waitForChatId(token, 60);
                if (chat == null) {
                    text = "No message received. Send /start to @" + bot + " and press Connect again";
                    color = AMBER;
                } else {
                    final String id = chat;
                    SwingUtilities.invokeLater(() -> tgChatField.setText(id));
                    telegram.sendMessage(token, chat, "BotOCR is connected. You will get a message here when the bot stops.");
                    text = "Connected to chat " + chat;
                    color = GREEN;
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                text = "Failed: " + ex.getMessage();
                color = RED;
            }

            final String t1 = text;
            final Color c1 = color;
            SwingUtilities.invokeLater(() -> {
                showTelegram(t1, c1);
                tgConnectButton.setEnabled(true);
                tgTestButton.setEnabled(true);
            });
        }, "telegram-connect");
        t.setDaemon(true);
        t.start();
    }

    private void telegramTest() {
        final String token = new String(tgTokenField.getPassword()).trim();
        final String chat = tgChatField.getText().trim();
        if (token.isEmpty() || chat.isEmpty()) {
            showTelegram("Press Connect first (token and chat ID are needed)", RED);
            return;
        }

        tgConnectButton.setEnabled(false);
        tgTestButton.setEnabled(false);
        showTelegram("Sending...", GRAY);

        Thread t = new Thread(() -> {
            String text;
            Color color;
            try {
                telegram.sendMessage(token, chat, "BotOCR test message");
                text = "Test message sent";
                color = GREEN;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                text = "Failed: " + ex.getMessage();
                color = RED;
            }

            final String t1 = text;
            final Color c1 = color;
            SwingUtilities.invokeLater(() -> {
                showTelegram(t1, c1);
                tgConnectButton.setEnabled(true);
                tgTestButton.setEnabled(true);
            });
        }, "telegram-test");
        t.setDaemon(true);
        t.start();
    }

    private void showTelegram(String text, Color color) {
        tgLabel.setText(text);
        tgLabel.setForeground(color);
    }

    private void showConnection(String text, Color color, String tooltip) {
        connLabel.setText(text);
        connLabel.setForeground(color);
        connLabel.setToolTipText(tooltip);
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText("\u25CF " + text);
        statusLabel.setForeground(color);
    }

    /** Вмикає/вимикає елементи залежно від стану (працює бот чи ні) і вибраного режиму. */
    private void updateControls() {
        boolean idle = !running;
        boolean adb = MODE_ADB.equals(modeBox.getSelectedItem());

        modeBox.setEnabled(idle);
        adbPathField.setEnabled(idle && adb);
        serialField.setEnabled(idle && adb);
        browseButton.setEnabled(idle && adb);
        testButton.setEnabled(idle && adb);
        if (!adb) {
            showConnection(" ", GRAY, null);
        }

        mainGoldField.setEnabled(idle);
        mainElixirField.setEnabled(idle);
        enemyLootField.setEnabled(idle);
        builderGoldField.setEnabled(idle);
        builderElixirField.setEnabled(idle);
        mainMaxAttacksField.setEnabled(idle);
        builderMaxAttacksField.setEnabled(idle);
        rewardBox.setEnabled(idle);

        startMain.setEnabled(idle);
        startBuilder.setEnabled(idle);
        stopButton.setEnabled(running);
    }

    // --------------------------------------------------------------- settings

    private BotConfig readConfig() {
        BotConfig config = new BotConfig();
        config.mainTargetGold = parseNumber(mainGoldField, "Main target gold");
        config.mainTargetElixir = parseNumber(mainElixirField, "Main target elixir");
        config.enemyMinLootTotal = parseNumber(enemyLootField, "Enemy min loot");
        config.builderTargetGold = parseNumber(builderGoldField, "Builder target gold");
        config.builderTargetElixir = parseNumber(builderElixirField, "Builder target elixir");
        config.mainMaxAttacks = parseOptionalNumber(mainMaxAttacksField, "Main attack limit");
        config.builderMaxAttacks = parseOptionalNumber(builderMaxAttacksField, "Builder attack limit");
        config.rewardChoice = rewardBox.getSelectedIndex();
        return config;
    }

    /** Empty field means 0 (no limit). */
    private static int parseOptionalNumber(JTextField field, String name) {
        if (field.getText().trim().isEmpty()) {
            return 0;
        }
        return parseNumber(field, name);
    }

    private static int parseNumber(JTextField field, String name) {
        String text = field.getText().replaceAll("[\\s,._]", "");
        try {
            int value = Integer.parseInt(text);
            if (value < 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a whole number");
        }
    }

    private void loadSettings() {
        BotConfig d = new BotConfig();

        modeBox.setSelectedItem(prefs.get("mode", MODE_ROBOT));
        String savedAdb = prefs.get("adbPath", "");
        if (savedAdb.isEmpty() || savedAdb.equals("adb")) {
            String found = AdbLocator.findAdb();
            savedAdb = found != null ? found : "adb";
        }
        adbPathField.setText(savedAdb);
        serialField.setText(prefs.get("serial", "emulator-5554"));

        mainGoldField.setText(prefs.get("mainGold", String.valueOf(d.mainTargetGold)));
        mainElixirField.setText(prefs.get("mainElixir", String.valueOf(d.mainTargetElixir)));
        enemyLootField.setText(prefs.get("enemyLoot", String.valueOf(d.enemyMinLootTotal)));
        builderGoldField.setText(prefs.get("builderGold", String.valueOf(d.builderTargetGold)));
        builderElixirField.setText(prefs.get("builderElixir", String.valueOf(d.builderTargetElixir)));
        tgEnabled.setSelected(prefs.getBoolean("tgEnabled", false));
        tgTokenField.setText(prefs.get("tgToken", ""));
        tgChatField.setText(prefs.get("tgChatId", ""));
        mainMaxAttacksField.setText(prefs.get("mainMaxAttacks", "0"));
        builderMaxAttacksField.setText(prefs.get("builderMaxAttacks", "0"));
        rewardBox.setSelectedIndex(Math.max(0, Math.min(2, prefs.getInt("rewardChoice", RewardPicker.RIGHT))));

        alwaysOnTop.setSelected(prefs.getBoolean("alwaysOnTop", false));
        frame.setAlwaysOnTop(alwaysOnTop.isSelected());
    }

    private void saveSettings() {
        prefs.put("mode", String.valueOf(modeBox.getSelectedItem()));
        prefs.put("adbPath", adbPathField.getText().trim());
        prefs.put("serial", serialField.getText().trim());

        prefs.put("mainGold", mainGoldField.getText().trim());
        prefs.put("mainElixir", mainElixirField.getText().trim());
        prefs.put("enemyLoot", enemyLootField.getText().trim());
        prefs.put("builderGold", builderGoldField.getText().trim());
        prefs.put("builderElixir", builderElixirField.getText().trim());
        prefs.putBoolean("tgEnabled", tgEnabled.isSelected());
        prefs.put("tgToken", new String(tgTokenField.getPassword()).trim());
        prefs.put("tgChatId", tgChatField.getText().trim());
        prefs.put("mainMaxAttacks", mainMaxAttacksField.getText().trim());
        prefs.put("builderMaxAttacks", builderMaxAttacksField.getText().trim());
        prefs.putInt("rewardChoice", rewardBox.getSelectedIndex());

        prefs.putBoolean("alwaysOnTop", alwaysOnTop.isSelected());
    }

    // ----------------------------------------------------------------- hotkey

    private void registerHotkey() {
        // JNativeHook дуже балакучий на рівні INFO
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.out.println("Global hotkey unavailable: " + ex.getMessage());
            return;
        }

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (e.getKeyCode() == STOP_KEY) {
                    stopAll();
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {}

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {}
        });
    }

    // -------------------------------------------------------------------- log

    /** Дублює System.out / System.err у вікно логу (консоль IntelliJ теж працює). */
    private void installLogCapture() {
        System.setOut(new PrintStream(new LogStream(System.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new LogStream(System.err), true, StandardCharsets.UTF_8));
    }

    private void appendLog(String line) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(line + "\n");

            if (logArea.getLineCount() > MAX_LOG_LINES) {
                try {
                    int cut = logArea.getLineEndOffset(logArea.getLineCount() - MAX_LOG_LINES + 500);
                    logArea.getDocument().remove(0, cut);
                } catch (Exception ignored) {
                }
            }
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private class LogStream extends OutputStream {
        private final PrintStream original;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        LogStream(PrintStream original) {
            this.original = original;
        }

        @Override
        public synchronized void write(int b) {
            original.write(b);
            if (b == '\n') {
                String line = buffer.toString(StandardCharsets.UTF_8).replace("\r", "");
                buffer.reset();
                appendLog(line);
            } else {
                buffer.write(b);
            }
        }

        @Override
        public void flush() {
            original.flush();
        }
    }
}
