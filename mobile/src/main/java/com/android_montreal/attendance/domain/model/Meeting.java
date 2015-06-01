package com.android_montreal.attendance.domain.model;

/**
 * Created by kanawish on 2015-05-31.
 */
public class Meeting {
   String title ;
   Long dateTimeGMT ;

   public Meeting(String title, Long dateTimeGMT) {
      this.title = title;
      this.dateTimeGMT = dateTimeGMT;
   }
}
