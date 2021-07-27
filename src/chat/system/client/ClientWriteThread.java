package chat.system.client;

import java.io.IOException;

/**
 * Subclass of Thread that is used to write to the server.
 * This allows concurrency between reading and writing from and to the server for the client.
 */
public class ClientWriteThread extends Thread {

    protected ChatClient client; // client that the thread was started by

    /**
     * Constructor for ClientWriteThread, assigns the client passed as an argument to the field.
     * @param client The client that the started the thread running.
     */
    public ClientWriteThread(ChatClient client) {
        this.client = client;
    }

    /**
     * This method is called when Thread.start() is run. It runs the processInput method in the client class.
     * This allows the thread to handle writing data to the server.
     */
    @Override
    public void run() {
        try {
            client.processInput();
            try {
                // once the client no longer needs to write to the server, close the I/O buffer and disconnect
                // the client from the server.
                client.getClientInputReader().close();
                client.getServerSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
