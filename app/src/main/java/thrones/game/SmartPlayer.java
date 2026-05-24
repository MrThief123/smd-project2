package thrones.game;

/**
 * Main smart-bot decision-maker.
 *
 * This class chooses which strategy to use, then delegates the actual
 * move selection to that strategy.
 */
public class SmartPlayer {
    private boolean passedLastTurn = false;

    private final SmartMoveStrategy characterSelectionStrategy;
    private final SmartMoveStrategy attackingStrategy;
    private final SmartMoveStrategy defendingStrategy;
    private final SmartMoveStrategy minimalPlayStrategy;

    public SmartPlayer() {
        this.characterSelectionStrategy = new CharacterSelectionStrategy();
        this.attackingStrategy = new AttackingStrategy();
        this.defendingStrategy = new DefendingStrategy();
        this.minimalPlayStrategy = new MinimalPlayStrategy();
    }

    public SmartMove chooseMove(SmartBotContext context) {
        SmartMoveStrategy strategy = chooseStrategy(context);
        SmartMove move = strategy.chooseMove(context);

        updatePassStatus(context, move, strategy);

        return move;
    }

    private SmartMoveStrategy chooseStrategy(SmartBotContext context) {
        if (context.isCharacterSelectionRequired()) {
            return characterSelectionStrategy;
        }

        if (passedLastTurn) {
            return minimalPlayStrategy;
        }

        int ownAttack = context.getOwnAttributes().getAttack();
        int opponentDefence = context.getOpponentAttributes().getDefence();

        if (ownAttack <= opponentDefence) {
            return attackingStrategy;
        }

        return defendingStrategy;
    }

    private void updatePassStatus(
            SmartBotContext context,
            SmartMove move,
            SmartMoveStrategy strategy
    ) {
        if (context.isCharacterSelectionRequired()) {
            passedLastTurn = false;
            return;
        }

        if (strategy == minimalPlayStrategy) {
            passedLastTurn = false;
            return;
        }

        passedLastTurn = move.isPass();
    }

    public void resetForNewPlay() {
        passedLastTurn = false;
    }
}