package thrones.game;

import ch.aplu.jcardgame.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Smart-bot strategy for Attacking Mode.
 *
 * Selects a move that increases the bot team's attack or decreases the
 * opponent's defence. On a tie in effect magnitude, prioritises decreasing
 * the opponent's defence (spec 3.3, p.7).
 */
public class AttackingStrategy implements SmartMoveStrategy {

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

            if (suit.isAttack()) {
                int effect = MoveCandidate.calculateEffect(card, context.getOwnPile());
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

                int effect = MoveCandidate.calculateEffect(card, context.getOpponentPile());
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

    private boolean isBetter(MoveCandidate candidate, MoveCandidate best) {
        if (candidate.getEffectValue() > best.getEffectValue()) {
            return true;
        }

        if (candidate.getEffectValue() < best.getEffectValue()) {
            return false;
        }

        // Tie rule: prioritise decreasing opponent defence.
        return candidate.getCategory() == MoveCategory.DECREASE_OPPONENT_DEFENCE
                && best.getCategory() != MoveCategory.DECREASE_OPPONENT_DEFENCE;
    }
}