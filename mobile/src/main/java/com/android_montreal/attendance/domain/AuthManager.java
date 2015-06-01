package com.android_montreal.attendance.domain;

import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import timber.log.Timber;

/**
 * Created by kanawish on 15-05-12.
 */
@Singleton
public class AuthManager {

   @Inject ErrorBus errorBus;

   private final Subject<Boolean,Boolean> stateBus;
   {
      BehaviorSubject<Boolean> subject = BehaviorSubject.create();
      stateBus = subject.toSerialized();
   }

   private final Subject<AuthData,AuthData> authDataBus;
   {
      BehaviorSubject<AuthData> subject = BehaviorSubject.create();
      authDataBus = subject.toSerialized();
   }


   // NOTE: If we end up with a lot of state attributes, consider bundling them in an AuthManagerState model class.
   // State, can be subscribed to.
   private boolean inProgress = false ;
   private AuthData authData;


   @Inject
   public AuthManager() {
   }

   void setInProgress(boolean inProgress) {
      this.inProgress = inProgress;
      stateBus.onNext(this.inProgress);
   }

   private void setAuthenticatedUser(AuthData authData) {
      this.authData = authData;
      authDataBus.onNext(this.authData);
   }

   public Observable<Boolean> behaviourInProgress() {
      return stateBus;
   }

   public Observable<AuthData> behaviourAuthData() {
      return authDataBus;
   }

   public AuthResultHandler createAuthResultHandler(String provider) {
      return new AuthResultHandler(provider);
   }

   /**
    * Utility class for authentication results
    */
   // TODO: Likely should split the firebase auth from the provider specific stuff.
   private class AuthResultHandler implements Firebase.AuthResultHandler {

      private final String provider;

      public AuthResultHandler(String provider) {
         this.provider = provider;
      }

      @Override
      public void onAuthenticated(AuthData authData) {
         // FIXME: Fire event on bus / publish data.
         Timber.i(provider + " auth successful");
         // authProgressDialog.hide();
         setInProgress(false);
         setAuthenticatedUser(authData);
      }

      @Override
      public void onAuthenticationError(FirebaseError firebaseError) {
         // FIXME: Fire event on bus / publish data.
         // authProgressDialog.hide();
         setInProgress(false);
         // showErrorDialog(firebaseError.toString());
         errorBus.send(firebaseError.toString());
      }
   }

}
