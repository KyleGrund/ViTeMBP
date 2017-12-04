#include "Adafruit_VL53L0X.h"
#include <EEPROM.h>

Adafruit_VL53L0X lox = Adafruit_VL53L0X();
int led = 0;
byte lastReading[2];
byte lastReadingL = 0;
VL53L0X_RangingMeasurementData_t measure;
int loopCounter = 0;
byte eepromGuid[16];

void setup() {
  Serial.setTimeout(1);
  Serial.begin(115200);

  pinMode(LED_BUILTIN, OUTPUT);

  // wait until serial port opens for native USB devices
  while (!Serial) {
    delay(1);
  }
  
  if (!lox.begin()) {
    Serial.println("Failed to boot VL53L0X");
    while(1);
  }

  lastReading[0] = 50;
  lastReading[1] = 51;

  if (lox.getSingleRangingMeasurement(&measure, false) == VL53L0X_ERROR_NONE) {
    lastReading[0] = (byte)measure.RangeMilliMeter;
    lastReading[1] = (byte)(measure.RangeMilliMeter >> 8);
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
        Serial.write(lastReading, 2);
        if (lox.getSingleRangingMeasurement(&measure, false) == VL53L0X_ERROR_NONE) {
          lastReading[0] = (byte)measure.RangeMilliMeter;
          lastReading[1] = (byte)(measure.RangeMilliMeter >> 8);
        }
        setLED();
        break;
      case 'i':
        Serial.print("3972d3a9-d55f-4e74-a61f-f2f8fe62f858");
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

