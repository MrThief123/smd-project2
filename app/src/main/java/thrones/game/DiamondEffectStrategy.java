package thrones.game;

import ch.aplu.jcardgame.Card;

public class DiamondEffectStrategy implements EffectStrategy {
    @Override
    public void applyEffect(Card current, Card previous, PileAttributes pile) {
        PileAttributes.AffectedAttribute prev = pile.getLastAffected();
        if (prev == PileAttributes.AffectedAttribute.ATTACK) {
            pile.setAttack(pile.getAttack() - calculateCardValue(current, previous));
        } else if (prev == PileAttributes.AffectedAttribute.DEFENCE) {
            pile.setDefence(pile.getDefence() - calculateCardValue(current, previous));
        } else {
            // Diamond stacked on Heart which is illegal move
        }
    }
}
