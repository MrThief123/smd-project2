package thrones.game;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.ArrayList;
import java.util.List;


public class SmartMoveEvaluator {

    public List<MoveCandidate> getAttackingCandidates(SmartBotContext context) {
        List<MoveCandidate> candidates = new ArrayList<>();

        for (Card card : context.getHand().getCardList()) {
            Suit suit = (Suit) card.getSuit();

            if (suit.isCharacter()) {
                continue;
            }

            if (suit.isAttack()) {
                int effect = calculateEffect(card, context.getOwnPile());
                candidates.add(new MoveCandidate(
                        card,
                        context.getOwnPileIndex(),
                        effect,
                        MoveCategory.INCREASE_OWN_ATTACK
                ));
            }

            if (suit.isMagic()
                    && context.getOpponentAttributes().getLastAffected()
                    == PileAttributes.AffectedAttribute.DEFENCE) {

                int effect = calculateEffect(card, context.getOpponentPile());
                candidates.add(new MoveCandidate(
                        card,
                        context.getOpponentPileIndex(),
                        effect,
                        MoveCategory.DECREASE_OPPONENT_DEFENCE
                ));
            }
        }

        return candidates;
    }

    public List<MoveCandidate> getDefendingCandidates(SmartBotContext context) {
        List<MoveCandidate> candidates = new ArrayList<>();

        for (Card card : context.getHand().getCardList()) {
            Suit suit = (Suit) card.getSuit();

            if (suit.isCharacter()) {
                continue;
            }

            if (suit.isDefence()) {
                int effect = calculateEffect(card, context.getOwnPile());
                candidates.add(new MoveCandidate(
                        card,
                        context.getOwnPileIndex(),
                        effect,
                        MoveCategory.INCREASE_OWN_DEFENCE
                ));
            }

            if (suit.isMagic()
                    && context.getOpponentAttributes().getLastAffected()
                    == PileAttributes.AffectedAttribute.ATTACK) {

                int effect = calculateEffect(card, context.getOpponentPile());
                candidates.add(new MoveCandidate(
                        card,
                        context.getOpponentPileIndex(),
                        effect,
                        MoveCategory.DECREASE_OPPONENT_ATTACK
                ));
            }
        }

        return candidates;
    }

    public List<MoveCandidate> getMinimalPlayCandidates(SmartBotContext context) {
        List<MoveCandidate> candidates = new ArrayList<>();

        for (Card card : context.getHand().getCardList()) {
            Suit suit = (Suit) card.getSuit();

            if (suit.isCharacter()) {
                continue;
            }

            if (suit.isMagic()
                    && context.getOpponentAttributes().getLastAffected()
                    != PileAttributes.AffectedAttribute.NONE) {

                int effect = calculateEffect(card, context.getOpponentPile());

                MoveCategory category;
                if (context.getOpponentAttributes().getLastAffected()
                        == PileAttributes.AffectedAttribute.ATTACK) {
                    category = MoveCategory.DECREASE_OPPONENT_ATTACK;
                } else {
                    category = MoveCategory.DECREASE_OPPONENT_DEFENCE;
                }

                candidates.add(new MoveCandidate(
                        card,
                        context.getOpponentPileIndex(),
                        effect,
                        category
                ));
            }

            if (suit.isAttack()) {
                int effect = calculateEffect(card, context.getOwnPile());
                candidates.add(new MoveCandidate(
                        card,
                        context.getOwnPileIndex(),
                        effect,
                        MoveCategory.INCREASE_OWN_ATTACK
                ));
            }

            if (suit.isDefence()) {
                int effect = calculateEffect(card, context.getOwnPile());
                candidates.add(new MoveCandidate(
                        card,
                        context.getOwnPileIndex(),
                        effect,
                        MoveCategory.INCREASE_OWN_DEFENCE
                ));
            }
        }

        return candidates;
    }

    private int calculateEffect(Card current, Hand targetPile) {
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