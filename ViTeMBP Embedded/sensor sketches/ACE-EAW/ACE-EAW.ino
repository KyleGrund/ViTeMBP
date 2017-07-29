#include <EEPROM.h>

// the state of the status led
int led = 0;

// the last value read from the encoder
byte lastReading;

int loopCounter = 0;

// holds the serial number guid read from the eeprom
byte eepromGuid[16];

// the look-up table which returns encoder index value
int dataTable[256];

void setup() {
  Serial.setTimeout(1);
  Serial.begin(115200);

  pinMode(LED_BUILTIN, OUTPUT);

  // encoder data lines 0-7
  pinMode(A0, INPUT_PULLUP);
  pinMode(A1, INPUT_PULLUP);
  pinMode(A2, INPUT_PULLUP);
  pinMode(A3, INPUT_PULLUP);
  pinMode(A4, INPUT_PULLUP);
  pinMode(A5, INPUT_PULLUP);
  pinMode(5, INPUT_PULLUP);
  pinMode(6, INPUT_PULLUP);

  // the sensor has positions 0 to 127 which are encoded in 8 bits,
  // because of this there are many un-used values so they are defined
  // here at startup.
  dataTable[127] = 0;
  dataTable[63] = 1;
  dataTable[62] = 2;
  dataTable[58] = 3;
  dataTable[56] = 4;
  dataTable[184] = 5;
  dataTable[152] = 6;
  dataTable[24] = 7;

  dataTable[8] = 8;
  dataTable[72] = 9;
  dataTable[73] = 10;
  dataTable[77] = 11;
  dataTable[79] = 12;
  dataTable[15] = 13;
  dataTable[47] = 14;
  dataTable[175] = 15;
  
  dataTable[191] = 16;
  dataTable[159] = 17;
  dataTable[31] = 18;
  dataTable[29] = 19;
  dataTable[28] = 20;
  dataTable[92] = 21;
  dataTable[76] = 22;
  dataTable[12] = 23;
  
  dataTable[4] = 24;
  dataTable[36] = 25;
  dataTable[164] = 26;
  dataTable[166] = 27;
  dataTable[167] = 28;
  dataTable[135] = 29;
  dataTable[151] = 30;
  dataTable[215] = 31;
  
  dataTable[223] = 32;
  dataTable[207] = 33;
  dataTable[143] = 34;
  dataTable[142] = 35;
  dataTable[14] = 36;
  dataTable[46] = 37;
  dataTable[38] = 38;
  dataTable[6] = 39;
  
  dataTable[2] = 40;
  dataTable[18] = 41;
  dataTable[82] = 42;
  dataTable[83] = 43;
  dataTable[211] = 44;
  dataTable[195] = 45;
  dataTable[203] = 46;
  dataTable[235] = 47;
  
  dataTable[239] = 48;
  dataTable[231] = 49;
  dataTable[199] = 50;
  dataTable[71] = 51;
  dataTable[7] = 52;
  dataTable[23] = 53;
  dataTable[19] = 54;
  dataTable[3] = 55;
  
  dataTable[1] = 56;
  dataTable[9] = 57;
  dataTable[41] = 58;
  dataTable[169] = 59;
  dataTable[233] = 60;
  dataTable[225] = 61;
  dataTable[229] = 62;
  dataTable[245] = 63;
  
  dataTable[247] = 64;
  dataTable[243] = 65;
  dataTable[227] = 66;
  dataTable[163] = 67;
  dataTable[131] = 68;
  dataTable[139] = 69;
  dataTable[137] = 70;
  dataTable[129] = 71;
  
  dataTable[128] = 72;
  dataTable[132] = 73;
  dataTable[148] = 74;
  dataTable[212] = 75;
  dataTable[244] = 76;
  dataTable[240] = 77;
  dataTable[242] = 78;
  dataTable[250] = 79;
  
  dataTable[251] = 80;
  dataTable[249] = 81;
  dataTable[241] = 82;
  dataTable[209] = 83;
  dataTable[193] = 84;
  dataTable[197] = 85;
  dataTable[196] = 86;
  dataTable[192] = 87;
  
  dataTable[64] = 88;
  dataTable[66] = 89;
  dataTable[74] = 90;
  dataTable[106] = 91;
  dataTable[122] = 92;
  dataTable[120] = 93;
  dataTable[121] = 94;
  dataTable[125] = 95;
  
  dataTable[253] = 96;
  dataTable[252] = 97;
  dataTable[248] = 98;
  dataTable[232] = 99;
  dataTable[224] = 100;
  dataTable[226] = 101;
  dataTable[98] = 102;
  dataTable[96] = 103;
  
  dataTable[32] = 104;
  dataTable[33] = 105;
  dataTable[37] = 106;
  dataTable[53] = 107;
  dataTable[61] = 108;
  dataTable[60] = 109;
  dataTable[188] = 110;
  dataTable[190] = 111;
  
  dataTable[254] = 112;
  dataTable[126] = 113;
  dataTable[124] = 114;
  dataTable[116] = 115;
  dataTable[112] = 116;
  dataTable[113] = 117;
  dataTable[49] = 118;
  dataTable[48] = 119;
  
  dataTable[16] = 120;
  dataTable[144] = 121;
   dataTable[146] = 122;
  dataTable[154] = 123;
  dataTable[158] = 124;
  dataTable[30] = 125;
  dataTable[94] = 126;
  dataTable[95] = 127;

  // wait until USB is connected
  while (!Serial) {
    delay(1);
  }
}

void loop() {
  // process any serial events
  while (Serial.available()) {
    switch (Serial.read()) {
      case 'r':
        // the r command returns a reading from the encoder
        lastReading =
          digitalRead(A0) |
          digitalRead(A1) << 1 |
          digitalRead(A2) << 2 |
          digitalRead(A3) << 3 |
          digitalRead(A4) << 4 |
          digitalRead(A5) << 5 |
          digitalRead(5) << 6 |
          digitalRead(6) << 7;
        // return the decoded value
        Serial.write(dataTable[lastReading]);

        // toggle the led
        led = !led;
        digitalWrite(LED_BUILTIN, led);
        break;
      case 'i':
        // returns the identification GUID
        Serial.print("75d05ba8-639c-46e6-a940-591d920a2d86");
        break;
      case 's':
        // s command returns the serial number of the sensor
        // read in the serial number from the eeprom
        for (int i = 0; i < 16; i++) {
          eepromGuid[i] = EEPROM.read(i);
        }
        // return the value to the host
        Serial.write(eepromGuid, 16);
        break;
      case 'e':
        // the e command programs the serial number in the eeprom
        // set the timeout to 1/10th of a second to wait longer for new data from slower hosts
        Serial.setTimeout(100);
        // read in and write 16 bytes to the eeprom
        for (int i = 0; i < 16; i++) {
          EEPROM.write(i, Serial.read());
        }
        // set the timeout back to original value
        Serial.setTimeout(1);
        break;
    }
  }
}

