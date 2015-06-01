package com.android_montreal.attendance.domain.model;

public class GDGBuilder {
   private String name;
   private String city;
   private float longitude;
   private float latitude;

   public GDGBuilder setName(String name) {
      this.name = name;
      return this;
   }

   public GDGBuilder setCity(String city) {
      this.city = city;
      return this;
   }

   public GDGBuilder setLongitude(float longitude) {
      this.longitude = longitude;
      return this;
   }

   public GDGBuilder setLatitude(float latitude) {
      this.latitude = latitude;
      return this;
   }

   public GDG createGDG() {
      return new GDG(name, city, longitude, latitude);
   }
}