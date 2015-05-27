package com.android_montreal.attendance;

import android.app.Application;
import android.content.Context;

import com.android_montreal.attendance.module.ApplicationModule;

import hugo.weaving.DebugLog;

/**
 * Created by kanawish on 15-05-11.
 */
public class AttendanceApp extends Application {
   private AttendanceComponent component ;

   @Override
   @DebugLog
   public void onCreate() {
      super.onCreate();


      component = DaggerAttendanceComponent.builder()
         .applicationModule(new ApplicationModule(this.getApplicationContext()))
         .build();

      component.inject(this);

   }

   /**
    * These are used by Activity and others in the system to bootstrap injection.
    */

   @DebugLog
   public AttendanceComponent component() {
      return component;
   }

   public static AttendanceApp get(Context context) {
      return (AttendanceApp) context.getApplicationContext();
   }

}
