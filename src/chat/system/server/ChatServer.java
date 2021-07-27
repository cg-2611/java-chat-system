package chat.system.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Main class that handles and runs the server for the chat system.
 */
public class ChatServer {

    private static final int DEFAULT_PORT = 14001;

    private ServerSocket serverSocket;

    // a set that stores all the threads that have a client socket connected to the
    // server, a set is used to ensure that there were no duplicate client sockets
    // connected
    private Set<ServerThread> users;

    private String playerClient; // field to store the name of the client playing the game

    /**
     * Constructor to instantiate a new server. It initialises the server socket
     * object and the HashSet used to store the threads running.
     * @param port The port that the server gets bound to.
     */
    public ChatServer(int port) {
        try {
            serverSocket = new ServerSocket(port);

            users = new HashSet<ServerThread>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return The name of the client who is playing the game, null if no one is playing.
     */
    public String getPlayerClient() {
        return playerClient;
    }

    /**
     * Assigns the value of the argument to the playerClient field.
     *
     * @param playerClient The name of the new client playing the game.
     */
    public void setPlayerClient(String playerClient) {
        this.playerClient = playerClient;
    }

    /**
     * Method that starts the server running. It also starts a new instance of a
     * Thread that is used to stop the server when the server master enters "exit"
     * into the console. Once started, the server will run infinitely, accepting any
     * requested connections. When a connection is accepted, a new instance of a
     * ServerThread is added to the set ServerThread objects.
     */
    public void start() {
        System.out.println("Server listening on port " + serverSocket.getLocalPort() + "...");

        Thread exitThread = new Thread() {
            @Override
            public void run() {
                BufferedReader serverConsoleInputReader = new BufferedReader(new InputStreamReader(System.in));

                String serverInput = "";
                while (!serverInput.toUpperCase().equals("EXIT")) {
                    try {
                        serverInput = serverConsoleInputReader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    serverConsoleInputReader.close();
                    stopServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        exitThread.start();

        while (true) {
            try {
                // accept a connection if one is requested and print that a connection has been
                // accepted along with the port the server is listening on and the port in which
                // the client is connected
                Socket clientSocket = serverSocket.accept();
                String connectionInfo = + serverSocket.getLocalPort() + " ; " + clientSocket.getPort();
                System.out.println("Server accepted connection on: " + connectionInfo);

                addUser(new ServerThread(clientSocket, this));
            } catch (IOException e) {
                try {
                    exitThread.join();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * Adds the given ServerThread, representing a user, to the set of ServerThread
     * objects. Once an object has been added to the set, the thread it is run on is
     * started.
     * @param user: The instance of a ServerThread to be added to the set.
     */
    public void addUser(ServerThread user) {
        users.add(user);
        user.start();
    }

    /**
     * Removes a given ServerThread, representing a user, from the set of
     * ServerThread objects. Once an object has been removed from the set, the
     * thread is joined.
     *
     * @param user The instance of the ServerThread to be removed from the set.
     */
    public void removeUser(ServerThread user) {
        users.remove(user);
        try {
            user.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the server's response to all client sockets connected.
     * @param serverResponse The message being sent out to all users.
     */
    public void sendResponseToAllUsers(String serverResponse) {
        for (ServerThread user : users) {
            user.sendServerResponse(serverResponse);
        }
    }

    /**
     * Sends the server's response to only the client socket of the user playing the
     * game.
     * @param serverResponse The message being sent to the client socket.
     */
    public void sendResponseToPlayer(String serverResponse) {
        for (ServerThread user : users) {
            if (user.getClientName().equals(playerClient)) {
                user.sendServerResponse(serverResponse);
            }
        }
    }

    /**
     * Closes the server socket and then stops the program running.
     */
    public void stopServer() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    /**
     * Used only on the instantiation of the server. Validates the optional port argument provided.
     * If an impossible port number is supplied, the user is told the problem, and the program exits
     * with a message asking for another attempt.
     * @param args The array of strings that were provided as command line arguments.
     * @return The port number supplied as a command line argument, otherwise, if none is given,
     * the default port number is used.
     */
    private static int getPortArg(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-csp")) {
                    try {
                        // if the label for the port value is found, then assign the value after the label to
                        // the variable to be returned
                        int portArg = Integer.parseInt(args[i + 1]);

                        // if this value is in the valid range for a port number, then accept it
                        if (portArg >= 1 && portArg <= 65535) {
                            port = portArg;
                        } else {
                            String m = "Port number not in valid range, please try again with a valid port number.";
                            System.out.println(m);
                            System.exit(0);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // if the user has provided the label for the port number, but not a value,
                        // inform the user of this and start the server with the default port number
                        String m = "Port parameter given with no value, default port " + DEFAULT_PORT + " used.";
                        System.out.println(m);
                    }
                } else {
                    continue;
                }
            }
        }

        return port;
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(getPortArg(args));
        server.start();
    }
}