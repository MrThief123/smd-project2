package thrones.game;

enum GoTSuit { CHARACTER, DEFENCE, ATTACK, MAGIC }
public enum Suit {
    SPADES(GoTSuit.DEFENCE, "S", new SpadeEffectStrategy()),
    HEARTS(GoTSuit.CHARACTER, "H", null),
    DIAMONDS(GoTSuit.MAGIC, "D", new DiamondEffectStrategy()),
    CLUBS(GoTSuit.ATTACK, "C", new ClubEffectStrategy());
    private String suitShortHand = "";
    private final GoTSuit gotsuit;
    private final EffectStrategy effectStrategy;

    Suit(GoTSuit gotsuit, String shortHand, EffectStrategy effectStrategy) {
        this.gotsuit = gotsuit;
        this.suitShortHand = shortHand;
        this.effectStrategy = effectStrategy;
    }

    public String getSuitShortHand() {
        return suitShortHand;
    }

    public boolean isDefence(){ return gotsuit == GoTSuit.DEFENCE; }

    public boolean isAttack(){ return gotsuit == GoTSuit.ATTACK; }

    public boolean isCharacter(){ return gotsuit == GoTSuit.CHARACTER; }

    public boolean isMagic(){ return gotsuit == GoTSuit.MAGIC; }

    public EffectStrategy getEffectStrategy() {
        return effectStrategy;
    }
}
