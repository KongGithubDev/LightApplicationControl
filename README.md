# Light Application Control (ESP32 + Android MQTT)

This project demonstrates controlling a light state via MQTT between an ESP32 (Arduino) device and an Android application.

- ESP32 firmware is in `main.ino`. It connects to Wi‑Fi, subscribes to `esp32/light_control` on HiveMQ public broker, and reacts to commands: `ON`, `OFF`, `STATUS`.
- Android app (Kotlin) is in `App/LightControlApplication/`. It uses Eclipse Paho MQTT to publish/subscribe on the same topic and display or control the state.

## Features
- MQTT communication over public broker (`broker.hivemq.com:1883`)
- Commands: `ON`, `OFF`, `STATUS`
- Status messages from ESP32: `STATUS_ON` or `STATUS_OFF`
- Auto-reconnect for ESP32 when MQTT connection is lost

## Repository Structure
- `main.ino` — ESP32 Arduino sketch
- `App/LightControlApplication/` — Android app module (Gradle/Kotlin)
- `LICENSE` — License for this repository
- `.gitignore` — Git ignore rules for Android + Arduino

## Prerequisites
### Hardware
- ESP32 development board
- USB cable

### Software
- Arduino IDE (or Arduino CLI)
- Android Studio (Arctic Fox or newer)

### Arduino Libraries
- `WiFi.h` (bundled with ESP32 board support)
- `PubSubClient` (by Nick O'Leary)

Install ESP32 board support in Arduino IDE via Boards Manager, and install PubSubClient via Library Manager.

## ESP32 Setup and Upload
1. Open `main.ino` in Arduino IDE.
2. Configure your Wi‑Fi:
   - Edit `ssid` and `password` in `main.ino` to match your network.
3. Select Board and Port:
   - Tools → Board → ESP32 Arduino → your ESP32 model
   - Tools → Port → your device port
4. Upload the sketch.
5. Open Serial Monitor at 115200 baud to see connection logs and status messages.

The sketch:
```cpp
// Topic used by both ESP32 and Android app
const char* topic = "esp32/light_control";
// Commands expected from Android: ON | OFF | STATUS
// Status published by ESP32: STATUS_ON | STATUS_OFF
```

## Android App Setup (Kotlin)
1. Open `App/LightControlApplication/` in Android Studio.
2. Let Gradle sync. Ensure internet permission exists in the app manifest if you add UI activities.
3. The MQTT service is implemented in `app/src/main/java/com/demo/lightcontrolapplication/MqttService.kt` using Eclipse Paho client.
4. Run the app on a device/emulator with internet access.

### MQTT Details
- Broker: `broker.hivemq.com`
- Port: `1883`
- Topic: `esp32/light_control`
- Client IDs: Randomized for both ESP32 and Android to avoid conflicts.

## Usage
- From the Android app, publish `ON` or `OFF` to toggle the ESP32 light state.
- Publish `STATUS` to request the current state.
- ESP32 publishes `STATUS_ON` or `STATUS_OFF` whenever state changes or upon status request.

## Security Notes
- Replace the default Wi‑Fi credentials in `main.ino` before committing code to any public repository.
- Public brokers are for testing only. For production, host your own MQTT broker with TLS and authentication.
- Do not commit secrets (keystores, API keys). `.gitignore` is configured to help avoid accidental commits.

## Troubleshooting
- If ESP32 fails to connect to Wi‑Fi: verify SSID/password and 2.4GHz availability.
- If MQTT connect fails: public brokers can rate‑limit; wait and retry or use your own broker.
- Ensure both ESP32 and Android use the exact same topic `esp32/light_control`.
- Check that corporate/VPN/firewall does not block port 1883.

## License
This project is licensed under the terms in `LICENSE`.
