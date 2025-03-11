package com.callumwong.jishintray;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class P2PQuakeClientTest {
    public static void main (String[] args) {
        WebSocketServer server = new WebSocketServer(new InetSocketAddress(8080)) {
            @Override
            public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
                System.out.println("new connection to " + webSocket.getRemoteSocketAddress());
            }

            @Override
            public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
                System.out.println("closed " + webSocket.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
            }

            @Override
            public void onMessage(WebSocket webSocket, String s) {}

            @Override
            public void onError(WebSocket webSocket, Exception e) {
                System.err.println("an error occurred on connection " + webSocket.getRemoteSocketAddress()  + ":" + e);
            }

            @Override
            public void onStart() {
                System.out.println("test server started successfully");
            }
        };

        server.start();

        P2PQuakeClient client = new P2PQuakeClient(URI.create("ws://localhost:8080"));
        client.connect();

        client.onMessage("""
                {
                    "id": "5ee1ad7e02add676dd5a67a0",
                    "time": "2020/06/11 13:05:18.249",
                    "code": 552,
                    "cancelled": false,
                    "issue": {
                      "source": "気象庁",
                      "time": "2019/06/18 22:24:00",
                      "type": "Focus"
                    },
                    "areas": [
                      {
                        "grade": "Warning",
                        "immediate": true,
                        "name": "福島県",
                        "firstHeight": {
                          "condition": "津波到達中と推測"
                        },
                        "maxHeight": {
                          "description": "３ｍ",
                          "value": 3
                        }
                      },
                      {
                        "grade": "Watch",
                        "immediate": false,
                        "name": "青森県太平洋沿岸",
                        "firstHeight": {
                          "arrivalTime": "2019/06/18 22:40:00"
                        },
                        "maxHeight": {
                          "description": "１ｍ",
                          "value": 1
                        }
                      }
                    ]
                  }
                """);
    }
}