public class UiElements {

    // Поріг схожості для OpenCV (TM_CCOEFF_NORMED): правильний збіг ~0.95+, схожа інша кнопка ~0.4.
    // Стартове значення; точніше підбирається для кожного елемента через tools/MatchTest.
    private static final double DEFAULT_THRESHOLD = 0.5;

    public static final UiElement FIND_MATCH;
    public static final UiElement ATTACK;
    public static final UiElement NEXT;
    public static final UiElement ATTACK_GO;
    public static final UiElement END_BATTLE;
    public static final UiElement SURRENDER_OKAY;
    public static final UiElement RETURN_HOME;
    public static final UiElement KING;
    public static final UiElement QUEEN;
    public static final UiElement WARDEN;
    public static final UiElement CHAMPION;
    public static final UiElement VALKYRIE;
    public static final UiElement JUMP_SPELL;
    public static final UiElement OKAY_STAR_BONUS;
    public static final UiElement OKAY_STAR_BONUS_DC;
    public static final UiElement DRAGON;
    public static final UiElement WIZARD;
    public static final UiElement BARBARIAN;
    public static final UiElement SNEAKY_GOBLIN;
    public static final UiElement Attack_DC;
    public static final UiElement Find_Now;
    public static final UiElement SURRENDER_DC;
    public static final UiElement Battle_Machine;
    public static final UiElement RETURN_HOME_DC;
    public static final UiElement COLLECT;
    public static final UiElement CASE;
    public static final UiElement CONTINUE;
    public static final UiElement PICK_REWARD;
    public static final UiElement DRAGONDUKE;
    public static final UiElement Battle_COPTER;
    public static final UiElement MINIONPRINCE;

    public UiElements() {
    }

    static {
        FIND_MATCH = new UiElement(Regions.FIND_MATCH, "images/FIND_MATCH.png", DEFAULT_THRESHOLD);
        ATTACK = new UiElement(Regions.ATTACK, "images/ATTACK.png", DEFAULT_THRESHOLD);
        NEXT = new UiElement(Regions.NEXT, "images/NEXT.png", DEFAULT_THRESHOLD);
        ATTACK_GO = new UiElement(Regions.ATTACK_GO, "images/ATTACK_GO.png", DEFAULT_THRESHOLD);
        END_BATTLE = new UiElement(Regions.END_BATTLE,"images/END_BATTLE.png", DEFAULT_THRESHOLD);
        SURRENDER_OKAY = new UiElement(Regions.SURRENDER_OKAY,"images/SURRENDER_OKAY.png", DEFAULT_THRESHOLD);
        RETURN_HOME = new UiElement(Regions.RETURN_HOME,"images/RETURN_HOME.png", DEFAULT_THRESHOLD);
        OKAY_STAR_BONUS = new UiElement(Regions.OKAY_STAR_BONUS,"images/OKAY_STAR_BONUS.png", DEFAULT_THRESHOLD);
        OKAY_STAR_BONUS_DC = new UiElement(Regions.OKAY_STAR_BONUS_DC,"images/OKAY_STAR_BONUS_DC.png", DEFAULT_THRESHOLD);
        CONTINUE = new UiElement(Regions.CONTINUE,"images/CONTINUE.png", DEFAULT_THRESHOLD);
        PICK_REWARD = new UiElement(Regions.PICK_REWARD,"images/PICK_REWARD.png", DEFAULT_THRESHOLD);

        KING = new UiElement(Regions.KING,"images/KING.png", DEFAULT_THRESHOLD);
        QUEEN = new UiElement(Regions.QUEEN,"images/QUEEN.png", DEFAULT_THRESHOLD);
        WARDEN = new UiElement(Regions.WARDEN,"images/WARDEN.png", DEFAULT_THRESHOLD);
        CHAMPION = new UiElement(Regions.CHAMPION,"images/CHAMPION.png", DEFAULT_THRESHOLD);
        DRAGONDUKE = new UiElement(Regions.DRAGONDUKE,"images/DRAGONDUKE.png", DEFAULT_THRESHOLD);
        MINIONPRINCE = new UiElement(Regions.MINIONPRINCE,"images/MINIONPRINCE.png", DEFAULT_THRESHOLD);

        VALKYRIE = new UiElement(Regions.VALKYRIE,"images/VALKYRIE.png", DEFAULT_THRESHOLD);
        JUMP_SPELL = new UiElement(Regions.JUMP_SPELL,"images/JUMP_SPELL.png", DEFAULT_THRESHOLD);
        DRAGON = new UiElement(Regions.DRAGON,"images/DRAGON.png", DEFAULT_THRESHOLD);
        WIZARD = new UiElement(Regions.WIZARD,"images/WIZARD.png", DEFAULT_THRESHOLD);
        BARBARIAN = new UiElement(Regions.BARBARIAN,"images/BARBARIAN.png", DEFAULT_THRESHOLD);
        SNEAKY_GOBLIN = new UiElement(Regions.SNEAKY_GOBLIN,"images/SNEAKY_GOBLIN.png", DEFAULT_THRESHOLD);


        Attack_DC = new UiElement(Regions.Attack_DC,"images/Attack_DC.png", DEFAULT_THRESHOLD);
        Find_Now = new UiElement(Regions.Find_Now,"images/Find_Now.png", DEFAULT_THRESHOLD);
        SURRENDER_DC = new UiElement(Regions.SURRENDER_DC,"images/SURRENDER_DC.png", DEFAULT_THRESHOLD);
        Battle_Machine = new UiElement(Regions.Battle_Machine,"images/Battle_Machine.png", DEFAULT_THRESHOLD);
        Battle_COPTER = new UiElement(Regions.Battle_COPTER,"images/Battle_COPTER.png", DEFAULT_THRESHOLD);
        RETURN_HOME_DC = new UiElement(Regions.RETURN_HOME_DC,"images/RETURN_HOME_DC.png", DEFAULT_THRESHOLD);
        COLLECT = new UiElement(Regions.COLLECT,"images/COLLECT.png", DEFAULT_THRESHOLD);
        CASE = new UiElement(Regions.CASE,"images/CASE.png", DEFAULT_THRESHOLD);

    }
}