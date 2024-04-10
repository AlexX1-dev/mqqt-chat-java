package de.tmexner.mqtt.chat;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTController implements MqttCallback {
  private final String username;

  private final ChatClientGUI gui;

  private final String broker;

  private final String topic;

  public MQTTController(String ip, int port, String username, ChatClientGUI gui) {
    this.username = username;
    this.broker = "tcp://" + ip + ":" + port;
    this.topic = "mqtt-chat/" + username + "/#";
    this.gui = gui;
  }

  public void subscribe() throws MqttException {
    MemoryPersistence persistence = new MemoryPersistence();
    MqttClient client = new MqttClient(broker, username, persistence);
    MqttConnectOptions connOpts = new MqttConnectOptions();
    connOpts.setCleanSession(true);
    client.connect(connOpts);
    client.setCallback(this);
    client.subscribe(topic);

  }

  @Override
  public void connectionLost(Throwable throwable) {

  }

  @Override
  public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
    String message = mqttMessage.getPayload().toString();
    gui.addMessageToHistory(s, message);

  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

  }

  public void sendMQTTMessage(String topic, String message) {

  }
}
