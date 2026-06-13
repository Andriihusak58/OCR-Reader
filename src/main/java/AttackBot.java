import java.util.Random;

public class AttackBot {

    private final BattleBarScanner scanner;
    private final Random random = new Random();

    private volatile boolean running = true;

    public AttackBot() throws Exception {
        this.scanner = new BattleBarScanner();
    }

    public void stop() {
        running = false;
    }

    private boolean shouldStop() {
        return !running;
    }

    private void sleep(int minMs, int randomAddMs) throws InterruptedException {
        int total = minMs + random.nextInt(randomAddMs);
        int waited = 0;

        while (waited < total) {
            if (shouldStop()) {
                return;
            }

            Thread.sleep(100);
            waited += 100;
        }
    }

    public boolean attack() throws Exception {
        running = true;

        System.out.println("Attack started");

        BattleLayout layout = scanner.scanAll();

        if (shouldStop()) return false;

        boolean hasAnyTroop =
                layout.king != null ||
                        layout.queen != null ||
                        layout.warden != null ||
                        layout.champion != null ||
                        layout.valkyrie != null ||
                        layout.barbarian != null ||
                        layout.dragon != null ||
                        layout.wizard != null ||
                        layout.jumpSpell != null ||
                        layout.SNEAKY_GOBLIN != null;

        if (!hasAnyTroop) {
            System.out.println("No battle slots found");
            return false;
        }

        deployHeroes(layout);
        if (shouldStop()) return false;
        sleep(300, 390);

        activateHeroes(layout);
        if (shouldStop()) return false;
        sleep(300, 390);

        deployJumpSpells(layout);
        if (shouldStop()) return false;
        sleep(300, 390);

        deployValkyries(layout);
        if (shouldStop()) return false;
        sleep(300, 390);

        deployWizard(layout);
        if (shouldStop()) return false;
        sleep(300, 390);

        deployBarbarian(layout);
        if (shouldStop()) return false;
        sleep(300, 390);

        deploySNEAKY_GOBLIN(layout);
        if (shouldStop()) return false;
        sleep(300, 390);

        deployDragon(layout);
        if (shouldStop()) return false;
        sleep(300, 390);

        sleep(5000, 8000);
        if (shouldStop()) return false;

        finishBattle();

        if (shouldStop()) return false;

        System.out.println("Attack finished");
        return true;
    }

    private void deployHeroes(BattleLayout layout) throws Exception {
        deployHeroIfPresent("KING", layout.king, Regions.POINT_1);
        deployHeroIfPresent("QUEEN", layout.queen, Regions.POINT_1);
        deployHeroIfPresent("WARDEN", layout.warden, Regions.POINT_1);
        deployHeroIfPresent("CHAMPION", layout.champion, Regions.POINT_1);
    }

    private void deployHeroIfPresent(String name, Region heroSlot, Region dropPoint) throws Exception {
        if (shouldStop()) return;

        if (heroSlot == null) {
            System.out.println(name + " not found, skip");
            return;
        }

        Actions.battleClick(heroSlot);
        sleep(300, 450);

        if (shouldStop()) return;

        Actions.battleClick(dropPoint);
        sleep(300, 450);

        if (shouldStop()) return;

        Actions.battleClick(dropPoint);
        sleep(100, 200);
    }

    private void activateHeroes(BattleLayout layout) throws Exception {
        activateHeroIfPresent("KING", layout.king);
        activateHeroIfPresent("QUEEN", layout.queen);
        activateHeroIfPresent("WARDEN", layout.warden);
        activateHeroIfPresent("CHAMPION", layout.champion);
    }

    private void activateHeroIfPresent(String name, Region heroSlot) throws Exception {
        if (shouldStop()) return;

        if (heroSlot == null) {
            return;
        }

        Actions.battleClick(heroSlot);
        sleep(80, 120);
    }

    private void deployJumpSpells(BattleLayout layout) throws Exception {
        if (shouldStop()) return;

        if (layout.jumpSpell == null) {
            System.out.println("JUMP_SPELL not found, skip");
            return;
        }

        Actions.battleClick(layout.jumpSpell);
        sleep(300, 450);

        clickPoint(Regions.POINT_JUMP_1, 300, 450);
        clickPoint(Regions.POINT_JUMP_2, 300, 450);
        clickPoint(Regions.POINT_JUMP_3, 300, 450);
        clickPoint(Regions.POINT_JUMP_4, 300, 450);
    }

