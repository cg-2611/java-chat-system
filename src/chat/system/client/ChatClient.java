package chat.system.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Main class that connects and runs a client for the chat system.
 */
public class ChatClient {

    protected static final String DEFAULT_ADDRESS = "localhost";
    protected static final int DEFAULT_PORT = 14001;

    protected String address;
    protected int port;

    protected Socket serverSocket; // server socket the client is connected to

    // threads used to handle the read and writing interactions with the server
    protected ClientReadThread readThread;
    protected ClientWriteThread writeThread;

    protected BufferedReader clientInputReader;
    protected BufferedReader serverInputReader;
    protected PrintWriter serverOutputWriter;

    // the clients chosen name
    protected String name;

    /**
     * Constructor to instantiate a new client. It assigns the address and port given as arguments to
     * the respective fields.
     * @param address The host name the port is connected on.
     * @param port The port number the client is connected to.
     */
    public ChatClient(String address, int port) {
        this.address = address;
        this.port = port;
    }

    /**
     * @return The server socket the client is connected to.
     */
    public Socket getServerSocket() {
        return serverSocket;
    }

    /**
     * @return The object used to read the input from the clients console.
     */
    public BufferedReader getClientInputReader() {
        return clientInputReader;
    }

    /**
     * @return The object used to read the output from the server.
     */
    public BufferedReader getServerInputReader() {
        return serverInputReader;
    }

    /**
     * @return The chosen name of the client.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the client to the argument provided.
     * @param name The new value for the name field of the client.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method starts the client running. It asks for and validates a users name and then connects them to the server.
     * Once the user has entered a valid name, they are connected to the server.
     */
    public void start() {
        clientInputReader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter a name to connect to server: ");
        try {
            name = clientInputReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // if the user enters an invalid name, keep requesting a new name until they enter a valid name
        while (name.equals("ChatBot") || name.equals("DoDClient")) {
            System.out.print("The name you entered is not valid, please try another name: ");
            try {
                name = clientInputReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        connectToServer();
    }

    /**
     * Terminates the client program.
     */
    public void stop() {
        System.exit(0);
    }

    /**
     * Connects the client to the server on the same port as the one the client object is instantiated with.
     */
    public void connectToServer() {
        try {
            serverSocket = new Socket(address, port);

            // reads the output from the server (input into the client socket)
            serverInputReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            // used to write to the server
            serverOutputWriter = new PrintWriter(getServerSocket().getOutputStream(), true);
        } catch (UnknownHostException e) {
            System.out.println("The address you have selected cannot be determined. You have not been connected.");
            System.out.println("If this is unexpected, ensure that the address you have selected is correct and try again.");
            stop();
        } catch (IOException e) {
            System.out.println("The server you are trying to connect to is not running. You have not been connected.");
            System.out.println("If this is unexpected, ensure that port you have selected is correct and try again.");
            stop();
        }

        // if the connection was successful, inform the user which server they have connected to and on what address
        System.out.println("Connected to address: " + address + " and port: " + port);

        // broadcast to all clients that this client has connected
        serverOutputWriter.println("User " + this.name + " has joined the chat.");

        // instantiate and start the threads used to handle reading and writing from and to the server
        readThread = new ClientReadThread(this);
        writeThread = new ClientWriteThread(this);

        readThread.start();
        writeThread.start();
    }

    /**
     * Gets the output from the server and prints it to the clients console.
     * If the server has been shut down with this client still connected, reading from the server returns null,
     * so if the client receives null from the server, it terminates the program with a message informing the user of this.
     * @throws IOException
     */
    public void processOutput() throws IOException {
        while (true) {
            String serverResponse = serverInputReader.readLine();

            try {
                if (!serverResponse.equals("null")) {
                    System.out.println(serverResponse);
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
     * Gets the users input from the console and sends it to the server.
     * If the client sends "quit" to the server, the server disconnects the client and a message is sent
     * to all connected clients saying that this user has left.
     * @throws IOException
     */
    public void processInput() throws IOException {
        while (true) {
            String userInput = clientInputReader.readLine();

            if (userInput.toUpperCase().equals("QUIT")) {
                serverOutputWriter.println("User " + this.name + " has left the chat.");
                break;
            }

            serverOutputWriter.println("["+ this.name + "]: " + userInput);
        }
    }

    /**
     * Used only on the instantiation of the client. Validates the optional address argument provided.
     * @param args The array of strings that were provided as command line arguments.
     * @return The address supplied as a command line argument, otherwise, if none is given,
     * the default address is used.
     */
    protected static String getAddressArg(String[] args) {
        String address = DEFAULT_ADDRESS;

        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-cca")) {
                    try {
                        if (!args[i + 1].equals("-ccp")) {
                            // if the label for the address value is found, then assign the value after the label to
                            // the variable to be returned
                            address = args[i + 1];
                        } else {
                            // throw error when there is a label but no value
                            throw new IndexOutOfBoundsException();
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // if the user has provided the label for the address, but not a value
                        // inform the user of this and start the server with the default address
                        String m = "Address parameter given with no value, default address " + DEFAULT_ADDRESS + " used.";
                        System.out.println(m);
                    }
                } else {
                    continue;
                }
            }
        }

        return address;
    }

    /**
     * Used only on the instantiation of the client. Validates the optional port argument provided.
     * If an impossible port number is supplied, the user is told the problem, and the program exits
     * with a message asking for another attempt.
     * @param args The array of strings that were provided as command line arguments.
     * @return The port number supplied as a command line argument, otherwise, if none is given,
     * the default port number is used.
     */
    protected static int getPortArg(String args[]) {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-ccp")) {
                    try {
                        if (!args[i + 1].equals("-cca")) {
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
                        } else {
                            // throw error when there is a label but no value
                            throw new IndexOutOfBoundsException();
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
        ChatClient chatClient = new ChatClient(getAddressArg(args), getPortArg(args));
        chatClient.start();
    }

}
