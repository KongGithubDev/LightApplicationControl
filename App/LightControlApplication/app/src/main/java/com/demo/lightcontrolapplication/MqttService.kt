package com.demo.lightcontrolapplication

import android.content.Context
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import java.util.*

class MqttService(private val context: Context) {
    private val mqttServer = "broker.hivemq.com"
    private val mqttPort = 1883
    private val topic = "esp32/light_control"
    private var mqttClient: MqttClient? = null
    private var messageCallback: ((String) -> Unit)? = null
    private var connectionCallback: (() -> Unit)? = null

    private fun generateUniqueClientId(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random().nextInt(10000)
        return "AndroidClient_${timestamp}_${random}"
    }

    fun connect() {
        try {
            val persistence = MemoryPersistence()
            val clientId = generateUniqueClientId()
            mqttClient = MqttClient("tcp://$mqttServer:$mqttPort", clientId, persistence)
            val options = MqttConnectOptions().apply {
                isCleanSession = true
            }
            mqttClient?.connect(options)
            setupCallback()
            subscribe()
            connectionCallback?.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupCallback() {
        mqttClient?.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                // Handle connection lost
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                message?.let {
                    val payload = String(it.payload)
                    messageCallback?.invoke(payload)
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // Handle delivery complete
            }
        })
    }

    private fun subscribe() {
        try {
            mqttClient?.subscribe(topic)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun subscribe(callback: (String) -> Unit) {
        messageCallback = callback
    }

    fun publishMessage(message: String) {
        try {
            if (mqttClient?.isConnected == true) {
                val mqttMessage = MqttMessage(message.toByteArray())
                mqttClient?.publish(topic, mqttMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            mqttClient?.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onConnected(callback: () -> Unit) {
        connectionCallback = callback
    }
} 