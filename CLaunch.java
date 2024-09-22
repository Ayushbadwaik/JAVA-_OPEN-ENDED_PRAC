import java.awt.*;
import javax.swing.*;

public class CLaunch {

    public static void main(String[] args) {
        // Create the main launcher frame
        JFrame frame = new JFrame("Chat Application Launcher");
        frame.setSize(300, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Load the image and set it as the background
        ImageIcon backgroundIcon = new ImageIcon("C://Users//ayush//OneDrive//Desktop//logo.jpg"); 
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        backgroundLabel.setLayout(new BorderLayout()); 

        JLabel logoLabel = new JLabel("@ AYUSH");
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setForeground(Color.WHITE); 
        backgroundLabel.add(logoLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // Make the panel transparent
        JButton serverButton = new JButton("Start Server");
        JButton clientButton = new JButton("Start Client");

        serverButton.addActionListener(e -> startServer());
        clientButton.addActionListener(e -> startClient());

        buttonPanel.add(serverButton);
        buttonPanel.add(clientButton);
        backgroundLabel.add(buttonPanel, BorderLayout.SOUTH);

        frame.setContentPane(backgroundLabel);
        frame.setVisible(true);
    }

    // Method to start the server
    private static void startServer() {
        SwingUtilities.invokeLater(CServer::new); // Start CServer GUI
    }

    // Method to start the client
    private static void startClient() {
        SwingUtilities.invokeLater(Client::new); // Start Client GUI
    }
}
