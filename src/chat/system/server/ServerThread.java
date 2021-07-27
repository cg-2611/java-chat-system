package chat.system.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Subclass of Thread that handles a single client connection to the server.
 * This allows the server to be multi-threaded and allow for multiple connections at any given time.
 */
public class ServerThread extends Thread {

    private Socket clientSocket;
    private ChatServer server; // server that its client is connected to

    private PrintWriter clientOutputWriter; // used to write to the client

    private String clientName;

    /**
     * Constructor for ServerThread, assigns arguments to respective fields;
     * @param clientSocket The client socket that will connect with the server.
     * @param server The server that has instantiated this ServerThread.
     */
    public ServerThread(Socket clientSocket, ChatServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    /**
     * @return The chosen user name of the client connected to the server on this thread.
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * This method is called when Thread.start() is run. It creates objects to handle I/O between the client and the server.
     * Loops infinitely until the client disconnects. Each loop, it receives input from the client and sends it to all
     *  connected users, and in special circumstances, to only a single client.
     */
    @Override
    public void run() {
        try {
            // BufferedReader that reads the input from the client socket
            BufferedReader clientInputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // PrintWriter that writes to the client socket
            clientOutputWriter = new PrintWriter(clientSocket.getOutputStream(), true);

            while (true) {
                String clientInput = clientInputReader.readLine();

                if (!clientInput.toUpperCase().equals("QUIT")) {
                    // if a message saying that a user has joined is received, extract the name from the message and
                    // set the clientName to this value
                    if (!clientInput.startsWith("[") && clientInput.endsWith("joined the chat.")) {
                        clientName = getNameFromString(clientInput, "User", "has");
                    }

                    synchronized(server) {
                        // if a message from the GameClient is received, the message content needs to be examined
                        // so that only the player is sent the response from the game they are playing
                        if (clientInput.startsWith("[GameClient]:")) {
                            // all clients receive messages involving a client starting, attempting to start,
                            // and ending the game session, any other messages from the GameClient are sent only
                            // to the player client
                            if (clientInput.endsWith("spawned.")) {
                                String playerName = getNameFromString(clientInput, "Player", "has");
                                server.setPlayerClient(playerName);
                                server.sendResponseToAllUsers(clientInput);
                            } else if (clientInput.endsWith("exited.")) {
                                server.setPlayerClient(null);
                                server.sendResponseToAllUsers(clientInput);
                            } else if (!clientInput.endsWith("try again.")) {
                                // if the message is about a client interaction with the game,
                                // send the response to only the player
                                // if the player calls LOOK, read and send each individual
                                // line of the output to the player in order
                                if (server.getPlayerClient() != null) {
                                    server.sendResponseToPlayer(clientInput);

                                    if (clientInput.equals("[GameClient]: ")) {
                                        for (int i = 0; i < 5; i++) {
                                            clientInput = clientInputReader.readLine();
                                            server.sendResponseToPlayer(clientInput);
                                        }
                                    }
                                }
                            } else {
                                server.sendResponseToAllUsers(clientInput);
                            }
                        } else {
                            // any other messages are sent to all clients.
                            server.sendResponseToAllUsers(clientInput);
                        }
                    }
                } else {
                    break;
                }
            }

            server.removeUser(this);
        } catch (IOException e) {
            System.out.println("Client disconnected.");
            server.removeUser(this);
        } catch (NullPointerException e) {
            System.out.println("Client disconnected.");
        }
    }

    /**
     * Extracts the sub-string from the given string containing the name of the client.
     * @param string The string containing the clients name.
     * @param wordBefore The word that precedes the clients name in the string.
     * @param wordAfter The word that proceeds the client name in the string.
     * @return The string found between the final two arguments, the clients name.
     */
    private String getNameFromString(String string, String wordBefore, String wordAfter) {
        int nameStartIndex = string.indexOf(wordBefore) + wordBefore.length();
        int nameEndIndex = string.lastIndexOf(wordAfter);
        return string.substring(nameStartIndex, nameEndIndex).trim();
    }

    /**
     * Writes the message supplied as the argument to the client socket.
     * @param serverResponse The response from the server that is to be sent to the client socket and printed.
     */
    public void sendServerResponse(String serverResponse) {
        clientOutputWriter.println(serverResponse);
    }

}
