package chat.system.client;

import java.io.IOException;

/**
 * Subclass of Thread that is used to read from the server.
 * This allows concurrency between reading and writing from and to the server for the client.
 */
public class ClientReadThread extends Thread {

    private ChatClient client; // client that the thread was started by

    /**
     * Constructor for ClientReadThread, assigns the client passed as an argument to the field.
     * @param client The client that the started the thread running.
     */
    public ClientReadThread(ChatClient client) {
        this.client = client;
    }

    /**
     * This method is called when Thread.start() is run. It runs the processOutput method in the client class.
     * This allows the thread to handle reading data from the server.
     */
    @Override
    public void run() {
        try {
            client.processOutput();
        } catch (IOException e) {
            try {
                // if there are any exceptions thrown, close the I/O buffer and disconnect the client from the server
                client.getServerInputReader().close();
                client.getServerSocket().close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

}
