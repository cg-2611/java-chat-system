package chat.system.game.map;

/**
 * Reads and contains in memory the map of the game.
 */
public class Map {

    private char[][] map;

    private String mapName;

    private int goldRequired;

    /**
     * Default constructor, creates the default map "Chat System Map".
     */
    public Map() {
        mapName = "Chat System Map";
        goldRequired = 2;
        map = new char[][] {
                {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
                {'#', 'E', '.', '.', '.', '.', '#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'},
                {'#', '#', '#', '#', '.', '.', '#', 'G', '#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#'},
                {'#', '.', '.', '#', '.', '.', '#', '#', '#', '.', '.', '#', '#', '#', '.', '.', '.', '.', '.', '#'},
                {'#', '.', '.', '.', '.', '.', '.', '#', '#', '.', '.', '#', '#', '#', '.', '#', '#', '#', '.', '#'},
                {'#', '.', '.', '#', '#', '#', '.', '.', '#', '.', '.', '#', 'G', '#', '.', '.', '.', '#', '.', '#'},
                {'#', '.', '.', '.', 'G', '#', '.', '.', '.', '.', '.', '#', '.', '#', '#', '#', '.', '#', '.', '#'},
                {'#', '.', '.', '.', '.', '#', '.', '.', '.', '.', '.', '#', '.', '.', '.', '.', '.', '#', 'E', '#'},
                {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#','#' }};
    }

    /**
     * @return Gold required to exit the current map.
     */
    public int getGoldRequired() {
        return goldRequired;
    }

    /**
     * @return The map as stored in memory.
     */
    public char[][] getMap() {
        return map;
    }

    /**
     * @return The name of the current map.
     */
    public String getMapName() {
        return mapName;
    }

    /**
     * @return The number of rows in the map.
     */
    public int getNumberOfRows() {
        return getMap().length;
    }

    /**
     * @return The number of columns in the map.
     */
    public int getNumberOfColumns() {
        return getMap()[0].length;
    }

    /**
     * Changes the character at given position to the new character provided.
     * @param row the row of the character to be changed.
     * @param column the column of the character to be changed.
     * @param newCharacter the new character the old character is to be replaced with.
     */
    public void changeCharacter(int row, int column, char newCharacter) {
        map[row][column] = newCharacter;
    }

}
