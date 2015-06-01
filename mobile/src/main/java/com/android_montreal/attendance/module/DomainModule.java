package com.android_montreal.attendance.module;

import android.content.Context;

import com.android_montreal.attendance.R;
import com.android_montreal.attendance.domain.FirebaseManager;
import com.android_montreal.attendance.domain.FirebaseManagerImpl;
import com.android_montreal.attendance.domain.GoogleAuthManager;
import com.android_montreal.attendance.domain.GoogleAuthManagerImpl;
import com.android_montreal.attendance.domain.LocalManager;
import com.android_montreal.attendance.domain.LocalManagerImpl;
import com.android_montreal.attendance.domain.MockFirebaseManagerImpl;
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
   LocalManager provideLocalManager() {
      return new LocalManagerImpl();
   }

   @Provides @Singleton
   FirebaseManager provideFirebaseManager(Context appContext) {
      /* Create the Firebase ref that is used for authentication with Firebase */
      Firebase.setAndroidContext(appContext);

      return new FirebaseManagerImpl(new Firebase(appContext.getResources().getString(R.string.firebase_url)));
   };


   @Provides @Singleton
   Firebase getFirebaseInstance(Context appContext){
      /* Create the Firebase ref that is used for all authentication with Firebase */
      Firebase.setAndroidContext(appContext);
      return new Firebase(appContext.getResources().getString(R.string.firebase_url));
   }

   @Provides @Singleton
   GoogleAuthManager provideGoogleAuthManager(GoogleAuthManagerImpl impl) {
      return impl;
   }

}
