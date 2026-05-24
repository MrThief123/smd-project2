package thrones.game;

public interface SmartMoveStrategy {
    SmartMove chooseMove(SmartBotContext context);
}
