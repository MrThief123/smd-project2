package thrones.game;

import ch.aplu.jcardgame.Card;
import thrones.game.Rank;

public interface EffectStrategy {
    void applyEffect(Card current, Card previous, PileAttributes pile);

    default int calculateCardValue(Card current, Card previous) {
        int currentScoreValue = ((Rank) current.getRank()).getScoreValue();

        if (previous == null) {
            return currentScoreValue;
        }

        int previousScoreValue = ((Rank) previous.getRank()).getScoreValue();

        if (currentScoreValue == previousScoreValue) {
            currentScoreValue *= 2;
        }

        return currentScoreValue;
    }
}
