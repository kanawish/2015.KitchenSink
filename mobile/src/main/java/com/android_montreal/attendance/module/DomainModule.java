package com.android_montreal.attendance.module;

import android.content.Context;

import com.android_montreal.attendance.R;
import com.android_montreal.attendance.domain.GoogleAuthManager;
import com.android_montreal.attendance.domain.GoogleAuthManagerImpl;
import com.android_montreal.attendance.domain.LocalInteractor;
import com.android_montreal.attendance.domain.LocalInteractorImpl;
import com.firebase.client.Firebase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by kanawish on 15-05-11.
 *
 * Starting module, make sure again to split into smaller module before it grows unwieldy.
 *
 */
@Module
public class DomainModule {

   @Provides @Singleton
   LocalInteractor provideLocalInteractor() {
      return new LocalInteractorImpl();
   }

   @Provides @Singleton
   public Firebase getFirebaseInstance(Context appContext){
      /* Create the Firebase ref that is used for all authentication with Firebase */
      Firebase.setAndroidContext(appContext);
      return new Firebase(appContext.getResources().getString(R.string.firebase_url));
   }

   @Provides @Singleton
   public GoogleAuthManager provideGoogleAuthManager(GoogleAuthManagerImpl impl) {
      return impl;
   }

}
