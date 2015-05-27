package com.android_montreal.attendance.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by kanawish on 15-05-11.
 *
 * A module to access application-level context.
 */
@Module
public class ApplicationModule {
   Context appContext ;

   public ApplicationModule(Context appContext) {
      this.appContext = appContext;
   }

   @Provides @Singleton
   Context provideAppContext() {
      return appContext;
   }
}
