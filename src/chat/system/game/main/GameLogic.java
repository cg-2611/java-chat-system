package chat.system.game.main;

import chat.system.game.entity.BotPlayer;
import chat.system.game.entity.HumanPlayer;
import chat.system.game.entity.Player;
import chat.system.game.map.Map;

/**
 * Contains the main game logic.
 */
public class GameLogic {

    private Map map;

    private HumanPlayer humanPlayer;
    private BotPlayer bot;

    private boolean running;

    /**
     * Default constructor.
     */
    public GameLogic() {
        running = false;

        map = new Map();

        humanPlayer = new HumanPlayer(map);

        bot = new BotPlayer(map, humanPlayer);
    }

    /**
     * @return Map object of the game.
     */
    public Map getGameMap() {
        return map;
    }

    /**
     * @return If the game is running.
     */
    public boolean gameRunning() {
        return running;
    }

    /**
     * @return Gold required to win.
     */
    protected String hello() {
        return String.valueOf(map.getGoldRequired());
    }

    /**
     * @return Gold currently owned.
     */
    protected String gold() {
        return String.valueOf(humanPlayer.getGold());
    }

    /**
     * Checks if movement is legal and updates player's location on the map.
     * @param player the player object that will be moved, i.e. the human player or the bot
     * @param direction the direction of the movement.
     * @return a string that says the the move was either a success or it failed.
     */
    protected String move(Player player, char direction) {
        int previousRow = player.getRow();
        int previousColumn = player.getColumn();

        try {
            if (player instanceof HumanPlayer) {
                humanPlayer.move(direction);
            } else if (player instanceof BotPlayer) {
                bot.moveBot();
            }

            // if the move is unsuccessful, move the player back
            if (map.getMap()[player.getRow()][player.getColumn()] == '#') {
                player.setRow(previousRow);
                player.setColumn(previousColumn);

                return "Fail";
            } else {
                return "Success";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // prevent the player from moving outside the boundary of the map,
            // even if the the map does not use a # character for the walls
            player.setRow(previousRow);
            player.setColumn(previousColumn);

            return "Fail";
        }
    }

    /**
     * Perform the PICKUP command, updating the map and the player's gold amount.
     * @return If the player successfully picked-up gold.
     */
    protected String pickup() {
        // the character at the players position in the map
        char mapCharacter = map.getMap()[humanPlayer.getRow()][humanPlayer.getColumn()];
        if (mapCharacter== 'G') {
            // remove the gold from the map if the pickup is successful
            map.changeCharacter(humanPlayer.getRow(), humanPlayer.getColumn(), '.');
            humanPlayer.incrementGold();
            return "Success. Gold owned:" + gold();
        } else {
            return "Fail. Gold owned: " + gold();
        }
    }

    /**
     * Converts the map from a 2D character array to a single string.
     * @param player the player that is using the method.
     * @return A String representation of the game map.
     */
    protected String look(Player player) {
        int outputSize = 5;
        String outputMap = "";

        int centre = (outputSize - 1) / 2;

        // loop through a 5x5 grid within the game map with the given player at the centre
        for (int i = player.getRow() - centre; i < player.getRow() - centre + outputSize; i++) {
            for (int j = player.getColumn() - centre; j < player.getColumn() - centre + outputSize; j++) {
                try {
                    if (i == humanPlayer.getRow() && j == humanPlayer.getColumn()) {
                        // put a P at the human player position of the player if the player is in the 5x5 grid
                        outputMap += 'P';
                    } else if (i == bot.getRow() && j == bot.getColumn()) {
                        // put a B at the bot player position of the bot if the bot is in the 5x5 grid
                        outputMap += 'B';
                    } else {
                        // otherwise, put the corresponding character from the map into the 5x5 grid
                        outputMap += map.getMap()[i][j];
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // display any visible areas outside the map as a #
                    outputMap += '#';
                }
            }

            outputMap += "\n";
        }

        return outputMap;
    }

    /**
     * Checks the winning condition of the player.
     * @return String which says whether or not the player has won or lost.
     */
    protected String exit() {
        char characterUnderPlayer = map.getMap()[humanPlayer.getRow()][humanPlayer.getColumn()];
        if (humanPlayer.getGold() >= map.getGoldRequired() && characterUnderPlayer == 'E') {
            return "WIN\nCongratulations you collected enough gold to escape the dungeon.";
        } else {
            return "LOSE";
        }
    }

    /**
     * Quits the game, shutting down the program.
     * @param message the message displayed when the game quits.
     */
    protected void quitGame(String message) {
        System.out.println("\n" + message + "\n");
        System.exit(0);
    }

    /**
     * Provides the feedback to the user about their chosen action.
     * @param action the command the user inputs.
     * @return the outcome of the input action
     */
    public String processAction(String action) {
        String output = "";
        if (action.equals("HELLO")) {
            output = "Gold to win: " + hello();
        } else if (action.equals("GOLD")) {
            output = "Gold owned: " + gold();
        } else if (action.contains("MOVE ")) {
            output = move(humanPlayer, action.charAt(action.length() - 1));
        } else if (action.equals("PICKUP")) {
            output = pickup();
        } else if (action.equals("LOOK")) {
            output = "\n" + look(humanPlayer);
        } else if (action.equals("EXIT")) {
            output = exit();
        } else {
            return "Invalid";
        }

        // perform the bot player move
        botMove();

        if (!getBotMoveResult().equals("Not caught")) {
            output = getBotMoveResult();
        }

        return output;
    }

    /**
     * Performs the bot player turn.
     */
    protected void botMove() {
        if (bot.getMapRequired()) {
            bot.updateVisibleMap(look(bot));
        } else {
            move(bot, '0'); // '0' passed as direction as it is not required by the bot
        }

        bot.toggleMapRequired();
    }

    /**
     * Gets the result of the bot player move.
     *
     * @return "Not caught" if the player has not been caught by the bot player, or the capture message
     * if the player has been caught.
     */
    protected String getBotMoveResult() {
        String botMoveResult = "Not caught";

        if (bot.getRow() == humanPlayer.getRow() && bot.getColumn() == humanPlayer.getColumn()) {
            botMoveResult = "You were caught by the bot, you lose.";
        }

        return botMoveResult;
    }

    /**
     * Runs the game.
     */
    public void runGame() {
        running = true;
    }

}
