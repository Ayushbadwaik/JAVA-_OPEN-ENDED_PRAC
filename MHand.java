import java.net.Socket;
import java.awt.Color;
import java.awt.TextArea;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

public class MHand implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private TextArea chatLog;
    private CServer server;
    private String clientName;
    private Color clientColor;

    public MHand(Socket socket, TextArea chatLog, CServer server) {
        this.socket = socket;
        this.chatLog = chatLog;
        this.server = server;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            if (server != null) {
                this.clientName = reader.readLine();
                this.clientColor = generateRandomColor();

                // Notify the client of their assigned color
                writer.write("COLOR:" + clientColor.getRGB() + "\n");
                writer.flush();

                // Notify all clients with the updated client list
                server.updateClientList();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                if (server != null) {
                    // Parse the message to include recipient information
                    String[] parts = message.split(":", 4);
                    String recipient = parts[2];
                    String msg = parts[3];

                    // Broadcast including recipient
                    server.broadcastMessage(clientName, clientColor.getRGB(), recipient, msg);
                } else {
                    chatLog.append(message + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(String message) throws IOException {
        writer.write(message + "\n");
        writer.flush();
    }

    public void closeConnection() {
        try {
            if (server != null) {
                server.removeClient(this);
                server.updateClientList(); // Update client list when a client disconnects
            }
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Color generateRandomColor() {
        Random rand = new Random();
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    public String getClientName() {
        return clientName;
    }

    public Color getClientColor() {
        return clientColor;
    }
}
