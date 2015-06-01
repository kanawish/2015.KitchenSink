package com.android_montreal.attendance.domain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import timber.log.Timber;

/**
 * Created by kanawish on 15-05-11.
 */
public class MockGoogleAuthManagerImpl implements GoogleAuthManager {

   @Inject
   FirebaseManager firebaseRef;

   @Inject
   ErrorBus errorBus;

   @Inject
   AuthManager authManager;

   Context appContext;


   // NOTE: This must always be set via setter, to keep the stateBus in a valid... state.
   private State state ;
   private Subject<State,State> stateBus ;
   {
      BehaviorSubject<State> subject = BehaviorSubject.create();
      stateBus = subject.toSerialized();
   }

   // NOTE: Inject needed items for initialization methods, since attrib injection happens after construction.
   @Inject
   public MockGoogleAuthManagerImpl(Context appContext) {
      this.appContext = appContext;

      this.setState(State.NONE);
   }

   @DebugLog
   private GoogleApiClient buildGoogleApiClient() {
      // Setup the Google API object to allow Google+ logins
      ConnectionHandler handler = new ConnectionHandler();
      return new GoogleApiClient.Builder(appContext)
         .addConnectionCallbacks(handler)
         .addOnConnectionFailedListener(handler)
         .addApi(Plus.API)
         .addScope(Plus.SCOPE_PLUS_LOGIN)
         .build();
   }

   @Override
   @DebugLog
   public void signin() {
      // Connect API now
      // TODO: Add a mock async connection attempt
      Timber.d("Trying to connect to Google API");
      setState(State.CONNECTING);
   }

   @Override
   @DebugLog
   public void signout() {
      // TODO: Mock the flow effects of a disconnect.
      setState(State.NONE);
   }

   @Override
   @DebugLog
   public void revoke() {
      // TODO: Mock the flow effects of a revoke.
      setState(State.NONE);
   }

   /**
    * Only can work if state is CONNECTED.
    *
    * TODO: Check up on the recovery if this is called while not connected.
    */
   @Override
   @DebugLog
   public void authentifyToFirebase() {
      // Signal we've started on the auth bus.
      authManager.setInProgress(true);

      // Simple auth mock
      Observable.just("MOCK-GOOGLE-TOKEN")
         .subscribeOn(Schedulers.newThread())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(new Action1<String>() {
            @Override
            public void call(String token) {
               // Successfully got OAuth token, now login to Firebase with Google's OAuth token.
               firebaseRef.authWithOAuthToken("google", token, authManager.createAuthResultHandler("google"));
            }
         });

      // TODO: Add artificial retry attempts?

   }


   private class ConnectionHandler implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
      @Override
      @DebugLog
      public void onConnected(Bundle bundle) {
         // NOTE: Is it possible for connected/suspended to be called due to network avail?
         // If so, we'll want to safeguard against calling the get token multiple times.
         setState(State.CONNECTED);
      }

      @Override
      @DebugLog
      public void onConnectionSuspended(int i) {
         // NOTE: I'm guessing this is called when we disconnect..?
         // The connection to Google Play services was lost for some reason.
         // We call connect() to attempt to re-establish the connection or get a
         // ConnectionResult that we can attempt to resolve.
         setState(State.NONE);
         // googleApiClient.connect();
      }

      @Override
      @DebugLog
      public void onConnectionFailed(ConnectionResult result) {
         setState(State.NONE);
         if (result.hasResolution()) {
            // Send recoverable issue on bus.
            send(new RecoverableIssueImpl(result));
         }
      }
   }


   public class RecoverableIssueImpl implements RecoverableIssue {
      boolean processed = false ;
      ConnectionResult connectionResult ;
      UserRecoverableAuthException recoverableAuthException;

      @DebugLog
      RecoverableIssueImpl(ConnectionResult connectionResult) {
         this.connectionResult = connectionResult;
      }

      @DebugLog
      RecoverableIssueImpl(UserRecoverableAuthException recoverableAuthException) {
         this.recoverableAuthException = recoverableAuthException;
      }

      @Override
      @DebugLog
      public boolean isProcessed() {
         return processed;
      }

      // NOTE (Check, is that going to be a problem?)
      // It'll be up to caller not to execute if intent already in progress.
      @Override
      @DebugLog
      public void recover(Activity activity, int requestCode) {
         processed = true;

         if(connectionResult!=null) {
            try {
               connectionResult.startResolutionForResult(activity, RC_FRAG_GOOGLE_LOGIN);
            } catch (IntentSender.SendIntentException e) {
               // The intent was canceled before it was sent.  Return to the default
               // state and attempt to connect to get an updated ConnectionResult.
               setState(State.CONNECTING);
//               googleApiClient.connect();
            }
         } else if(recoverableAuthException!=null) {
            Intent recover = recoverableAuthException.getIntent();
            activity.startActivityForResult(recover, RC_FRAG_GOOGLE_LOGIN);
         }
      }

      @Override
      public void recoveryAttempResult(int resultCode) {
//         if( !googleApiClient.isConnecting() ) {
//            googleApiClient.connect();
//         }
      }
   }

   private void setState(State state) {
      this.state = state;
      send(state);
   }

   @DebugLog
   private void send(State state) {
      stateBus.onNext(state);
   }

   // TODO: This is where Activities or fragments will register to observe and attempt to recover from issues.
   @Override
   @DebugLog
   public Subscription subscribeToState(Action1<? super State> nextAction) {
      return stateBus
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(nextAction);
   }


   private final Subject<RecoverableIssue,RecoverableIssue> recoverableIssueBus;
   {
      PublishSubject<RecoverableIssue> subject = PublishSubject.create();
      recoverableIssueBus = subject.toSerialized();
   }

   @DebugLog
   private void send(RecoverableIssue issue) {
      recoverableIssueBus.onNext(issue);
   }

   // TODO: This is where Activities or fragments will register to observe and attempt to recover from issues.
   @Override
   @DebugLog
   public Subscription subscribeToRecoverableIssue(Action1<? super RecoverableIssue> onNext) {
      return recoverableIssueBus
         .filter(new Func1<RecoverableIssue, Boolean>() {
            @Override
            public Boolean call(RecoverableIssue recoverableIssue) {
               return !recoverableIssue.isProcessed();
            }
         })
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(onNext);
   }

}
