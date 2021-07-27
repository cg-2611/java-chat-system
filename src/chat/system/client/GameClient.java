package chat.system.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import chat.system.game.main.GameLogic;

/**
 * Subclass of ChatClient that can be used to play the game.
 */
public class GameClient extends ChatClient {

    private boolean gameRequested;

    private String playerRequested; // name of player that requested a new game

    private GameLogic activeGame;
    private String activePlayer;

    private Boolean playerMoved; // set to true when the player makes a move in the game
    private String playerMove; // the move the player made

    /**
     * Constructor for a BotClient object, assigns necessary fields initial values.
     * @param address The host name the port is connected on.
     * @param port The port number the client is connected to.
     */
    public GameClient(String address, int port) {
        super(address, port);

        gameRequested = false;
        playerMoved = false;
    }

    /**
     * @return True if a user has requested to play the game. False if no such request has been made.
     */
    public boolean getGameRequested() {
        return gameRequested;
    }

    /**
     * @return The name of the player that has made the request to play the game.
     */
    public String getPlayerRequested() {
        return playerRequested;
    }

    /**
     * @return True if a player has entered a move to be performed in the game.
     */
    public Boolean getPlayerMoved() {
        return playerMoved;
    }

    /**
     * @return The move the player desires to execute.
     */
    public String getPlayerMove() {
        return playerMove;
    }

    /**
     * Sets the value of gameRequested to the value of the argument provided.
     * @param gameRequested The new value of gameRequested.
     */
    public void setGameRequested(boolean gameRequested) {
        this.gameRequested = gameRequested;
    }

    /**
     * Sets the name of the player who has requested to play the game.
     * @param playerRequested The name of the client who made the request.
     */
    public void setPlayerRequested(String playerRequested) {
        this.playerRequested = playerRequested;
    }

    /**
     * Sets the value of playerMoved to the value of the argument provided.
     * @param playerMoved The new value of playerMoved.
     */
    public void setPlayerMoved(Boolean playerMoved) {
        this.playerMoved = playerMoved;
    }

    /**
     * Sets the move of the player to the move provided as the argument.
     * @param playerMove The move the player wants to make.
     */
    public void setPlayerMove(String playerMove) {
        this.playerMove = playerMove;
    }

    /**
     * Starts the game client running. It sets the name of the client and connects it to the server.
     */
    @Override
    public void start() {
        setName("GameClient");
        connectToServer();
    }

