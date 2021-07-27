package chat.system.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

/**
 * Subclass of ChatClient that behaves as a computer controlled bot.
 */
public class BotClient extends ChatClient {

    // arrays of responses the bot can give
    private static final String[] BOT_QUESTIONS = {"How are you?", "Are you well?", "How do you feel?", "Are you ok?"};
    private static final String[] GREETING_RESPONSES = {"Hello.", "Hi.", "Greetings."};
    private static final String[] BOT_FEELING_RESPONSES = {"I am well.", "I am good.", "I am fine."};
    private static final String[] USER_POSITIVE_FEELING_RESPONSES = {"That is good to hear.", "I am glad to hear thet you feel well.", "I think it is great you feel this way."};
    private static final String[] USER_NEGATIVE_FEELING_RESPONSES = {"It is unfortunate that you feel like this.", "I am sorry to hear you feel this way.", "I hope you start to feel more positive soon."};
    private static final String[] THANK_RESPONSES = {"You are welcome.", " No problem."};
    private static final String[] GOODBYE_RESPONSES = {"Goodbye.", "Bye.", "Farewell.", "See you later."};
    private static final String[] UNKNOWN_RESPONSES = {"I am sorry, but I do not understand.", "Apologies, I do not know how to respond to that."};

    private boolean responseRequired;

    private ArrayList<String> botMessages; // messages the bot has received

    private int responsesGiven;

    /**
     * Constructor for a BotClient object, assigns fields initial values.
     * @param address The host name the port is connected on.
     * @param port The port number the client is connected to.
     */
    public BotClient(String address, int port) {
        super(address, port);

        responseRequired = false;

        botMessages = new ArrayList<String>();

        responsesGiven = 0;
    }

    /**
     * @return True if the bot needs to respond to a message. False if there are no messages it has not responded to.
     */
    public boolean getResponseRequired() {
        return responseRequired;
    }

    /**
     * Starts the bot running. It sets the name of the bot and connects it to the server.
     */
    @Override
    public void start() {
        setName("ChatBot");
        connectToServer();
    }

    /**
     * Adds the message provided as an argument to the ArrayList of messages.
     * Also sets responseRequired to true to prompt a reply to the added message.
     * @param message
     */
    public void addMessage(String message) {
        botMessages.add(message);
        responseRequired = true;
    }

    /**
     * Takes the message and checks for specific phrases and words to determine the type of message
     * it received.
     * @param message The message received from a user.
     * @return The type of message the bot thinks it has received, as a string.
     */
    public String analyseMessage(String message) {
        String messageType = "";

        if (message.contains("hello") || message.contains("hi") || message.contains("greetings")) {
            messageType = "greeting";
        } else if (message.contains("how are you") || message.contains("are you ok") || message.contains("how doe you feel")) {
            messageType = "bot-feeling";
        } else if (message.contains("not well") || (message.contains("bad") && !message.contains("not bad"))
                || message.contains("not great") || message.contains("sad") || message.contains("upset")
                || message.contains("low") || message.contains("not good")) {
            messageType = "user-negative-feeling";
        } else if (message.contains("good") || message.contains("well") || message.contains("ok")
                || message.contains("fine") || message.contains("great") || message.contains("brilliant")
                || message.contains("excellent") || message.contains("happy") || message.contains("not bad")) {
            messageType = "user-positive-feeling";
        } else if (message.contains("thank you") || message.contains("thanks")) {
            messageType = "thanks";
        } else if (message.contains("bye") || message.contains("goodbye") || message.contains("see you later")) {
            messageType = "goodbye";
        }

        return messageType;
    }

    /**
     * Takes the type of message generated from the analysis of the message and generates a response it deems appropriate.
     * It does this by generating a random number and using this number as an index to get a random element from the
     * array that has the responses for the given message type. In some cases, responses are concatenated to form a more
     * complex sentence as a reply. It does this by, in appropriate circumstances, generating a random number and adding an
     * extra clause to the end of the initial response if that number is even.
     * @param messageType The type of message returned by analyseMessage()
     * @return The response the bot has generated for the message received.
     */
    public String generateResponse(String messageType) {
        Random random = new Random();
        int replyIndex = 0;
        String generatedResponse = "";

        switch (messageType) {
            case "greeting":
                replyIndex = random.nextInt(GREETING_RESPONSES.length);
                generatedResponse += GREETING_RESPONSES[replyIndex] + " ";

                if (random.nextInt() % 2 == 0) {
                    replyIndex = random.nextInt(BOT_QUESTIONS.length);
                    generatedResponse += BOT_QUESTIONS[replyIndex];
                }

                break;

            case "bot-feeling":
                replyIndex = random.nextInt(BOT_FEELING_RESPONSES.length);
                generatedResponse += BOT_FEELING_RESPONSES[replyIndex] + " ";

                if (random.nextInt() % 2 == 0) {
                    replyIndex = random.nextInt(BOT_QUESTIONS.length);
                    generatedResponse += BOT_QUESTIONS[replyIndex];
                }

                break;

            case "user-positive-feeling":
                replyIndex = random.nextInt(USER_POSITIVE_FEELING_RESPONSES.length);
                generatedResponse += USER_POSITIVE_FEELING_RESPONSES[replyIndex] + " ";
                break;

            case "user-negative-feeling":
                replyIndex = random.nextInt(USER_NEGATIVE_FEELING_RESPONSES.length);
                generatedResponse += USER_NEGATIVE_FEELING_RESPONSES[replyIndex] + " ";
                break;

            case "thanks":
                replyIndex = random.nextInt(THANK_RESPONSES.length);
                generatedResponse += THANK_RESPONSES[replyIndex] + " ";
                break;

            case "goodbye":
                replyIndex = random.nextInt(GOODBYE_RESPONSES.length);
                generatedResponse += GOODBYE_RESPONSES[replyIndex] + " ";
                break;

            default:
                replyIndex = random.nextInt(UNKNOWN_RESPONSES.length);
                generatedResponse = UNKNOWN_RESPONSES[replyIndex];
                break;
        }

        return generatedResponse.trim();
    }

    /**
     * Gets the next message the bot is yet to respond to and generates the response for that message.
     * @return The final string containing the response that will be sent to the server.
     */
    public String getResponse() {
        String nextMessage = botMessages.get(responsesGiven).toLowerCase();

        String response = generateResponse(analyseMessage(nextMessage));

        return response;
    }

    /**
     * Gets the output from the server and checks if the string contains "@bot". If so, the message is added to
     * the ArrayList of messages it has received. This in turn will prompt a response from the client.
     * If the server has been shut down with this client still connected, reading from the server returns null,
     * so if the client receives null from the server, it terminates the program with a message informing the user of this.
     */
    @Override
    public void processOutput() throws IOException {
        while (true) {
            String serverResponse = super.serverInputReader.readLine();

            try {
                if (!serverResponse.equals("null")) {
                    System.out.println(serverResponse);

                    if (serverResponse.contains("@bot")) {
                        addMessage(serverResponse);
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
     * Initially creates and starts a Thread that handles the termination of this client when "quit" is entered into
     * it's console. Then when it needs to respond to a message it then loops until all messages have been responded to
     * and generates and sends a response for each one.
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
            // if a response is required, respond to all messages that have no replies
            if (getResponseRequired()) {
                while (responsesGiven < botMessages.size()) {
                    String botResponse = getResponse();
                    serverOutputWriter.println("[" + getName() + "]: " + botResponse);

                    responsesGiven++;
                }

                responseRequired = false;
            }
            try {
                ClientWriteThread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient(getAddressArg(args), getPortArg(args));
        botClient.start();
    }

}
