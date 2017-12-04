#include <Wire.h>
#include "Adafruit_VL6180X.h"
#include <EEPROM.h>

Adafruit_VL6180X vl = Adafruit_VL6180X();
int led = 0;
byte lastReading[1];
byte lastReadingL = 0;
int loopCounter = 0;
byte eepromGuid[16];
uint8_t range;
uint8_t rangeStatus;

void setup() {
  Serial.setTimeout(1);
  Serial.begin(115200);

  pinMode(LED_BUILTIN, OUTPUT);

  // wait until serial port opens for native USB devices
  while (!Serial) {
    delay(1);
  }
  
  if (! vl.begin()) {
    Serial.println("Failed to find VL61080X");
    while (1);
  }

  lastReading[0] = 50;

  range = vl.readRange();
  rangeStatus = vl.readRangeStatus();
  if (rangeStatus == VL6180X_ERROR_NONE) {
    lastReading[0] = range;
  }

  pinMode(0, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(0), setLED, CHANGE);

}

void setLED() {
  //led = digitalRead(A1);
  led = !led;
  digitalWrite(LED_BUILTIN, led);
}

void loop() {
  while (Serial.available()) {
    switch (Serial.read()) {
      case 'r':
        Serial.write(lastReading, 1);
        range = vl.readRange();
        rangeStatus = vl.readRangeStatus();
        if (rangeStatus == VL6180X_ERROR_NONE) {
          lastReading[0] = range;
        }
        setLED();
        break;
      case 'i':
        Serial.print("416ffd9b-67ac-4cb3-9c04-30ba1f8640ca");
        break;
      case 's':
        for (int i = 0; i < 16; i++) {
          eepromGuid[i] = EEPROM.read(i);
        }
        Serial.write(eepromGuid, 16);
        break;
      case 'e':
        Serial.setTimeout(100);
        for (int i = 0; i < 16; i++) {
          EEPROM.write(i, Serial.read());
        }
        Serial.setTimeout(1);
        break;
    }
  }
}
