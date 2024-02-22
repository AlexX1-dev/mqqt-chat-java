package de.tmexner.mqtt.chat.java;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTListener implements MqttCallback {
    private final String ip;
    private final int port;
    private final String username;

    private final String broker;

    private final String topic;
    public MQTTListener(String ip, int port, String username) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.broker = "tcp://" + ip + ":" + port;
        this.topic = "mqtt-chat/" + username + "/#";
    }

    void subscribe() throws MqttException {
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

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