    /**
     * Checks if all letter characters in a string are uppercase. It does this by looping through an array of
     * the characters in the string and checking the case of each letter.
     * @param string The string of characters to be assessed.
     * @return True if all the characters in a string are uppercase, false if at least one of the characters
     * is not uppercase.
     */
    private boolean isStringUppercase(String string) {
        char[] charArray = string.toCharArray();

        for (int i = 0; i < charArray.length; i++) {
            if (!Character.isUpperCase(charArray[i]) && Character.isLetter(charArray[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the output from the server. If a user has requested to play the game, the necessary field values are set.
     * Otherwise, if another message is received, the message is trimmed and the content is set as the move the player
     * will make. The players game is ended if they leave the chat mid-game. If the server has been shut down with this
     * client still connected, reading from the server returns null, so if the client receives null from the server,
     * it terminates the program with a message informing the user of this.
     */
    @Override
    public void processOutput() throws IOException {
        while (true) {
            String serverResponse = super.serverInputReader.readLine();
            try {
                if (!serverResponse.equals("null")) {
                    System.out.println(serverResponse);

                    if (serverResponse.startsWith("[")) {
                        // get the name of the client of the message received
                        int openBracketIndex = serverResponse.indexOf("[");
                        int closeBracketIndex = serverResponse.indexOf("]");
                        String playerName = serverResponse.substring(openBracketIndex + 1, closeBracketIndex);

                        if (serverResponse.endsWith("JOIN")) {
                            setGameRequested(true);
                            setPlayerRequested(playerName);
                        } else {
                            if (!this.name.equals(playerName)) {
                                // if there is someone currently playing the game and a message is received, from the
                                // player, that is of the format of a game command, i.e. all uppercase string,
                                // their message is processed and stored as the move for their turn
                                if (activeGame != null && activePlayer.equals(playerName)) {
                                    int messageStartIndex = closeBracketIndex + "]:".length();
                                    String playerMessageContent = serverResponse.substring(messageStartIndex).trim();

                                    if (activeGame.gameRunning() && isStringUppercase(playerMessageContent)) {
                                        setPlayerMove(playerMessageContent);
                                        setPlayerMoved(true);
                                    }
                                }
                            }
                        }
                    }

                    if (serverResponse.equals(activePlayer +  " has left the chat.")) {
                        endActiveGame();
                    }
                } else {
                    throw new NullPointerException();
                }
            } catch (NullPointerException e) {
                System.out.println("The server has been shut down, as a result you have been disconnected.");
                stop();
                break;
            }
        }
    }

    /**
     * Initially creates and starts a Thread that handles the termination of this
     * client when "quit" is entered into its console. Then if a game is requested,
     * it handles the request. The if the player currently playing the game makes
     * their move, the outcome of this move is sent to the server to be sent to the player.
     */
    @Override
    public void processInput() throws IOException {
        Thread quitThread = new Thread() {
            @Override
            public void run() {
                BufferedReader botConsoleInputReader = new BufferedReader(new InputStreamReader(System.in));

                String serverInput = "";
                while (!serverInput.toUpperCase().equals("QUIT")) {
                    try {
                        serverInput = botConsoleInputReader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    botConsoleInputReader.close();
                    try {
                        serverSocket.close();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.exit(0);
                }
            }
        };
        quitThread.start();

        while (true) {
            if (getGameRequested()) {
                // if there is no one currently playing the game, tell the player that they are now playing the game
                // and start a new game.
                // if someone is already playing the game, inform the user that they must wait until
                // the other user has finished playing the game.
                if (activeGame == null && activePlayer == null) {
                    String gameClientResponse = "Player " +  getPlayerRequested() + " has been spawned.";
                    serverOutputWriter.println("[" + this.name + "]: " + gameClientResponse);
                    setGameRequested(false);

                    startActiveGame(new GameLogic(), getPlayerRequested());
                } else {
                    String m = " is currently playing the game, please wait for their game to finish and try again.";
                    serverOutputWriter.println("[" + this.name + "]: " + activePlayer + m);
                    setGameRequested(false);
                }
            }

            if (getPlayerMoved()) {
                // get the outcome of the players move from the GameLogic object
                String moveResult = activeGame.processAction(getPlayerMove());
                serverOutputWriter.println("[" + this.name + "]: " + moveResult);
                setPlayerMoved(false);

                if (moveResult.contains("caught") || moveResult.contains("WIN") || moveResult.contains("LOSE")) {
                    String gameClientResponse = "Player " +  this.activePlayer + " has exited.";
                    serverOutputWriter.println("[" + this.name + "]: " + gameClientResponse);
                    endActiveGame();
                }
            }

            try {
                ClientWriteThread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starts a new instance of a GameLogic object.
     * @param activeGame The new GameLogic object that the player will interact with.
     * @param activePlayer The player that will be playing the game.
     */
    public void startActiveGame(GameLogic activeGame, String activePlayer) {
        this.activeGame = activeGame;
        this.activePlayer = activePlayer;
        this.activeGame.runGame();
    }

    /**
     * Resets the game being played so that a new user can play the game.
     */
    public void endActiveGame() {
        this.activeGame = null;
        this.activePlayer = null;
    }

    public static void main(String[] args) {
        GameClient gameClient = new GameClient(getAddressArg(args), getPortArg(args));
        gameClient.start();
    }

}
