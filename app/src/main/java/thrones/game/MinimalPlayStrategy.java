package thrones.game;

import java.util.List;

/**
 * Smart-bot strategy for Minimal Play Mode.
 *
 * Used after the smart bot passed on its previous turn.
 */
public class MinimalPlayStrategy implements SmartMoveStrategy {
    private final SmartMoveEvaluator evaluator = new SmartMoveEvaluator();

    @Override
    public SmartMove chooseMove(SmartBotContext context) {
        List<MoveCandidate> candidates = evaluator.getMinimalPlayCandidates(context);

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