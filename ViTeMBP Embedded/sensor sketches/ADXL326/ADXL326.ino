#include <EEPROM.h>

int led = 0;
int xValue;
int yValue;
int zValue;
byte lastReading[6];
byte eepromGuid[16];

void setup() {
  Serial.setTimeout(1);
  Serial.begin(115200);

  // use the external refrence returned from the sensor for more accuracy
  analogReference(EXTERNAL);

  // set analog pins to inputs
  pinMode(A0, INPUT);
  pinMode(A1, INPUT);
  pinMode(A2, INPUT);

  // set LED pin to output mode
  pinMode(LED_BUILTIN, OUTPUT);

  // wait until serial port opens for native USB devices
  while (!Serial) {
    delay(1);
  }
}

void readSensor() {
  xValue = analogRead(A0);
  yValue = analogRead(A1);
  zValue = analogRead(A2);

  lastReading[0] = xValue >> 8;
  lastReading[1] = xValue & 0xFF;
  lastReading[2] = yValue >> 8;
  lastReading[3] = yValue & 0xFF;
  lastReading[4] = zValue >> 8;
  lastReading[5] = zValue & 0xFF;
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
        readSensor();
        Serial.write(lastReading, 6);
        setLED();
        break;
      case 'i':
        Serial.print("f06ee9e1-345a-490d-8b03-a736a5e5d7bf");
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

