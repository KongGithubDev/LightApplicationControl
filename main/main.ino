#include <WiFi.h>
#include <PubSubClient.h>

const char* ssid = "Kong_Wifi";
const char* password = "Password";

const char* mqttServer = "broker.hivemq.com";
const int mqttPort = 1883;
const char* topic = "esp32/light_control";

WiFiClient espClient;
PubSubClient client(espClient);

bool lightState = false;
bool needsPublish = false;
String pendingMessage = "";

void setup() {
  Serial.begin(115200);
  Serial.println("\nStarting...");
  
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi connected!");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  
  client.setServer(mqttServer, mqttPort);
  client.setCallback(callback);
  
  mqttConnect();
  
  sendStatus();
}

void mqttConnect() {
  String clientId = "ESP32-" + String(random(0xffff), HEX);
  
  Serial.println("\nConnecting to MQTT Broker...");
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    
    if (client.connect(clientId.c_str())) {
      Serial.println("connected!");
      if (client.subscribe(topic)) {
        Serial.print("Subscribed to: ");
        Serial.println(topic);
      } else {
        Serial.println("Subscription failed!");
      }
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" retrying in 2 seconds...");
      delay(2000);
    }
  }
}

void sendStatus() {
  if (client.connected()) {
    String status = lightState ? "STATUS_ON" : "STATUS_OFF";
    client.publish(topic, status.c_str());
    Serial.print("Sent status: ");
    Serial.println(status);
  }
}

void callback(char* topic, byte* payload, unsigned int length) {
  String message = "";
  for (int i = 0; i < length; i++) {
    message += (char)payload[i];
  }
  message.trim();
  
  String cmd = message;
  cmd.toUpperCase();
  
  if (cmd == "ON") {
    lightState = true;
    Serial.println(">> LIGHT ON COMMAND RECEIVED <<");
    sendStatus();
  } else if (cmd == "OFF") {
    lightState = false;
    Serial.println(">> LIGHT OFF COMMAND RECEIVED <<");
    sendStatus();
  } else if (cmd == "STATUS") {
    Serial.println(">> STATUS REQUEST RECEIVED <<");
    sendStatus();
  }
}

void loop() {
  if (!client.connected()) {
    Serial.println("MQTT connection lost! Reconnecting...");
    mqttConnect();
    sendStatus();
  }
  
  client.loop();
  delay(10);
}