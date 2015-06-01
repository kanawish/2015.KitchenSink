package com.android_montreal.attendance;

import com.android_montreal.attendance.module.ApplicationModule;
import com.android_montreal.attendance.module.DomainModule;
import com.android_montreal.attendance.module.MockDomainModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by kanawish on 15-05-11.
 */
@Singleton
@Component(modules = {ApplicationModule.class, MockDomainModule.class})
public interface BuildSpecificComponent extends AttendanceComponent {
   /**
    * An initializer that creates the graph from an application.
    */
   final static class Initializer {
      static AttendanceComponent init(AttendanceApp app) {
         return DaggerBuildSpecificComponent.builder()
            .applicationModule(new ApplicationModule(app.getApplicationContext()))
            .build();
      }
      private Initializer() {} // No instances.
   }

}
