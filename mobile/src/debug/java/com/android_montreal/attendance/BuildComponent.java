package com.android_montreal.attendance;

import com.android_montreal.attendance.module.ApplicationModule;
import com.android_montreal.attendance.module.DomainModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by kanawish on 15-05-11.
 */
@Singleton
@Component(modules = {ApplicationModule.class, DomainModule.class})
public interface BuildComponent {
   void inject(AttendanceApp app);
   void inject(AttendanceActivity activity);
   void inject(AttendanceActivityFragment fragment);
}
