package thrones.game;

import java.util.List;


public class DefendingStrategy implements SmartMoveStrategy {
    private final SmartMoveEvaluator evaluator = new SmartMoveEvaluator();

    @Override
    public SmartMove chooseMove(SmartBotContext context) {
        List<MoveCandidate> candidates = evaluator.getDefendingCandidates(context);

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