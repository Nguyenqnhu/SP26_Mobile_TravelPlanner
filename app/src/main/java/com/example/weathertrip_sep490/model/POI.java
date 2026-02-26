package com.example.weathertrip_sep490.model;

public class POI {

        private String name;
        private String location;
        private int imageResId;

        public POI(String name, String location, int imageResId) {
            this.name = name;
            this.location = location;
            this.imageResId = imageResId;
        }

        public String getName() {
            return name;
        }

        public String getLocation() {
            return location;
        }

        public int getImageResId() {
            return imageResId;
        }
    }

