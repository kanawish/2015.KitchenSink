package com.android_montreal.attendance.domain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.Firebase;
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
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by kanawish on 15-05-11.
 */
public class GoogleAuthManagerImpl implements GoogleAuthManager {

   private static final String TAG = GoogleAuthManagerImpl.class.getSimpleName();

   @Inject
   Firebase firebaseRef;

   @Inject
   ErrorBus errorBus;

   @Inject
   AuthManager authManager;

   Context appContext;


   // Client used to interact with Google APIs.
   private GoogleApiClient googleApiClient;

   // NOTE: This must always be set via setter, to keep the stateBus in a valid... state.
   private State state ;
   private Subject<State,State> stateBus ;
   {
      BehaviorSubject<State> subject = BehaviorSubject.create();
      stateBus = subject.toSerialized();
   }

   // NOTE: Inject needed items for initialization methods, since attrib injection happens after construction.
   @Inject
   public GoogleAuthManagerImpl(Context appContext) {
      this.appContext = appContext;
      this.googleApiClient = buildGoogleApiClient();

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
      if (!googleApiClient.isConnecting()) {
         if (!googleApiClient.isConnected()) {
            // Connect API now
            Log.d(TAG, "Trying to connect to Google API");
            setState(State.CONNECTING);
            googleApiClient.connect();
         }
      }
   }

   @Override
   @DebugLog
   public void signout() {
      if (googleApiClient.isConnected()) {
         Plus.AccountApi.clearDefaultAccount(googleApiClient);
         googleApiClient.disconnect();

         setState(State.NONE);
         googleApiClient = buildGoogleApiClient();
      }
   }

   @Override
   @DebugLog
   public void revoke() {
      Plus.AccountApi.clearDefaultAccount(googleApiClient);
      // Our sample has caches no user data from Google+, however we
      // would normally register a callback on revokeAccessAndDisconnect
      // to delete user data so that we comply with Google developer
      // policies.
      Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient);

      setState(State.NONE);
      googleApiClient = buildGoogleApiClient();
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

      // TODO: Get rid of asynctask
        /* Get OAuth token in Background */
      AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
         // If set, sent on postExec()
         String errorMessage = null;

         @Override
         @DebugLog
         protected String doInBackground(Void... params) {
            String token = null;

            try {
               String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
               token = GoogleAuthUtil.getToken(appContext, Plus.AccountApi.getAccountName(googleApiClient), scope);
            } catch (IOException transientEx) {
               // Network or server error
               Log.e(TAG, "Error authenticating with Google: " + transientEx);
               errorMessage = "Network error: " + transientEx.getMessage();
            } catch (UserRecoverableAuthException e) {
               Log.w(TAG, "Recoverable Google OAuth error: " + e.toString());
               // We probably need to ask for permissions.
               send(new RecoverableIssueImpl(e));
            } catch (GoogleAuthException authEx) {
               // The call is not ever expected to succeed assuming you have already verified that Google Play services is installed.
               Log.e(TAG, "Error authenticating with Google: " + authEx.getMessage(), authEx);
               errorMessage = "Error authenticating with Google: " + authEx.getMessage();
            }
            return token;
         }

         @Override
         @DebugLog
         protected void onPostExecute(String token) {
            if (token != null) {
               // Successfully got OAuth token, now login to Firebase with Google's OAuth token.
               firebaseRef.authWithOAuthToken("google", token, authManager.createAuthResultHandler("google"));
            } else if (errorMessage != null) {
               // Signal on the bus.
               authManager.setInProgress(false);
               errorBus.send(errorMessage);
            }
         }
      };
      task.execute();
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
               googleApiClient.connect();
            }
         } else if(recoverableAuthException!=null) {
            Intent recover = recoverableAuthException.getIntent();
            activity.startActivityForResult(recover, RC_FRAG_GOOGLE_LOGIN);
         }
      }

      @Override
      public void recoveryAttempResult(int resultCode) {
         if( !googleApiClient.isConnecting() ) {
            googleApiClient.connect();
         }
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