    private void deployValkyries(BattleLayout layout) throws Exception {
        if (shouldStop()) return;

        if (layout.valkyrie == null) {
            System.out.println("VALKYRIE not found, skip");
            return;
        }

        Actions.battleClick(layout.valkyrie);
        sleep(300, 450);

        holdPoint(Regions.POINT_2, 900, 200, 300, 450);
        holdPoint(Regions.POINT_3, 900, 200, 300, 450);
        holdPoint(Regions.POINT_4, 900, 200, 300, 450);
        holdPoint(Regions.POINT_5, 900, 200, 300, 450);
        holdPoint(Regions.POINT_6, 2500, 200, 300, 450);
    }

    private void deployDragon(BattleLayout layout) throws Exception {
        if (shouldStop()) return;

        if (layout.dragon == null) {
            System.out.println("DRAGON not found, skip");
            return;
        }

        Actions.battleClick(layout.dragon);
        sleep(300, 450);

        holdPoint(Regions.POINT_3, 900, 200, 300, 450);
        holdPoint(Regions.POINT_4, 900, 200, 300, 450);
        holdPoint(Regions.POINT_5, 900, 200, 300, 450);
        holdPoint(Regions.POINT_6, 900, 200, 300, 450);
        holdPoint(Regions.POINT_2, 1900, 200, 300, 450);
    }

    private void deployBarbarian(BattleLayout layout) throws Exception {
        if (shouldStop()) return;

        if (layout.barbarian == null) {
            System.out.println("BARBARIAN not found, skip");
            return;
        }

        Actions.battleClick(layout.barbarian);
        sleep(300, 450);

        holdPoint(Regions.POINT_5, 900, 200, 300, 450);
        holdPoint(Regions.POINT_6, 900, 200, 300, 450);
        holdPoint(Regions.POINT_1, 900, 200, 300, 450);
        holdPoint(Regions.POINT_2, 900, 200, 300, 450);
        holdPoint(Regions.POINT_3, 1900, 200, 300, 450);
    }

    private void deployWizard(BattleLayout layout) throws Exception {
        if (shouldStop()) return;

        if (layout.wizard == null) {
            System.out.println("WIZARD not found, skip");
            return;
        }

        Actions.battleClick(layout.wizard);
        sleep(300, 450);

        holdPoint(Regions.POINT_4, 900, 200, 300, 450);
        holdPoint(Regions.POINT_5, 900, 200, 300, 450);
        holdPoint(Regions.POINT_6, 900, 200, 300, 450);
        holdPoint(Regions.POINT_2, 900, 200, 300, 450);
        holdPoint(Regions.POINT_3, 1900, 200, 300, 450);
    }

    private void deploySNEAKY_GOBLIN(BattleLayout layout) throws Exception {
        if (shouldStop()) return;

        if (layout.SNEAKY_GOBLIN == null) {
            System.out.println("SNEAKY_GOBLIN not found, skip");
            return;
        }

        Actions.battleClick(layout.SNEAKY_GOBLIN);
        sleep(300, 450);

        Region[] points = {
                Regions.POINT_4,
                Regions.POINT_5,
                Regions.POINT_6,
                Regions.POINT_2,
                Regions.POINT_3,
                Regions.POINT_4,
                Regions.POINT_5,
                Regions.POINT_6,
                Regions.POINT_2,
                Regions.POINT_3,
                Regions.POINT_4,
                Regions.POINT_5,
                Regions.POINT_6,
                Regions.POINT_2,
                Regions.POINT_3,
                Regions.POINT_3,
                Regions.POINT_4,
                Regions.POINT_5,
                Regions.POINT_6
        };

        for (Region point : points) {
            holdPoint(point, 900, 200, 300, 450);
        }

        holdPoint(Regions.POINT_2, 2500, 200, 300, 450);
    }

    private void clickPoint(Region point, int sleepMin, int sleepRandom) throws Exception {
        if (shouldStop()) return;

        Actions.battleClick(point);
        sleep(sleepMin, sleepRandom);
    }

    private void holdPoint(
            Region point,
            int holdMin,
            int holdRandom,
            int sleepMin,
            int sleepRandom
    ) throws Exception {
        if (shouldStop()) return;

        Actions.battleHoldClick(point, holdMin + random.nextInt(holdRandom));
        sleep(sleepMin, sleepRandom);
    }

    private void finishBattle() throws Exception {
        if (shouldStop()) return;

        Actions.click(Regions.SURRENDER);
        sleep(700, 1000);

        if (shouldStop()) return;

        Actions.click(Regions.SURRENDER_OKAY);
        sleep(1500, 2500);

        if (shouldStop()) return;

        Actions.click(Regions.RETURN_HOME);
        sleep(1500, 2500);
    }
}