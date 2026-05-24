package thrones.game;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.Rank;
import thrones.game.Suit;
import java.util.List;


public class PileAttributes {
    public enum AffectedAttribute {ATTACK, DEFENCE, NONE};
    private int attack;
    private int defence;
    private AffectedAttribute previousAffected = AffectedAttribute.NONE;

    public PileAttributes() {
        // empty
    }

    public int getAttack()  {
        return attack;
    }

    public int getDefence() {
        return defence;
    }

    public AffectedAttribute getLastAffected() {
        return previousAffected;
    }

    public void setAttack(int attack) {
        this.attack = Math.max(0, attack);
    }

    public void setDefence(int defence) {
        this.defence = Math.max(0, defence);
    }

    public void setPreviousAffected(AffectedAttribute previous) {
        this.previousAffected = previous;
    }

    public PileAttributes calculate(List<Card> pile) {
        attack = 0;
        defence = 0;
        previousAffected = AffectedAttribute.NONE;

        if (pile.isEmpty()) {
            return this;
        }

        // Set base scores to heart face value
        Rank characterRank = (Rank) pile.get(0).getRank();
        setAttack(characterRank.getScoreValue());
        setDefence(characterRank.getScoreValue());

        for (int i = 1; i < pile.size(); i++) {
            Card current = pile.get(i);
            Card previous = pile.get(i - 1);
            Suit suit = (Suit) current.getSuit();
            EffectStrategy strategy = suit.getEffectStrategy();
            if (strategy != null) {
                strategy.applyEffect(current, previous, this);
            }
        }

        return this;
    }

    public PileAttributes calculate(Hand pile) {
        return calculate(pile.getCardList());
    }
}
