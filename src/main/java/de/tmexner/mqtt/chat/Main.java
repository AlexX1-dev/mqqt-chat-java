package de.tmexner.mqtt.chat;

import javax.swing.SwingUtilities;

public class Main {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      ChatClientGUI gui = new ChatClientGUI();
      gui.setVisible(true);
      gui.requestFocus();
    });
  }
}
