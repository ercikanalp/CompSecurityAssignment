# Simple Client-Server Application

This is a simple Java client-server application that allows clients to log in with credentials and perform actions on their account values.

## Getting Started

1. Clone the repository to your local machine.
2. Open the project in your favorite Java IDE (e.g., IntelliJ IDEA, Eclipse).
3. Compile and run the `Server` class to start the server.
4. Compile and run the `Client` class to start one or more client(s).
5. Check the config.json files to see the ids and passwords of the clients.
6. In the client console, enter the id and password of the client you want to log in with.
7. After that, follow the instructions in the client console to perform actions on the account values.

## Features
- Client authentication based on credentials.
- Actions: Increase, Decrease, Print current value.
- Server logs actions to a file (`server_log.txt`).
- Prevents multiple logins for the same client.
- Interactive client console.
- Active value updates on the credentials file (credentials.json).

## Author
Alp Ercikan
