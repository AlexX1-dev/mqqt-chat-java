package de.tmexner.mqtt.chat;

import java.util.Map;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import de.tmexner.mqtt.chat.object.Chat;

public class Util {
   protected static void sendMessage(ChatClientGUI gui,MQTTController controller, Map<String, Chat> chats, JList<String> chatList, JTextArea chatDisplayArea, JTextField messageInputField) {
        if (controller == null) {
            JOptionPane.showMessageDialog(gui, "Not connected to MQTT broker.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Chat selectedChat = chats.get(chatList.getSelectedValue());
        if (selectedChat == null) {
            JOptionPane.showMessageDialog(gui, "No chat selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = messageInputField.getText().trim();
        if (!message.isEmpty()) {
            String formattedMessage = "Me: " + message;
            chatDisplayArea.append(formattedMessage + "\n");
            messageInputField.setText("");
            System.out.println("Sending message using topic " + gui.getTopicForSelectedChat());

            selectedChat.addMessage(formattedMessage);

            controller.sendMQTTMessage(selectedChat.getUser(), message);
        }
    } 
}
