package chat.system.game.entity;

import java.util.Random;

import chat.system.game.map.Map;

/**
 * Non human player in the game, will try to make the player lose.
 */
public class BotPlayer extends Player {

    private final char[] DIRECTIONS = {'N', 'E', 'S', 'W'};

    private String visibleMap; // string representation of the game map that the bot can use

    private HumanPlayer player; // the human player object the bot will try to defeat

    private boolean mapRequired;

    /**
     * Constructor for Bot object.
     * @param gameMap the map the player will play in.
     * @param player the human player object opponent of the bot.
     */
    public BotPlayer(Map gameMap, HumanPlayer player) {
        super(gameMap);

        this.player = player;

        mapRequired = true;

        // prevent the player and bot spawning in the same position
        if (row == player.getRow() && column == player.getColumn()) {
            initialisePlayerPosition();
        }
    }

    /**
     * @return The map visible to the bot, i.e. the 5x5 section of the overall game map.
     */
    public String getVisibleMap() {
        return visibleMap;
    }

    /**
     * @return Whether or not the bot needs to update its visible map.
     */
    public boolean getMapRequired() {
        return mapRequired;
    }

    /**
     * Sets the visible map to a new map.
     *
     * @param visibleMap the new map that the bot will be able to view.
     */
    public void updateVisibleMap(String visibleMap) {
        this.visibleMap = visibleMap;
    }

    /**
     * Switches the value of the boolean mapRequired.
     */
    public void toggleMapRequired() {
        mapRequired = !mapRequired;
    }

    /**
     * If the bot can see the player in the game, then it will chase the player,
     * otherwise it will move randomly, in an attempt to find the player.
     */
    public void moveBot() {
        if (isPlayerVisible()) {
            moveTowardsPlayer();
        } else {
            moveRandomDirection();
        }
    }

    /**
     * Loops through the map visible to the bot and checks for the player.
     * @return True if the player is in within the map that the bot can view.
     */
    private boolean isPlayerVisible() {
        for (int i = 0; i < getVisibleMap().length(); i++) {
            if (getVisibleMap().charAt(i) == 'P') {
                return true;
            }
        }

        return false;
    }

    /**
     * Moves the bot in a random direction.
     */
    private void moveRandomDirection() {
        move(DIRECTIONS[new Random().nextInt(DIRECTIONS.length)]);
    }

    /**
     * Calculates and performs the best move to make in order to chase the player.
     */
    private void moveTowardsPlayer() {
        if (player.getRow() <= this.row && player.getColumn() <= this.column) {
            // if player in top left quadrant of the map visible to bot, move bot in direction if the
            // horizontal distance between the player and bot is greater than the vertical distance,
            // move bot in direction W, otherwise move in direction N
            if (Math.abs(player.getRow() - this.row) <= Math.abs(player.getColumn() - this.column)) {
                move(DIRECTIONS[3]);
            } else {
                move(DIRECTIONS[0]);
            }
        } else if (player.getRow() <= this.row && player.getColumn() >= this.column) {
            // if player in top right quadrant of the map visible to bot, move bot in direction if the
            // horizontal distance between the player and bot is greater than the vertical distance,
            // move bot in direction E, otherwise move in direction N
            if (Math.abs(player.getRow() - this.row) <= Math.abs(player.getColumn() - this.column)) {
                move(DIRECTIONS[1]);
            } else {
                move(DIRECTIONS[0]);
            }
        } else if (player.getRow() >= this.row && player.getColumn() >= this.column) {
            // if player in bottom right quadrant of the map visible to bot, move bot in direction if the
            // horizontal distance between the player and bot is greater than the vertical distance,
            // move bot in direction E, otherwise move in direction S
            if (Math.abs(player.getRow() - this.row) <= Math.abs(player.getColumn() - this.column)) {
                move(DIRECTIONS[1]);
            } else {
                move(DIRECTIONS[2]);
            }
        } else if (player.getRow() >= this.row && player.getColumn() <= this.column) {
            // if player in bottom left quadrant of the map visible to bot, move bot in direction if the
            // horizontal distance between the player and bot is greater than the vertical distance,
            // move bot in direction W, otherwise move in direction S
            if (Math.abs(player.getRow() - this.row) <= Math.abs(player.getColumn() - this.column)) {
                move(DIRECTIONS[3]);
            } else {
                move(DIRECTIONS[2]);
            }
        }
    }

}
