import java.awt.*;
import java.awt.event.*;
import java.net.Socket;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.HashMap;

public class Client {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private JTextPane chatLog;
    private TextField messageField;
    private Button sendButton;
    private JComboBox<String> recipientDropdown;
    private String clientName;
    private Color clientColor;
    private HashMap<String, Color> userColors = new HashMap<>();
    private static final int PORT = 8000;

    public Client() {
        Frame frame = new Frame("Chat Client");
        frame.setSize(500, 500);
        frame.setLayout(new BorderLayout());
        frame.setBackground(Color.MAGENTA);

        chatLog = new JTextPane();
        chatLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatLog);
        frame.add(scrollPane, BorderLayout.CENTER);

        Panel bottomPanel = new Panel();
        bottomPanel.setLayout(new BorderLayout());

        messageField = new TextField(30);
        sendButton = new Button("Send");
        sendButton.setBackground(Color.GREEN);
        sendButton.setForeground(Color.YELLOW);

        // Add dropdown for recipient selection
        recipientDropdown = new JComboBox<>();
        recipientDropdown.addItem("All"); // Default option to send to all clients

        ActionListener sendAction = e -> {
            try {
                sendMessage(messageField.getText());
                messageField.setText("");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };

        messageField.addActionListener(sendAction);
        sendButton.addActionListener(sendAction);

        bottomPanel.add(recipientDropdown, BorderLayout.WEST);
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        try {
            String ip = JOptionPane.showInputDialog(frame, "Enter Server IP (e.g., 192.168.1.100):", "localhost");
            clientName = JOptionPane.showInputDialog(frame, "Enter your name:");

            socket = new Socket(ip, PORT);
            appendMessage("Connected to server at " + ip + ":" + PORT + ".\nDesigned & Developed by AYUSH B.\n", Color.BLACK);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Send client name to the server
            writer.write(clientName + "\n");
            writer.flush();

            // Start a new thread to handle incoming messages
            new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("COLOR:")) {
                            clientColor = new Color(Integer.parseInt(line.substring(6)));
                            userColors.put(clientName, clientColor); // Store client's own color
                        } else if (line.startsWith("MESSAGE:")) {
                            String[] parts = line.split(":", 5);
                            int color = Integer.parseInt(parts[1]);
                            String senderName = parts[2];
                            String recipient = parts[3];
                            String message = parts[4];

                            // Assign or retrieve color for the sender
                            Color messageColor = userColors.getOrDefault(senderName, new Color(color));
                            userColors.put(senderName, messageColor);

                            // Append message with sender's color if the message is meant for this client or all
                            if (recipient.equals("All") || recipient.equals(clientName)) {
                                appendMessage(senderName + ": " + message + "\n", messageColor);
                            }
                        } else if (line.startsWith("CLIENT_LIST:")) {
                            // Update dropdown with current client list
                            String[] clients = line.substring(12).split(",");
                            recipientDropdown.removeAllItems();
                            recipientDropdown.addItem("All");
                            for (String client : clients) {
                                recipientDropdown.addItem(client);
                            }
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                try {
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });
    }

    private void sendMessage(String message) throws Exception {
        String recipient = (String) recipientDropdown.getSelectedItem();
        writer.write("MESSAGE:" + clientName + ":" + recipient + ":" + message + "\n");
        writer.flush();
    }

    private void appendMessage(String message, Color color) {
        StyledDocument doc = chatLog.getStyledDocument();
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, color);
        StyleConstants.setFontFamily(attr, "Cooper Black");
        StyleConstants.setFontSize(attr, 14);
        try {
            doc.insertString(doc.getLength(), message, attr);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client();
    }
}
