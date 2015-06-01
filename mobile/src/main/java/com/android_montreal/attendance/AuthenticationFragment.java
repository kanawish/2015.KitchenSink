package com.android_montreal.attendance;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android_montreal.attendance.domain.AuthManager;
import com.android_montreal.attendance.domain.ErrorBus;
import com.android_montreal.attendance.domain.FirebaseManager;
import com.android_montreal.attendance.domain.GoogleAuthManager;
import com.android_montreal.attendance.domain.LocalManager;
import com.android_montreal.attendance.domain.RecoverableIssue;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.google.android.gms.common.SignInButton;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;


/**
 * A placeholder fragment containing a simple view.
 */
public class AuthenticationFragment extends Fragment
{

   /**
    * Utility class for authentication results
   private class AuthResultHandler implements Firebase.AuthResultHandler {

      private final String provider;

      public AuthResultHandler(String provider) {
         this.provider = provider;
      }

      @Override
      public void onAuthenticated(AuthData authData) {
         authProgressDialog.hide();
         Log.i(TAG, provider + " auth successful");
         setAuthenticatedUser(authData);
      }

      @Override
      public void onAuthenticationError(FirebaseError firebaseError) {
         authProgressDialog.hide();
         showErrorDialog(firebaseError.toString());
      }
   }
    */


   /* *************************************
    *              GENERAL                *
    ***************************************/
   // TextView that is used to display information about the logged in user
   private TextView loggedInStatusTextView;

   // A dialog that is presented until the Firebase authentication finished.
   private ProgressDialog authProgressDialog;

   // The sign-in button for Google
   private SignInButton signInGoogleButton;

   // The auth-to-firebase with Google button.
   private Button authToFirebaseWithGoogleButton;

   // Sign-out button (both Firebase and Google)
   private Button signoutButton;

   // Revoke Google authentication/authorisation
   private Button revokeGoogleButton;


   /*** Model ***/
   @Inject FirebaseManager firebaseRef;
   @Inject AuthManager authManager;
   @Inject GoogleAuthManager googleAuthManager;

   @Inject
   LocalManager localManager;

   @Inject ErrorBus errorBus;

   /* Data from the authenticated user */
   // private AuthData authData;

   private RecoverableIssue currentIssue;
   private Subscription subscriptionToGAuthStates;
   private Subscription subscriptionToIssues;
   private Subscription subscriptionToErrorBus;

   public AuthenticationFragment() {
   }

   @Override @DebugLog
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      View view = inflater.inflate(R.layout.fragment_scratchpad, container, false);

      signInGoogleButton = (SignInButton) view.findViewById(R.id.signInGoogleButton);
      signInGoogleButton.setOnClickListener(new View.OnClickListener() {
         @Override
         @DebugLog
         public void onClick(View view) {
            googleAuthManager.signin();
         }
      });

