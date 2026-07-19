public class UiElements {
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
    public static final UiElement DRAGONDUKE;
    public static final UiElement Battle_COPTER;
    public static final UiElement MINIONPRINCE;

    public UiElements() {
    }

    static {
        FIND_MATCH = new UiElement(Regions.FIND_MATCH, "images/FIND_MATCH.png", 0.4);
        ATTACK = new UiElement(Regions.ATTACK, "images/ATTACK.png", 0.2);
        NEXT = new UiElement(Regions.NEXT, "images/NEXT.png", 0.7);
        ATTACK_GO = new UiElement(Regions.ATTACK_GO, "images/ATTACK_GO.png", 0.7);
        END_BATTLE = new UiElement(Regions.END_BATTLE,"images/END_BATTLE.png",0.7);
        SURRENDER_OKAY = new UiElement(Regions.SURRENDER_OKAY,"images/SURRENDER_OKAY.png",0.7);
        RETURN_HOME = new UiElement(Regions.RETURN_HOME,"images/RETURN_HOME.png",0.5);
        OKAY_STAR_BONUS = new UiElement(Regions.OKAY_STAR_BONUS,"images/OKAY_STAR_BONUS.png",0.7);
        OKAY_STAR_BONUS_DC = new UiElement(Regions.OKAY_STAR_BONUS_DC,"images/OKAY_STAR_BONUS_DC.png",0.7);
        CONTINUE = new UiElement(Regions.CONTINUE,"images/CONTINUE.png",0.7);

        KING = new UiElement(Regions.KING,"images/KING.png",0.7);
        QUEEN = new UiElement(Regions.QUEEN,"images/QUEEN.png",0.7);
        WARDEN = new UiElement(Regions.WARDEN,"images/WARDEN.png",0.7);
        CHAMPION = new UiElement(Regions.CHAMPION,"images/CHAMPION.png",0.7);
        DRAGONDUKE = new UiElement(Regions.DRAGONDUKE,"images/DRAGONDUKE.png",0.7);
        MINIONPRINCE = new UiElement(Regions.MINIONPRINCE,"images/MINIONPRINCE.png",0.7);

        VALKYRIE = new UiElement(Regions.VALKYRIE,"images/VALKYRIE.png",0.7);
        JUMP_SPELL = new UiElement(Regions.JUMP_SPELL,"images/JUMP_SPELL.png",0.7);
        DRAGON = new UiElement(Regions.DRAGON,"images/DRAGON.png",0.7);
        WIZARD = new UiElement(Regions.WIZARD,"images/WIZARD.png",0.7);
        BARBARIAN = new UiElement(Regions.BARBARIAN,"images/BARBARIAN.png",0.7);
        SNEAKY_GOBLIN = new UiElement(Regions.SNEAKY_GOBLIN,"images/SNEAKY_GOBLIN.png",0.7);


        Attack_DC = new UiElement(Regions.Attack_DC,"images/Attack_DC.png",0.2);
        Find_Now = new UiElement(Regions.Find_Now,"images/Find_Now.png",0.7);
        SURRENDER_DC = new UiElement(Regions.SURRENDER_DC,"images/SURRENDER_DC.png",0.7);
        Battle_Machine = new UiElement(Regions.Battle_Machine,"images/Battle_Machine.png",0.7);
        Battle_COPTER = new UiElement(Regions.Battle_COPTER,"images/Battle_COPTER.png",0.7);
        RETURN_HOME_DC = new UiElement(Regions.RETURN_HOME_DC,"images/RETURN_HOME_DC.png",0.7);
        COLLECT = new UiElement(Regions.COLLECT,"images/COLLECT.png",0.7);
        CASE = new UiElement(Regions.CASE,"images/CASE.png",0.7);

    }
}