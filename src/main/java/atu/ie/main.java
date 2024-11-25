
#include <WiFi.h>
#include <WiFiClient.h>
#include <WebServer.h>
#include <ESPmDNS.h>
#include <ESP32Servo.h>
#include <Wire.h>
#include "rgb_lcd.h"
        #include "homepage.h"
        #include <Adafruit_PN532.h>
Servo myservo;
rgb_lcd lcd;
#define SDA_PIN 21
        #define SCL_PIN 22
Adafruit_PN532 nfc(SDA_PIN, SCL_PIN);
uint8_t validUID[] = { 0xDE, 0xAD, 0xBE, 0xEF };  // Replace with your card's UID
const int uidLength = 4;

const char* ssid = "Mattphone";
        const char* password = "xboxone69";

WebServer server(80);

// Variable to track the servo's position
int servoPosition = 0;
bool servoOpen = false;

String getservo() {
    if (servoPosition == 90) {
        return "On";  // Servo is at 90 degrees (on)
    } else {
        return "Off";  // Servo is at 0 degrees (off)
    }
}

void handleRoot() {
    String message = homePagePart1 + getservo();
    server.send(200, "text/html", message);  // Send the webpage
}

void handleTurnOn() {
    myservo.write(90);   // Move the servo to 90 degrees
    servoPosition = 90;  // Update the servo state
    lcd.clear();
    lcd.print("Servo On");
    delay(5000);
    myservo.write(0);   // Move the servo to 90 degrees
    servoPosition = 0;  // Update the servo state
    String message = homePagePart1 + getservo();
    server.send(200, "text/html", message);  // Return the updated webpage
}


void handleNotFound() {
    String message = "File Not Found\n\n";
    message += "URI: ";
    message += server.uri();
    message += "\nMethod: ";
    message += (server.method() == HTTP_GET) ? "GET" : "POST";
    message += "\nArguments: ";
    message += server.args();
    message += "\n";
    for (uint8_t i = 0; i < server.args(); i++) {
        message += " " + server.argName(i) + ": " + server.arg(i) + "\n";
    }
    server.send(404, "text/plain", message);
}

void setup() {
    Serial.begin(115200);
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println("Connected to WiFi");
    Serial.print("IP address: ");
    Serial.println(WiFi.localIP());

    if (MDNS.begin("esp32")) {
        Serial.println("MDNS responder started");
    }

    server.on("/", handleRoot);
    server.on("/turnon", handleTurnOn);  // Correctly define the route
    // Handle the turn-on action for the servo
    server.onNotFound(handleNotFound);

    server.begin();


    myservo.attach(18);  // Attach the servo to pin 18
    lcd.begin(16, 2);
    lcd.print("Access Needed");
    nfc.begin();
    uint32_t versiondata = nfc.getFirmwareVersion();
    if (!versiondata) {
        Serial.println("Didn't find PN53x board");
        while (1)
            ;
    }
    nfc.SAMConfig();
    Serial.println("PN532 initialized");
}

void loop() {
    server.handleClient();
    delay(2);  // Allow CPU to switch to other tasks

    uint8_t buffer[7];
    uint8_t uidLengthDetected;

    // Check for a new card
    if (nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A, buffer, &uidLengthDetected)) {
        Serial.print("Card detected with UID: ");
        for (uint8_t i = 0; i < uidLengthDetected; i++) {
            Serial.print(buffer[i], HEX);
            Serial.print(" ");
        }
        Serial.println();

        // Check if the detected card matches the predefined UID
        if (uidLengthDetected == uidLength && memcmp(buffer, validUID, uidLength) == 0) {
            if (!servoOpen) {
                openServo();
            } else {
                closeServo();
            }
        } else {
            lcd.clear();
            lcd.print("Access Denied");
            delay(2000);
            lcd.clear();
            lcd.print("Waiting for card");
        }
    }
}

void openServo() {
    servoOpen = true;
    myservo.write(90);  // Open servo
    lcd.clear();
    lcd.print("Access Granted");
    delay(3000);  // Keep the servo open for 3 seconds
}

void closeServo() {
    servoOpen = false;
    myservo.write(0);  // Close servo
    lcd.clear();
    lcd.print("Servo Closed");
    delay(2000);
    lcd.clear();
    lcd.print("Waiting for card");
}