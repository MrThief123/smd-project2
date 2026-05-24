package thrones.game;

import ch.aplu.jcardgame.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Smart-bot strategy for Defending Mode.
 *
 * Selects a move that increases the bot team's defence or decreases the
 * opponent's attack. On a tie in effect magnitude, prioritises decreasing
 * the opponent's attack (spec 3.3, p.8).
 */
public class DefendingStrategy implements SmartMoveStrategy {

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

            if (suit.isDefence()) {
                int effect = MoveCandidate.calculateEffect(card, context.getOwnPile());
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

                int effect = MoveCandidate.calculateEffect(card, context.getOpponentPile());
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

    private boolean isBetter(MoveCandidate candidate, MoveCandidate best) {
        if (candidate.getEffectValue() > best.getEffectValue()) {
            return true;
        }

        if (candidate.getEffectValue() < best.getEffectValue()) {
            return false;
        }

        // Tie rule: prioritise decreasing opponent attack.
        return candidate.getCategory() == MoveCategory.DECREASE_OPPONENT_ATTACK
                && best.getCategory() != MoveCategory.DECREASE_OPPONENT_ATTACK;
    }
}