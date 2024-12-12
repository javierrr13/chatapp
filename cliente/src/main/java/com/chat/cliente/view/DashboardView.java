package com.chat.cliente.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import com.chat.cliente.controller.LoginController;
import com.chat.cliente.model.UserModel;
import com.chat.cliente.toaster.Toaster;
import com.chat.cliente.utils.UIUtils;
import com.chat.shared.Conversation;

public class DashboardView extends JFrame {

    private static final long serialVersionUID = 1L;
    private final Toaster toaster;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final UserModel userModel;

    private final JPanel conversationListPanel = new JPanel(new GridLayout(0, 1, 10, 10));
    private final Map<Integer, JTextArea> chatAreas = new HashMap<>();

    public DashboardView(UserModel userModel) throws IOException {
        super("Dashboard");
        this.userModel = userModel;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximiza la ventana al inicio
        setMinimumSize(new Dimension(800, 600)); // Tamaño mínimo
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createDashboardPanel(), "Dashboard");
        this.add(cardPanel);
        toaster = new Toaster(cardPanel);

        loadConversations();
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        // Sidebar
        JPanel sidebar = new JPanel(new GridBagLayout());
        sidebar.setBackground(UIUtils.COLOR_INTERACTIVE_DARKER);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel logo = new JLabel(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("lumo_placeholder.png"))));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        sidebar.add(logo, gbc);

        gbc.gridy = 1;
        sidebar.add(createSidebarButton("Perfil", e -> showUserProfileAction().accept(null)), gbc);
        gbc.gridy = 2;
        sidebar.add(createSidebarButton("Desconectarse", e -> logoutAction().accept(null)), gbc);
        gbc.gridy = 3;
        sidebar.add(createSidebarButton("Conversaciones", e -> openConversationsViewAction().accept(null)), gbc);

        // Content Area
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UIUtils.COLOR_BACKGROUND);

        JLabel title = new JLabel("Mis Conversaciones", SwingConstants.CENTER);
        title.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(24f));
        title.setForeground(UIUtils.COLOR_OUTLINE);
        contentArea.add(title, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(conversationListPanel);
        contentArea.add(scrollPane, BorderLayout.CENTER);

        dashboardPanel.add(sidebar, BorderLayout.WEST);
        dashboardPanel.add(contentArea, BorderLayout.CENTER);

        return dashboardPanel;
    }

    private JButton createSidebarButton(String text, Consumer<ActionEvent> action) {
        JButton button = new JButton(text);
        button.setBackground(UIUtils.COLOR_BACKGROUND);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(200, 40));

        button.addActionListener(e -> {
            if (action != null) {
                action.accept(e);
            } else {
                System.out.println("No action assigned for: " + text);
            }
        });
        return button;
    }

    private void loadConversations() {
        try {
            List<Conversation> conversations = userModel.getConversations();
            conversationListPanel.removeAll();

            for (Conversation conversation : conversations) {
                JButton conversationButton = new JButton(conversation.getName());
                conversationButton.setFont(new Font("Arial", Font.PLAIN, 14));
                conversationButton.addActionListener(e -> openChatView(conversation));
                conversationListPanel.add(conversationButton);
            }

            conversationListPanel.revalidate();
            conversationListPanel.repaint();
        } catch (IOException | ClassNotFoundException e) {
            toaster.error("Error cargando conversaciones.");
            e.printStackTrace();
        }
    }

    private void openChatView(Conversation conversation) {
        ChatView chatView = new ChatView(userModel, cardLayout, cardPanel, chatAreas, this);
        chatView.openChat(conversation, toaster);
        cardPanel.add(chatView.getChatPanel(), "ChatView");
        cardLayout.show(cardPanel, "ChatView");
    }

    public Consumer<Void> showUserProfileAction() {
        return (v) -> {
            UserProfileView userProfileView = new UserProfileView(userModel, 
                backAction -> cardLayout.show(cardPanel, "Dashboard"));
            userProfileView.loadUserProfile();
            cardPanel.add(userProfileView, "UserProfileView");
            cardLayout.show(cardPanel, "UserProfileView");
        };
    }

    public Consumer<Void> logoutAction() {
        return (v) -> {
            try {
                if (userModel.logout()) {
                    this.dispose();
                    new LoginView(LoginController.getInstance()).setVisible(true);
                }
            } catch (Exception e) {
                toaster.error("Error al cerrar sesión.");
                e.printStackTrace();
            }
        };
    }

    public Consumer<Void> openConversationsViewAction() {
        return (v) -> SwingUtilities.invokeLater(() -> new ConversationsView(userModel).setVisible(true));
    }
}
