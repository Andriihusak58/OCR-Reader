import javax.swing.*;
import java.awt.*;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class BotGui {

    private Main mainBot;
    private Builder builderBot;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BotGui().create());
    }

    private void create() {

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            ex.printStackTrace();
        }

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_S) {
                    if (mainBot != null) {
                        mainBot.stop();
                    }
                    if (builderBot != null) {
                        builderBot.stop();
                    }
                    System.out.println("S pressed - stopping bots");
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {}

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {}
        });

        JFrame frame = new JFrame("Bot Control");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 420);
        frame.setLayout(new GridLayout(0, 2, 8, 8));

        JTextField mainGoldField = new JTextField("9000000");
        JTextField mainElixirField = new JTextField("9000000");
        JTextField enemyLootField = new JTextField("1000000");

        JTextField builderGoldField = new JTextField("3000000");
        JTextField builderElixirField = new JTextField("3000000");

        JComboBox<String> modeBox = new JComboBox<>(new String[]{"Robot (PC screen)", "ADB (LDPlayer)"});
        JTextField serialField = new JTextField("emulator-5554");

        JButton startMain = new JButton("Start Main Bot");
        JButton stopMain = new JButton("Stop Main Bot = S");
        JButton startBuilder = new JButton("Start Builder Bot");
        JButton stopBuilder = new JButton("Stop Builder Bot = S");

        frame.add(new JLabel("Input mode:"));
        frame.add(modeBox);

        frame.add(new JLabel("ADB serial:"));
        frame.add(serialField);

        frame.add(new JLabel("Main target gold:"));
        frame.add(mainGoldField);

        frame.add(new JLabel("Main target elixir:"));
        frame.add(mainElixirField);

        frame.add(new JLabel("Enemy min loot:"));
        frame.add(enemyLootField);

        frame.add(new JLabel("Builder target gold:"));
        frame.add(builderGoldField);

        frame.add(new JLabel("Builder target elixir:"));
        frame.add(builderElixirField);

        frame.add(startMain);
        frame.add(stopMain);

        frame.add(startBuilder);
        frame.add(stopBuilder);

        startMain.addActionListener(e -> {
            BotConfig config = readConfig(
                    mainGoldField,
                    mainElixirField,
                    enemyLootField,
                    builderGoldField,
                    builderElixirField
            );

            mainBot = new Main(config, contextFactory(modeBox, serialField));

            new Thread(() -> {
                try {
                    mainBot.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        stopMain.addActionListener(e -> {
            if (mainBot != null) {
                mainBot.stop();
                System.out.println("Stop Main clicked");
            }
        });

        startBuilder.addActionListener(e -> {
            BotConfig config = readConfig(
                    mainGoldField,
                    mainElixirField,
                    enemyLootField,
                    builderGoldField,
                    builderElixirField
            );

            builderBot = new Builder(config, contextFactory(modeBox, serialField));

            new Thread(() -> {
                try {
                    builderBot.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        stopBuilder.addActionListener(e -> {
            if (builderBot != null) {
                builderBot.stop();
                System.out.println("Stop Builder clicked");
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }

    private ContextFactory contextFactory(JComboBox<String> modeBox, JTextField serialField) {
        final boolean adb = modeBox.getSelectedIndex() == 1;
        final String serial = serialField.getText().trim();
        return adb ? () -> GameContext.adb(serial) : GameContext::robot;
    }

    private BotConfig readConfig(
            JTextField mainGold,
            JTextField mainElixir,
            JTextField enemyLoot,
            JTextField builderGold,
            JTextField builderElixir
    ) {
        BotConfig config = new BotConfig();

        config.mainTargetGold = Integer.parseInt(mainGold.getText());
        config.mainTargetElixir = Integer.parseInt(mainElixir.getText());
        config.enemyMinLootTotal = Integer.parseInt(enemyLoot.getText());

        config.builderTargetGold = Integer.parseInt(builderGold.getText());
        config.builderTargetElixir = Integer.parseInt(builderElixir.getText());

        return config;
    }
}