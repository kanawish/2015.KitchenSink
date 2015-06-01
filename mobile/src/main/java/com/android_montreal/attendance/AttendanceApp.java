package com.android_montreal.attendance;

import android.app.Application;
import android.content.Context;

import com.android_montreal.attendance.module.ApplicationModule;

import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Created by kanawish on 15-05-11.
 */
public class AttendanceApp extends Application {
   private AttendanceComponent component ;

   @Override
   @DebugLog
   public void onCreate() {
      super.onCreate();

      if (BuildConfig.DEBUG) {
         Timber.plant(new Timber.DebugTree());
      } else {
         // TODO Crashlytics.start(this);
         // TODO Timber.plant(new CrashlyticsTree());
      }

      buildComponentAndInject();
   }

   // Following the Dagger U2021 example, set this one aside to debug with Hugo.
   @DebugLog
   private void buildComponentAndInject() {
      component = BuildSpecificComponent.Initializer.init(this);
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
