package chat.system.game.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import chat.system.game.map.Map;

/**
 * Runs the game with a human player and contains code needed to read inputs.
 */
public class HumanPlayer extends Player{

    private final String[] COMMANDS = {"HELLO", "GOLD", "MOVE N", "MOVE E", "MOVE S", "MOVE W", "PICKUP", "LOOK", "EXIT"};

    private ArrayList<String> availableCommands;

    private BufferedReader br;

    private int gold; // the amount of gold the player owns

    /**
     * Constructor for HumanPlayer object.
     * @param gameMap
     */
    public HumanPlayer(Map gameMap) {
        super(gameMap);

        availableCommands = new ArrayList<String>();
        availableCommands.addAll(Arrays.asList(COMMANDS));

        br = new BufferedReader(new InputStreamReader(System.in));

        gold = 0;
    }

    /**
     * @return The gold the player has collected.
     */
    public int getGold() {
        return gold;
    }

    /**
     * Called when the player picks up gold in the game.
     */
    public void incrementGold() {
        gold++;
    }

    /**
     * Process the users command and return the command as a string as long as it is a valid command.
     * @return the processed command or "Invalid" if the command is not recognised.
     */
    public String getNextAction() {
        while (true) {
            String command = getInputFromPlayer().trim().toUpperCase();

            if (command == null) {
                try {
                    br.close();
                } catch (IOException e) {
                   System.err.println("\nError: " + e.getMessage());
                   System.err.println("Cause: " + e.getCause());
                }

                System.exit(0);
            }

            if (availableCommands.contains(command)) {
                return command;
            } else {
                return "Invalid";
            }
        }
    }

    /**
     * Read player's input.
     * @return : A string containing the input the player entered.
     */
    private String getInputFromPlayer() {
        try {
            return br.readLine();
        } catch (IOException e) {
            System.err.println("\nError: " + e.getMessage());
            System.err.println("Cause: " + e.getCause());
            return null;
        }
    }

}
