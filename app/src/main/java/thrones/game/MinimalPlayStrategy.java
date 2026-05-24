package thrones.game;

import ch.aplu.jcardgame.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Smart-bot strategy for Minimal Play Mode.
 *
 * Used after the smart bot passed on its previous turn. Selects a legal card
 * with the smallest effect; on a tie, applies the priority: diamond-to-opponent
 * &gt; attack-to-team &gt; defence-to-team (spec 3.3, p.10).
 */
public class MinimalPlayStrategy implements SmartMoveStrategy {

    @Override
    public SmartMove chooseMove(SmartBotContext context) {
        List<MoveCandidate> candidates = collectCandidates(context);

        if (candidates.isEmpty()) {
            return SmartMove.pass();
        }

        MoveCandidate best = candidates.get(0);

        for (MoveCandidate candidate : candidates) {
            if (isBetter(candidate, best)) {
                best = candidate;
            }
        }

        return best.toSmartMove();
    }

    private List<MoveCandidate> collectCandidates(SmartBotContext context) {
        List<MoveCandidate> candidates = new ArrayList<>();

        for (Card card : context.getHand().getCardList()) {
            Suit suit = (Suit) card.getSuit();

            if (suit.isCharacter()) {
                continue;
            }

            if (suit.isMagic()
                    && context.getOpponentAttributes().getLastAffected()
                    != PileAttributes.AffectedAttribute.NONE) {

                int effect = MoveCandidate.calculateEffect(card, context.getOpponentPile());

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
                int effect = MoveCandidate.calculateEffect(card, context.getOwnPile());
                candidates.add(new MoveCandidate(
                        card,
                        context.getOwnPileIndex(),
                        effect,
                        MoveCategory.INCREASE_OWN_ATTACK
                ));
            }

            if (suit.isDefence()) {
                int effect = MoveCandidate.calculateEffect(card, context.getOwnPile());
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

    private boolean isBetter(MoveCandidate candidate, MoveCandidate best) {
        if (candidate.getEffectValue() < best.getEffectValue()) {
            return true;
        }

        if (candidate.getEffectValue() > best.getEffectValue()) {
            return false;
        }

        return getMinimalPriority(candidate.getCategory())
                < getMinimalPriority(best.getCategory());
    }

    private int getMinimalPriority(MoveCategory category) {
        if (category == MoveCategory.DECREASE_OPPONENT_ATTACK ||
                category == MoveCategory.DECREASE_OPPONENT_DEFENCE) {
            return 1;
        }

        if (category == MoveCategory.INCREASE_OWN_ATTACK) {
            return 2;
        }

        if (category == MoveCategory.INCREASE_OWN_DEFENCE) {
            return 3;
        }

        return 4;
    }
}