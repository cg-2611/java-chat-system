package chat.system.game.entity;

import java.util.Random;

import chat.system.game.map.Map;

/**
 * Superclass for both {@link HumanPlayer} and {@link Bot}.
 * Contains attributes and methods common to both subclasses.
 */
public class Player {

    protected Map gameMap; // the map the player is playing in

    // player position
    protected int row;
    protected int column;

    /**
     * Constructor that assigns the map argument to the map object and also
     * assigns the player a column and row value.
     * @param gameMap the map generated in the {@link GameLogic} class
     */
    public Player(Map gameMap) {
        this.gameMap = gameMap;

        initialisePlayerPosition();
    }

    /**
     * Places the player at random coordinates in the map.
     * <br></br>
     * If the player is on a wall character or on gold character, generate new coordinates.
     */
    public void initialisePlayerPosition() {
        Random rand = new Random();

        do {
            row = rand.nextInt(gameMap.getNumberOfRows());
            column = rand.nextInt(gameMap.getNumberOfColumns());
        } while (gameMap.getMap()[row][column] == '#' || gameMap.getMap()[row][column] == 'G');
    }

    /**
     * @return The row the player in on.
     */
    public int getRow() {
        return row;
    }

    /**
     * @return The column the player the player.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Updates the row field.
     * @param row the new value of the row the player is on.
     */
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * Updates the column field.
     * @param column the new value of the column the player is on.
     */
    public void setColumn(int column) {
        this.column = column;
    }

    /**
     * Changes the coordinates of the player according to the direction of the movement.
     * @param direction the direction the player moves in.
     */
    public void move(char direction) {
        switch (direction) {
            case 'N': setRow(this.row - 1);
                      break;

            case 'E': setColumn(this.column + 1);
                      break;

            case 'S': setRow(this.row + 1);
                      break;

            case 'W': setColumn(this.column - 1);
                      break;

            default: break;
        }
    }

}
