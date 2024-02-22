package de.tmexner.mqtt.chat.java;

import org.eclipse.paho.client.mqttv3.MqttException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ChatClientGUI extends JFrame {


    private final DefaultListModel<String> chatListModel;
    private final JList<String> chatList;
    private final JTextArea chatDisplayArea;
    private final JTextField messageInputField;
    private final JTextField usernameField;
    private final JTextField mqttBrokerIpField;
    private final JTextField mqttBrokerPortField;
    private final Map<String, List<String>> chatHistories = new HashMap<>();


    public ChatClientGUI() {
        setTitle("MQTT Chat Client");
        setSize(800, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel usernamePanel = new JPanel();
        usernamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel usernameLabel = new JLabel("Username: ");
        usernameField = new JTextField(10);

        JLabel mqttBrokerIpLabel = new JLabel("Broker IP: ");
        mqttBrokerIpField = new JTextField(10);

        JLabel mqttBrokerPortLabel = new JLabel("Broker Port: ");
        mqttBrokerPortField = new JTextField(5);

        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> {
            String username = getUsername();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a username.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String mqttBrokerIp = getMqttBrokerIp();
            if (mqttBrokerIp.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a broker IP.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int mqttBrokerPort = getMqttBrokerPort();
            if (mqttBrokerPort <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter a valid broker port.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MQTTListener listener = new MQTTListener(mqttBrokerIp, mqttBrokerPort, username, this);
            try {
                listener.subscribe();
            } catch (MqttException ex) {
                throw new RuntimeException(ex);
            }
        });


        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);
        usernamePanel.add(mqttBrokerIpLabel);
        usernamePanel.add(mqttBrokerIpField);
        usernamePanel.add(mqttBrokerPortLabel);
        usernamePanel.add(mqttBrokerPortField);
        usernamePanel.add(connectButton);

        add(usernamePanel, BorderLayout.NORTH);


        chatListModel = new DefaultListModel<>();
        chatList = new JList<>(chatListModel);
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.addListSelectionListener(e -> chatSelected());
        JScrollPane chatListScrollPane = new JScrollPane(chatList);
        chatListScrollPane.setPreferredSize(new Dimension(200, 0));
        add(chatListScrollPane, BorderLayout.WEST);

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatDisplayArea = new JTextArea();
        chatDisplayArea.setEditable(false);
        JScrollPane chatDisplayScrollPane = new JScrollPane(chatDisplayArea);
        chatPanel.add(chatDisplayScrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        messageInputField = new JTextField();
        JButton sendMessageButton = new JButton("Send");
        sendMessageButton.addActionListener(e -> sendMessage());
        messagePanel.add(messageInputField, BorderLayout.CENTER);
        messagePanel.add(sendMessageButton, BorderLayout.EAST);

        chatPanel.add(messagePanel, BorderLayout.SOUTH);

        add(chatPanel, BorderLayout.CENTER);

        initializeChats();
    }

    private void initializeChats() {
        chatListModel.addElement("jane_doe");
        chatListModel.addElement("john_doe");
    }

    private void chatSelected() {
        String selectedChat = chatList.getSelectedValue();
        chatDisplayArea.setText("");
        chatDisplayArea.append("Chat history for " + selectedChat + "\n");

        List<String> history = chatHistories.get(selectedChat);
        if (history != null) {
            for (String message : history) {
                chatDisplayArea.append(message + "\n");
            }
        }
    }

    private void sendMessage() {
        String selectedChat = chatList.getSelectedValue();
        if (selectedChat == null || selectedChat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No chat selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = messageInputField.getText().trim();
        if (!message.isEmpty()) {
            String formattedMessage = "Me: " + message;
            chatDisplayArea.append(formattedMessage + "\n");
            messageInputField.setText("");
            System.out.println("Sending message using topic " + getTopicForSelectedChat());

            chatHistories.computeIfAbsent(selectedChat, k -> new ArrayList<>()).add(formattedMessage);

            sendMQTTMessage(selectedChat, message);
        }
    }

    private void sendMQTTMessage(String topic, String message) {

    }

    public void receiveMQTTMessage(String topic, String message) {
        if (topic.equals(getTopicForSelectedChat())) {
            String formattedMessage = getUsername() + ": " + message;
            chatDisplayArea.append(formattedMessage + "\n");

            // Save the message to the chat history
            chatHistories.computeIfAbsent(getSelectedChat(), k -> new ArrayList<>()).add(formattedMessage);
        }


    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getSelectedChat() {
        return chatList.getSelectedValue();
    }

    public String getTopicForSelectedChat() {
        return "messanger/" + getUsername() + "/" + getSelectedChat();
    }

    public String getMqttBrokerIp() {
        return mqttBrokerIpField.getText().trim();
    }

    public int getMqttBrokerPort() {
        return Integer.parseInt(mqttBrokerPortField.getText().trim());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientGUI().setVisible(true));
    }
}
