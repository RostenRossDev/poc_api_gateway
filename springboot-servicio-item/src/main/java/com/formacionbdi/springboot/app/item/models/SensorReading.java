package com.formacionbdi.springboot.app.item.models;

import java.sql.Timestamp;

public class SensorReading {
        private Timestamp timestamp;
        private String sensorID;
        private Double temperature;
        private SensorReading.BaseUnit baseUnit;

        public enum BaseUnit {
                CELSIUS,
                FAHRENHEIT
        }

        public SensorReading() {
                timestamp = new Timestamp(System.currentTimeMillis());
        }

        public SensorReading(String sensorID, double temperature, BaseUnit baseUnit) {
                this();
                this.sensorID = sensorID;
                this.temperature = temperature;
                this.setBaseUnit(baseUnit);
        }

        public Timestamp getTimestamp() {
                return timestamp;
        }

        public void setTimestamp(Timestamp timestamp) {
                this.timestamp = timestamp;
        }

        public String getSensorID() {
                return sensorID;
        }

        public void setSensorID(String sensorID) {
                this.sensorID = sensorID;
        }

        public Double getTemperature() {
                return temperature;
        }

        public void setTemperature(Double temperature) {
                this.temperature = temperature;
        }

        public SensorReading.BaseUnit getBaseUnit() {
                return baseUnit;
        }

        public void setBaseUnit(SensorReading.BaseUnit baseUnit) {
                this.baseUnit = baseUnit;
        }

        @Override
        public String toString() {
                return "SensorReading [ "+timestamp + " " + sensorID + " " + String.format("%.1f", temperature) + " " + baseUnit + " ]";
        }
}