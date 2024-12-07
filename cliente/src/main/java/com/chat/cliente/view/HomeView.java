package com.chat.cliente.view;

import javax.swing.*;

import com.chat.cliente.model.UserModel;
import com.chat.cliente.toaster.Toaster;
import com.chat.cliente.utils.StyledButton;
import com.chat.cliente.utils.UIUtils;

import java.awt.*;
import java.io.IOException;

public class HomeView extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Toaster toaster;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private final UserModel userModel;

    public HomeView(UserModel userModel) {
        super("Home");
        this.userModel = userModel;

        // Configuración de la ventana
        this.setUndecorated(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(1000, 600));

        // Configuración del CardLayout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Agregar los paneles de Home y Chat
        cardPanel.add(createHomePanel(), "Home");
        cardPanel.add(createChatPanel(), "Chat");

        this.add(cardPanel);
        this.pack();
        this.setResizable(false);
        centerWindow();

        toaster = new Toaster(cardPanel);
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
    }

    private JPanel createHomePanel() {
        JPanel homePanel = new JPanel();
        homePanel.setLayout(null);
        homePanel.setPreferredSize(new Dimension(1000, 600));
        homePanel.setBackground(UIUtils.COLOR_BACKGROUND);

        // Logo
        JLabel logo = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("lumo_placeholder.png")));
        logo.setBounds(50, 50, 200, 100);
        homePanel.add(logo);

        // Separador vertical
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setForeground(UIUtils.COLOR_OUTLINE);
        separator.setBounds(300, 50, 1, 500);
        homePanel.add(separator);

        // Lista de conversaciones
        JLabel conversationListLabel = new JLabel("Conversations", SwingConstants.LEFT);
        conversationListLabel.setForeground(Color.WHITE);
        conversationListLabel.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(24f));
        conversationListLabel.setBounds(350, 50, 400, 30);
        homePanel.add(conversationListLabel);

        // Scroll para la lista de conversaciones
        JPanel conversationList = new JPanel();
        conversationList.setBackground(UIUtils.COLOR_BACKGROUND);
        conversationList.setLayout(new BoxLayout(conversationList, BoxLayout.Y_AXIS));

        for (int i = 1; i <= 10; i++) {
            JPanel conversationCard = createConversationCard("User " + i, "Last message...");
            conversationList.add(conversationCard);
            conversationList.add(Box.createVerticalStrut(10));
        }

        JScrollPane scrollPane = new JScrollPane(conversationList);
        scrollPane.setBounds(350, 100, 400, 400);
        scrollPane.setBorder(null);
        homePanel.add(scrollPane);

        // Botón para cambiar a Chat
        JLabel goToChatButton = new StyledButton("Go to Chat", UIUtils.COLOR_INTERACTIVE, UIUtils.COLOR_INTERACTIVE_DARKER, this::showChatPanel);
        goToChatButton.setBounds(350, 520, 400, 50);
        homePanel.add(goToChatButton);

        return homePanel;
    }

    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(null);
        chatPanel.setPreferredSize(new Dimension(1000, 600));
        chatPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        // Logo
        JLabel logo = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("lumo_placeholder.png")));
        logo.setBounds(50, 50, 200, 100);
        chatPanel.add(logo);

        // Separador vertical
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setForeground(UIUtils.COLOR_OUTLINE);
        separator.setBounds(300, 50, 1, 500);
        chatPanel.add(separator);

        // Área de mensajes
        JLabel chatLabel = new JLabel("Chat", SwingConstants.LEFT);
        chatLabel.setForeground(Color.WHITE);
        chatLabel.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(24f));
        chatLabel.setBounds(350, 50, 400, 30);
        chatPanel.add(chatLabel);

        JPanel chatMessages = new JPanel();
        chatMessages.setBackground(UIUtils.COLOR_BACKGROUND);
        chatMessages.setLayout(new BoxLayout(chatMessages, BoxLayout.Y_AXIS));

        for (int i = 1; i <= 10; i++) {
            JLabel message = new JLabel((i % 2 == 0 ? "You: " : "User: ") + "Message " + i);
            message.setFont(UIUtils.FONT_GENERAL_UI);
            message.setForeground(i % 2 == 0 ? Color.GREEN : Color.WHITE);
            chatMessages.add(message);
        }

        JScrollPane chatScrollPane = new JScrollPane(chatMessages);
        chatScrollPane.setBounds(350, 100, 600, 400);
        chatScrollPane.setBorder(null);
        chatPanel.add(chatScrollPane);

        // Entrada de texto y botón de enviar
        JTextField chatInput = new JTextField();
        chatInput.setFont(UIUtils.FONT_GENERAL_UI);
        chatInput.setBounds(350, 520, 500, 40);
        chatPanel.add(chatInput);

        JLabel sendButton = new StyledButton("Send", UIUtils.COLOR_INTERACTIVE, UIUtils.COLOR_INTERACTIVE_DARKER, () -> {
            String text = chatInput.getText();
            if (!text.isEmpty()) {
                toaster.success("Message sent: " + text);
                chatInput.setText("");
            }
        });
        sendButton.setBounds(860, 520, 90, 40);
        chatPanel.add(sendButton);

        // Botón para regresar a Home
        JLabel backButton = new StyledButton("Back to Home", UIUtils.COLOR_BACKGROUND, UIUtils.COLOR_OUTLINE, this::showHomePanel);
        backButton.setBounds(350, 580, 600, 40);
        chatPanel.add(backButton);

        return chatPanel;
    }

    private JPanel createConversationCard(String userName, String lastMessage) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(UIUtils.COLOR_INTERACTIVE);
        card.setPreferredSize(new Dimension(380, 60));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel(userName, SwingConstants.LEFT);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(UIUtils.FONT_GENERAL_UI);

        JLabel messageLabel = new JLabel(lastMessage, SwingConstants.LEFT);
        messageLabel.setForeground(UIUtils.COLOR_OUTLINE);
        messageLabel.setFont(UIUtils.FONT_FORGOT_PASSWORD);

        card.add(nameLabel, BorderLayout.NORTH);
        card.add(messageLabel, BorderLayout.CENTER);
        return card;
    }

    private void showHomePanel() {
        cardLayout.show(cardPanel, "Home");
    }

    private void showChatPanel() {
        cardLayout.show(cardPanel, "Chat");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserModel userModel = null;
            try { 
                userModel = new UserModel("localhost", 12345);
            } catch (IOException e) {
                e.printStackTrace();
            }
            new HomeView(userModel).setVisible(true);
        });
    }
}
