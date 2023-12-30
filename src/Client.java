import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws Exception {
        Socket s = null;
        try {
            Scanner scanner = new Scanner(System.in);
            String ip = "localhost";
            int port = 9999;
            s = new Socket(ip, port);

            // Get client ID and password from user
            System.out.println("Enter Client ID:");
            String clientId = scanner.nextLine();
            System.out.println("Enter Password:");
            String password = scanner.nextLine();

            // Sending the credentials in the format "ID:PASSWORD"
            String credentials = clientId + ":" + password;

            OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());
            PrintWriter out = new PrintWriter(os);
            out.println(credentials);
            out.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String serverResponse = br.readLine();
            if (serverResponse != null && serverResponse.equals("Acknowledged")) {
                System.out.println("Login successful");
            } else {
                throw new IOException("Login failed: " + serverResponse);
            }

            // Handle actions
            while (true) {
                System.out.println("Choose action (1 for INCREASE, 2 for DECREASE, 3 for PRINT, 0 to exit):");
                int choice = scanner.nextInt();

                if (choice == 0) {
                    // Exit and log "Logged out"
                    System.out.println("Logged out");
                    break;
                }

                if (choice == 3) {
                    // Print current value
                    out.println("PRINT");
                    out.flush();
                    serverResponse = br.readLine();
                    if (serverResponse != null) {
                        System.out.println("Data from Server: " + serverResponse);
                    } else {
                        throw new IOException("No response from server.");
                    }
                }

                else if (choice == 1 || choice == 2) {
                    // Increase or decrease value
                    System.out.println("Enter amount:");
                    int amount = scanner.nextInt();
                    String action = choice == 1 ? "INCREASE" : "DECREASE";
                    out.println(action + " " + amount);
                    out.flush();
                    serverResponse = br.readLine();
                    if (serverResponse != null) {
                        System.out.println("Data from Server: " + serverResponse);
                    } else {
                        throw new IOException("No response from server.");
                    }
                }
            }
        } catch (IOException e) {
            // Handle the exception for unsuccessful login and invalid input for action
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }
}
