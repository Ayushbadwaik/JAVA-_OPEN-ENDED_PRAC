import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;

public class CServer {
    private ServerSocket serverSocket;
    private List<MHand> clients = new ArrayList<>();
    private TextArea chatLog;
    private static final int PORT = 8000;

    public CServer() {
        Frame frame = new Frame("Central Server");
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());
        frame.setBackground(Color.LIGHT_GRAY);

        chatLog = new TextArea(10, 40);
        chatLog.setEditable(false);
        frame.add(chatLog, BorderLayout.CENTER);
        frame.setVisible(true);
        try {
            String ip = JOptionPane.showInputDialog(frame, "Enter Server IP (e.g., 0.0.0.0):", "@AYUSH");
            serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(ip));
            chatLog.append("Server started at " + ip + ":" + PORT + ".\nDesigned & Developed by AYUSH B.\n Waiting for clients...\n");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                chatLog.append("Client connected: " + clientSocket.getInetAddress().getHostAddress() + "\n");
                MHand clientHandler = new MHand(clientSocket, chatLog, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
                updateClientList(); // Update client list for all connected clients
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                try {
                    for (MHand client : clients) {
                        client.closeConnection();
                    }
                    if (serverSocket != null) serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });
    }

    // Broadcast messages to all connected clients or a specific client
    public void broadcastMessage(String clientName, int color, String recipient, String message) {
        String formattedMessage = "MESSAGE:" + color + ":" + clientName + ":" + recipient + ":" + message;
        Iterator<MHand> iterator = clients.iterator();
        while (iterator.hasNext()) {
            MHand client = iterator.next();
            try {
                // Send message only to the specified recipient or to all if recipient is "All"
                if (recipient.equals("All") || client.getClientName().equals(recipient)) {
                    client.sendMessage(formattedMessage);
                }
            } catch (IOException e) {
                chatLog.append("Failed to send message to a client. Removing client.\n");
                iterator.remove(); // Removes the client if the message fails to send
            }
        }
        chatLog.append(clientName + ": " + message + "\n");
    }

    // Update clients with the current list of connected clients
    public void updateClientList() {
        StringBuilder clientList = new StringBuilder("CLIENT_LIST:");
        for (MHand client : clients) {
            clientList.append(client.getClientName()).append(",");
        }
        String clientListMessage = clientList.toString();
        for (MHand client : clients) {
            try {
                client.sendMessage(clientListMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Remove a client from the list
    public void removeClient(MHand client) {
        clients.remove(client);
    }

    public static void main(String[] args) {
        new CServer();
    }
}
