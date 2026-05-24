package thrones.game;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.List;

public class MoveCandidate {

    private final Card card;
    private final int pileIndex;
    private final int effectValue;
    private final MoveCategory category;

    public MoveCandidate(Card card, int pileIndex, int effectValue, MoveCategory category) {
        this.card = card;
        this.pileIndex = pileIndex;
        this.effectValue = effectValue;
        this.category = category;
    }

    public Card getCard() {
        return card;
    }

    public int getPileIndex() {
        return pileIndex;
    }

    public int getEffectValue() {
        return effectValue;
    }

    public MoveCategory getCategory() {
        return category;
    }

    public SmartMove toSmartMove() {
        return SmartMove.play(card, pileIndex);
    }

    /**
     * Computes the effect value of playing {@code current} on {@code targetPile},
     * applying the doubled-effect rule when {@code current}'s rank equals the
     * rank of the pile's top card.
     */
    public static int calculateEffect(Card current, Hand targetPile) {
        int currentValue = ((Rank) current.getRank()).getScoreValue();

        List<Card> pileCards = targetPile.getCardList();

        if (pileCards.isEmpty()) {
            return currentValue;
        }

        Card previous = pileCards.get(pileCards.size() - 1);
        int previousValue = ((Rank) previous.getRank()).getScoreValue();

        if (currentValue == previousValue) {
            return currentValue * 2;
        }

        return currentValue;
    }
}