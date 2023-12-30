import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;


public class Server {
    private static Map<String, String> validCredentials = new HashMap<>();
    private static Map<String, Integer> userValues = new HashMap<>();
    private static String credentialsFile = "src/credentials.txt";
    private static String logFile = "src/server_log.txt";
    private static Set<String> activeSessions = new HashSet<>(); // Added active sessions set

    public static void main(String[] args) throws Exception {
        loadValidCredentials(credentialsFile);

        System.out.println("Server has started");
        ServerSocket ss = new ServerSocket(9999);

        while (true) {
            System.out.println("Server is waiting for a request");
            Socket s = ss.accept();
            System.out.println("Client connected");

            new ClientHandler(s).start();
        }
    }

    private static void loadValidCredentials(String filename) {
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                String[] credentials = scanner.nextLine().split(":");
                String clientId = credentials[0];
                String password = credentials[1];
                int initialValue = Integer.parseInt(credentials[2]);
                System.out.println("Loaded credentials: " + clientId + ":" + password + ":" + initialValue);
                validCredentials.put(clientId, password);
                userValues.put(clientId, initialValue);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private String clientId;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String str = br.readLine();

                String[] credentials = str.split(":");
                clientId = credentials[0];
                String password = credentials[1];

                OutputStreamWriter os = new OutputStreamWriter(clientSocket.getOutputStream());
                PrintWriter out = new PrintWriter(os);

                if (validCredentials.containsKey(clientId) && validCredentials.get(clientId).equals(password)) {
                    if (isActiveSession(clientId)) {
                        out.println("Error: Client is already logged in");
                        out.flush();
                        // Terminates the connection

                    } else {
                        out.println("Acknowledged");
                        out.flush();

                        addActiveSession(clientId);

                        while (true) {
                            str = br.readLine();

                            if (str == null || str.isEmpty()) {
                                break;
                            }

                            String[] actionParts = str.split(" ");
                            String action = actionParts[0];

                            if (action.equals("INCREASE") || action.equals("DECREASE")) {
                                int amount = Integer.parseInt(actionParts[1]);
                                int newValue;

                                if (action.equals("INCREASE")) {
                                    newValue = userValues.get(clientId) + amount;
                                } else { // DECREASE
                                    newValue = userValues.get(clientId) - amount;
                                }

                                userValues.put(clientId, newValue);

                                logAction(clientId, action, amount);

                                updateCredentialsFile(clientId, password, newValue);

                                out.println("Action " + action + " completed. New Value: " + newValue);
                                out.flush();
                            } else if (action.equals("PRINT")) {
                                int currentValue = userValues.get(clientId);
                                out.println("Current Value: " + currentValue);
                                out.flush();
                            }
                        }

                        removeActiveSession(clientId);
                    }
                } else {
                    out.println("Error: Invalid credentials");
                    out.flush();

                    // Terminates the connection

                }

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean isActiveSession(String clientId) {
            return activeSessions.contains(clientId);
        }

        private void addActiveSession(String clientId) {
            activeSessions.add(clientId);
        }

        private void removeActiveSession(String clientId) {
            activeSessions.remove(clientId);
        }

        private void logAction(String clientId, String action, int amount) {
            try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile, true))) {
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String logMessage = timeStamp + " - Client " + clientId + " " + action + " by " + amount;
                logWriter.write(logMessage);
                logWriter.newLine();
                logWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void updateCredentialsFile(String clientId, String password, int newValue) {
            try (Scanner scanner = new Scanner(new File(credentialsFile))) {
                List<String> updatedCredentials = new ArrayList<>();

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] credentials = line.split(":");
                    String currentClientId = credentials[0];
                    String currentPassword = credentials[1];

                    if (currentClientId.equals(clientId) && currentPassword.equals(password)) {
                        line = clientId + ":" + password + ":" + newValue;
                    }

                    updatedCredentials.add(line);
                }

                try (FileWriter writer = new FileWriter(credentialsFile)) {
                    for (String line : updatedCredentials) {
                        writer.write(line + System.lineSeparator());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
