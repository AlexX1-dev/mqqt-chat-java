package de.tmexner.mqtt.chat;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.eclipse.paho.client.mqttv3.MqttException;

import de.tmexner.mqtt.chat.object.Chat;

public class ChatClientGUI extends JFrame {

  private DefaultListModel<String> chatListModel;

  private JList<String> chatList;
  private JTextArea chatDisplayArea;

  private Map<String, Chat> chats = new HashMap<String, Chat>();

  private JTextField messageInputField;
  private JTextField usernameField;
  private JTextField mqttBrokerIpField;
  private JTextField mqttBrokerPortField;

  private JLabel mqttBrokerIpLabel;
  private JLabel mqttBrokerPortLabel;
  private JLabel usernameLabel = new JLabel("Username: ");

  private JButton connectButton;

  private JPanel usernamePanel;

  public ChatClientGUI() {
    createGUIWindow();
    createUIComponents();

    setupConnectButton();

    addItemsToGUI();

    createChatList();
    createChat();
    initializeChats();
  }

  private void createChat() {
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
  }

  private void createChatList() {
    chatListModel = new DefaultListModel<>();
    chatList = new JList<>(chatListModel);
    chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    chatList.addListSelectionListener(e -> chatSelected());
    JScrollPane chatListScrollPane = new JScrollPane(chatList);
    add(chatListScrollPane, BorderLayout.WEST);
  }

  private void addItemsToGUI() {
    usernamePanel.add(usernameLabel);
    usernamePanel.add(usernameField);
    usernamePanel.add(mqttBrokerIpLabel);
    usernamePanel.add(mqttBrokerIpField);
    usernamePanel.add(mqttBrokerPortLabel);
    usernamePanel.add(mqttBrokerPortField);
    usernamePanel.add(connectButton);
    add(usernamePanel, BorderLayout.NORTH);
  }

  private void setupConnectButton() {
    connectButton = new JButton("Connect");
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
  }

  private void initializeChats() {
    chatListModel.addElement("John_Doe");
    chats.put("John Doe", new Chat("John_Doe"));
    chatListModel.addElement("Jane_Doe");
    chats.put("Jane Doe", new Chat("Jane_Doe"));
  }

  private void createGUIWindow() {
    setTitle("MQTT Chat Client");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(600, 400);
    setLocationRelativeTo(null);
    setVisible(true);
  }

  private void createUIComponents() {
    usernamePanel = new JPanel();
    usernamePanel.setLayout(new FlowLayout());
    usernameField = new JTextField(10);
    mqttBrokerIpField = new JTextField(10);
    mqttBrokerPortField = new JTextField(10);
    mqttBrokerIpLabel = new JLabel("MQTT Broker IP: ");
    mqttBrokerPortLabel = new JLabel("MQTT Broker Port: ");
  }

  private void chatSelected() {
    Chat selectedChat = chats.get(chatList.getSelectedValue());
    chatDisplayArea.setText("");
    chatDisplayArea.append("Chat history for " + selectedChat + "\n");

    List<String> history = selectedChat.getMessages();
    if (history != null) {
      for (String message : history) {
        chatDisplayArea.append(message + "\n");
      }
    }
  }

  private void sendMessage() {
    Chat selectedChat = chats.get(chatList.getSelectedValue());
    if (selectedChat == null) {
      JOptionPane.showMessageDialog(this, "No chat selected.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    String message = messageInputField.getText().trim();
    if (!message.isEmpty()) {
      String formattedMessage = "Me: " + message;
      chatDisplayArea.append(formattedMessage + "\n");
      messageInputField.setText("");
      System.out.println("Sending message using topic " + getTopicForSelectedChat());

      selectedChat.addMessage(formattedMessage);

      sendMQTTMessage(selectedChat.getUser(), message);
    }
  }

  private void sendMQTTMessage(String topic, String message) {

  }

  public void receiveMQTTMessage(String topic, String message) {
    if (topic.equals(getTopicForSelectedChat())) {
      String formattedMessage = getUsername() + ": " + message;
      chatDisplayArea.append(formattedMessage + "\n");
      Chat chat = chats.get(getSelectedChat());
      // Save the message to the chat history
      chat.addMessage(formattedMessage);
    }

  }

  public String getUsername() {
    return usernameField.getText().trim();
  }

  public String getSelectedChat() {
    return chatList.getSelectedValue();
  }

  public String getTopicForSelectedChat() {
    return "messenger/" + getUsername() + "/" + getSelectedChat();
  }

  public String getMqttBrokerIp() {
    return mqttBrokerIpField.getText().trim();
  }

  public int getMqttBrokerPort() {
    return Integer.parseInt(mqttBrokerPortField.getText().trim());
  }
}