      authToFirebaseWithGoogleButton = (Button) view.findViewById(R.id.authFirebaseWithGoogleButton);
      authToFirebaseWithGoogleButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            googleAuthManager.authentifyToFirebase();
         }
      });

      signoutButton = (Button) view.findViewById(R.id.signoutComboButton);
      signoutButton.setOnClickListener(new View.OnClickListener() {
         @Override
         @DebugLog
         public void onClick(View v) {
            // logout of google plus
            googleAuthManager.signout();

            if (firebaseRef.getAuth() != null) {
               // logout of Firebase
               firebaseRef.unauth();
            }
         }
      });

      revokeGoogleButton = (Button) view.findViewById(R.id.revokeGoogleButton);
      revokeGoogleButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            googleAuthManager.revoke();
         }
      });

      loggedInStatusTextView = (TextView) view.findViewById(R.id.login_status);


      return view;
   }

   @Override @DebugLog
   public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      // Inject the fragment.
      AttendanceApp.get(this.getActivity()).component().inject(this);

      // FIREBASE
      // Setup the progress dialog that is displayed later when authenticating with Firebase
      authProgressDialog = new ProgressDialog(getActivity());
      authProgressDialog.setTitle("Loading");
      authProgressDialog.setMessage("Authenticating with Firebase...");
      authProgressDialog.setCancelable(false);
      authProgressDialog.show();

      // Check if the user is authenticated with Firebase already. If this is the case we can set the authenticated user and hide hide any login buttons
      firebaseRef.addAuthStateListener(new Firebase.AuthStateListener() {
         @Override
         @DebugLog
         public void onAuthStateChanged(AuthData authData) {
            authProgressDialog.hide();
            loggedInStatusTextView.setText(" F:" + authData);
         }
      });

   }

   @Override @DebugLog
   public void onResume() {
      super.onResume();

      // Subscribe to G+
      subscriptionToGAuthStates = googleAuthManager.subscribeToState(
         new Action1<GoogleAuthManager.State>() {
            @Override
            public void call(GoogleAuthManager.State state) {
               handleGoogleAuthState(state);
            }
         }
      );
      subscriptionToIssues = googleAuthManager.subscribeToRecoverableIssue(new Action1<RecoverableIssue>() {
         @Override
         @DebugLog
         public void call(RecoverableIssue recoverableIssue) {
            handleRecoverableIssue(recoverableIssue);
         }
      });
      subscriptionToErrorBus = errorBus.subscribeToErrorBus(new Action1<String>() {
         @Override
         public void call(String errorMessage) {
            loggedInStatusTextView.setText(errorMessage);
         }
      });

   }

   @Override @DebugLog
   public void onPause() {
      super.onPause();

      subscriptionToGAuthStates.unsubscribe();
      subscriptionToIssues.unsubscribe();

      subscriptionToErrorBus.unsubscribe();
   }

   private void handleGoogleAuthState( GoogleAuthManager.State state ) {
      loggedInStatusTextView.setVisibility(View.VISIBLE);
      AuthData auth = firebaseRef.getAuth();
      loggedInStatusTextView.setText("G+: " + state.toString() + " F:" + auth);

      switch(state) {
         case NONE:
            signInGoogleButton.setVisibility(View.VISIBLE);
            authToFirebaseWithGoogleButton.setVisibility(View.GONE);
            signoutButton.setVisibility(View.GONE);
            revokeGoogleButton.setVisibility(View.GONE);
            break;
         case CONNECTING:
            signInGoogleButton.setVisibility(View.GONE);
            authToFirebaseWithGoogleButton.setVisibility(View.GONE);
            signoutButton.setVisibility(View.GONE);
            revokeGoogleButton.setVisibility(View.GONE);
            break;
         case CONNECTED:
            signInGoogleButton.setVisibility(View.GONE);
            // also, optionally, this authData==null?View.VISIBLE:View.GONE
            authToFirebaseWithGoogleButton.setVisibility(View.VISIBLE);
            signoutButton.setVisibility(View.VISIBLE);
            revokeGoogleButton.setVisibility(View.VISIBLE);
            break;
      }
   }

   @DebugLog
   private void handleRecoverableIssue(RecoverableIssue recoverableIssue) {
      if (currentIssue == null) {
         currentIssue = recoverableIssue;
         recoverableIssue.recover(getActivity(), GoogleAuthManager.RC_FRAG_GOOGLE_LOGIN);
      } else {
         Timber.i("Ignoring recoverable issue until currentIssue is resolved.");
      }
   }

   /**
    * This method fires when any startActivityForResult finishes. The requestCode maps to
    * the value passed into startActivityForResult.
    */
   @Override @DebugLog
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      Map<String, String> options = new HashMap<String, String>();
      if (requestCode == GoogleAuthManager.RC_FRAG_GOOGLE_LOGIN) {
         // TODO: Send the result over to the Google auth manager.
         if( currentIssue != null ) {
            currentIssue.recoveryAttempResult(resultCode);
            currentIssue = null ;
         }
      }
   }

   /**
    * Show errors to users
    */
   @DebugLog
   private void showErrorDialog(String message) {
      new AlertDialog.Builder(getActivity())
         .setTitle("Error")
         .setMessage(message)
         .setPositiveButton(android.R.string.ok, null)
         .setIcon(android.R.drawable.ic_dialog_alert)
         .show();
   }


   /**
    * Once a user is logged in, take the authData provided from Firebase and "use" it.
    */
   @DebugLog
   @Deprecated
   private void deprecatedSetAuthenticatedUser(AuthData authData) {
      if (authData != null) {

         /* show a provider specific status text */
         String name = null;
         if (authData.getProvider().equals("facebook")
            || authData.getProvider().equals("google")
            || authData.getProvider().equals("twitter"))
         {
            name = (String) authData.getProviderData().get("displayName");
         } else if (authData.getProvider().equals("anonymous")
            || authData.getProvider().equals("password"))
         {
            name = authData.getUid();
         } else {
            Timber.e("Invalid provider: " + authData.getProvider());
         }
         if (name != null) {
            loggedInStatusTextView.setText("Logged in as " + name + " (" + authData.getProvider() + ")");
         }
      } else {
         /* No authenticated user show all the login buttons */
      }
      // this.authData = authData;

   }

}
