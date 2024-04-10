package de.tmexner.mqtt.chat.object;

import java.util.ArrayList;
import java.util.List;

public class Chat {
  // User is also the topic
  private String user;
  private ArrayList<String> messages;

  public Chat(String user) {
    this.user = user;
    this.messages = new ArrayList<>();
  }

  public void addMessage(String message) {
    this.messages.add(message);
  }

  public List<String> getMessages() {
    return this.messages;
  }

  public String getUser() {
    return this.user;
  }
}
