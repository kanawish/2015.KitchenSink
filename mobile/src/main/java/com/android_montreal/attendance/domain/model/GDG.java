package com.android_montreal.attendance.domain.model;

/**
 * Created by kanawish on 2015-05-31.
 */
public class GDG {

   String name ;
   String city ;
   float longitude ;
   float latitude ;

   public GDG(String name, String city, float longitude, float latitude) {
      this.name = name;
      this.city = city;
      this.longitude = longitude;
      this.latitude = latitude;
   }
}
