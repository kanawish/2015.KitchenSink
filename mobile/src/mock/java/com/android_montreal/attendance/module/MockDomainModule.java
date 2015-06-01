package com.android_montreal.attendance.module;

import com.android_montreal.attendance.domain.FirebaseManager;
import com.android_montreal.attendance.domain.GDGInfoProvider;
import com.android_montreal.attendance.domain.GoogleAuthManager;
import com.android_montreal.attendance.domain.GoogleAuthManagerImpl;
import com.android_montreal.attendance.domain.LocalManager;
import com.android_montreal.attendance.domain.LocalManagerImpl;
import com.android_montreal.attendance.domain.MockFirebaseManagerImpl;
import com.android_montreal.attendance.domain.MockGDGInfoProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by kanawish on 2015-05-31.
 */
@Module
public class MockDomainModule {
   @Provides @Singleton
   LocalManager provideLocalManager() {
      return new LocalManagerImpl();
   }

   @Provides @Singleton
   FirebaseManager provideFirebaseManager() {
      return new MockFirebaseManagerImpl();
   };

   @Provides @Singleton
   GoogleAuthManager provideGoogleAuthManager(GoogleAuthManagerImpl impl) {
      return impl;
   }

   @Provides @Singleton
   GDGInfoProvider provideGdgInfoProvider() {
      return new MockGDGInfoProvider();
   };

}