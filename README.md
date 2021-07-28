# Java Chat System

This is a simple java chat system that uses a multithreaded server to allow multiple clients to connect and send messages to each other. Also there is a very basic chat bot that clients can interact with, and clients can also play a slightly modified version of my [java terminal game](https://github.com/cg-2611/java-terminal-game) that I have made previously.


### Contents:
- [Compile and Run](#compile-and-run)
- [Server](#server)
- [Client](#client)
- [Chat Bot](#chat-bot)
- [Game Client](#game-client)


### Compile and Run
---
First, clone the repository with:
```
git clone https://github.com/cg-2611/java-chat-system.git
```
Next, open the directory created by the `git clone` command:
```
cd java-chat-system
```
Once in the project root directory, to compile the project using the `javac` command, run:
```
javac -d bin @sources.txt
```
To start the server, run:
```
java -cp bin chat.system.server.ChatServer
```
To start and connect a client, run:
```
java -cp bin chat.system.client.ChatClient
```
To start and connect the bot client, run:
```
java -cp bin chat.system.client.BotClient
```
To start and connect the game client, run:
```
java -cp bin chat.system.client.GameClient
```


### Server
---
When running the server, a port number can be specified using the `-csp <PortNumber>` option e.g. to start the server on port 8000, run:
```
java -cp bin chat.system.server.ChatServer -csp 8000
```
If this option is not specified, the default port number of 14001 will be used.

Once running, any connections are handled on their own thread and the server keeps track of all running threads.

When the server receives a message from a client, the message is distributed to all connected clients, including the client that sent the message. The only exception to this is when a client is playing the game.

To shut down the server, enter "EXIT" into the server terminal. Any connected clients will be disconnected if the server is shut down.


### Client
---
The default address used when a client is run is localhost and the default port number is 14001, however different values can be specified for these attributes by using the `-cca <Address>` and `-ccp <PortNumber>` command line options, e.g. to start the client on port 8000, run:
```
java -cp bin chat.system.client.ChatClient -ccp 8000
```
Any messages entered into the client terminal will be sent to the server and any responses received from the server will be printed in the client terminal as well.

Reading from and writing to the server socket are handled in separate threads and are performed concurrently.

To disconnect a client enter "QUIT" into the client terminal.


### Chat Bot
---
The bot client can be run with the same options as the regular chat client.

The bot client also behaves similarly to the regular chat client with regard to reading from and writing to the server and how these processes happen concurrently.

To trigger a response from the chat bot, a received message must contain "@bot" somewhere in the message. This is important as it lets the bot know that it must reply to the message, if the message does not contain "@bot" then there will be no response.

The bot very basic and can only understand and respond to a very limited selection of phrases.

To disconnect the chat bot from the server manually, enter "QUIT" into that bot client terminal.


### Game Client
---
The game client can be run with the same options as the regular chat client.

The game client also behaves similarly to the regular chat client with regard to reading from and writing to the server and how these processes happen concurrently.

Only one client can play the game at any one time, if any other clients attempt to start a game while it is being played, their request will be rejected.

To start playing, the game client must receive the message "JOIN" (specifically all uppercase). After the game client receives this message, as long as no one else is playing the game, the player is spawned.

Once the player has been spawned, they can interact with the game by using the [game commands](https://github.com/cg-2611/java-terminal-game#game-commands). 
> Note: all game commands must be in uppercase.

The game client process any commands it receives and the result of performing the command is sent to only the client playing the game.

All clients are sent a message when a player spawns in the game and exits the game so other users are able to tell when the game is free to be played.

If the player disconnects from the server before their game is finished, the game client can recognise this asn it will simply reset the game and allow another player to start playing.
