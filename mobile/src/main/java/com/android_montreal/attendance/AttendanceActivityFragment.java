package com.android_montreal.attendance;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.io.IOException;


/**
 * A placeholder fragment containing a simple view.
 */
public class AttendanceActivityFragment extends Fragment
   implements
      GoogleApiClient.ConnectionCallbacks,
      GoogleApiClient.OnConnectionFailedListener
{

   private static final String TAG = AttendanceActivityFragment.class.getSimpleName();

   /**
    * Utility class for authentication results
    */
   private class AuthResultHandler implements Firebase.AuthResultHandler {

      private final String provider;

      public AuthResultHandler(String provider) {
         this.provider = provider;
      }

      @Override
      public void onAuthenticated(AuthData authData) {
         mAuthProgressDialog.hide();
         Log.i(TAG, provider + " auth successful");
         setAuthenticatedUser(authData);
      }

      @Override
      public void onAuthenticationError(FirebaseError firebaseError) {
         mAuthProgressDialog.hide();
         showErrorDialog(firebaseError.toString());
      }
   }


   /* *************************************
    *              GENERAL                *
    ***************************************/
    /* TextView that is used to display information about the logged in user */
   private TextView mLoggedInStatusTextView;

   /* A dialog that is presented until the Firebase authentication finished. */
   private ProgressDialog mAuthProgressDialog;

   /* A reference to the Firebase */
   private Firebase mFirebaseRef;

   /* Data from the authenticated user */
   private AuthData mAuthData;


   /* *************************************
    *              GOOGLE                 *
    ***************************************/
   /* Request code used to invoke sign in user interactions for Google+ */
   public static final int RC_GOOGLE_LOGIN = 1;

   /* Client used to interact with Google APIs. */
   private GoogleApiClient mGoogleApiClient;

   /* A flag indicating that a PendingIntent is in progress and prevents us from starting further intents. */
   private boolean mGoogleIntentInProgress;

   /* Track whether the sign-in button has been clicked so that we know to resolve all issues preventing sign-in
    * without waiting. */
   private boolean mGoogleLoginClicked;

   /* Store the connection result from onConnectionFailed callbacks so that we can resolve them when the user clicks
    * sign-in. */
   private ConnectionResult mGoogleConnectionResult;

   /* The login button for Google */
   private SignInButton mGoogleLoginButton;

   public AttendanceActivityFragment() {
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      View view = inflater.inflate(R.layout.fragment_attendance, container, false);

      /* *************************************
      *               GOOGLE                *
      ***************************************/
      /* Load the Google login button */
      mGoogleLoginButton = (SignInButton) view.findViewById(R.id.login_with_google);
      mGoogleLoginButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            mGoogleLoginClicked = true;
            if (!mGoogleApiClient.isConnecting()) {
               if (mGoogleConnectionResult != null) {
                  resolveSignInError();
               } else if (mGoogleApiClient.isConnected()) {
                  getGoogleOAuthTokenAndLogin();
               } else {
                    /* connect API now */
                  Log.d(TAG, "Trying to connect to Google API");
                  mGoogleApiClient.connect();
               }
            }
         }
      });
        /* Setup the Google API object to allow Google+ logins */
      mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
         .addConnectionCallbacks(this)
         .addOnConnectionFailedListener(this)
         .addApi(Plus.API)
         .addScope(Plus.SCOPE_PLUS_LOGIN)
         .build();

      /**************************************
      *               GENERAL               *
      ***************************************/
      mLoggedInStatusTextView = (TextView) view.findViewById(R.id.login_status);

        /* Create the Firebase ref that is used for all authentication with Firebase */
      mFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
      mAuthProgressDialog = new ProgressDialog(getActivity());
      mAuthProgressDialog.setTitle("Loading");
      mAuthProgressDialog.setMessage("Authenticating with Firebase...");
      mAuthProgressDialog.setCancelable(false);
      mAuthProgressDialog.show();

        /* Check if the user is authenticated with Firebase already. If this is the case we can set the authenticated
         * user and hide hide any login buttons */
      mFirebaseRef.addAuthStateListener(new Firebase.AuthStateListener() {
         @Override
         public void onAuthStateChanged(AuthData authData) {
            mAuthProgressDialog.hide();
            setAuthenticatedUser(authData);
         }
      });


      return view;


   }

   /* ************************************
    *              GOOGLE                *
    **************************************
    */
    /* A helper method to resolve the current ConnectionResult error. */
   private void resolveSignInError() {
      if (mGoogleConnectionResult.hasResolution()) {
         try {
            mGoogleIntentInProgress = true;
            mGoogleConnectionResult.startResolutionForResult(getActivity(), RC_GOOGLE_LOGIN);
         } catch (IntentSender.SendIntentException e) {
            // The intent was canceled before it was sent.  Return to the default
            // state and attempt to connect to get an updated ConnectionResult.
            mGoogleIntentInProgress = false;
            mGoogleApiClient.connect();
         }
      }
   }

   private void getGoogleOAuthTokenAndLogin() {
      mAuthProgressDialog.show();
        /* Get OAuth token in Background */
      AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
         String errorMessage = null;

         @Override
         protected String doInBackground(Void... params) {
            String token = null;

            try {
               String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
               token = GoogleAuthUtil.getToken(getActivity(), Plus.AccountApi.getAccountName(mGoogleApiClient), scope);
            } catch (IOException transientEx) {
                    /* Network or server error */
               Log.e(TAG, "Error authenticating with Google: " + transientEx);
               errorMessage = "Network error: " + transientEx.getMessage();
            } catch (UserRecoverableAuthException e) {
               Log.w(TAG, "Recoverable Google OAuth error: " + e.toString());
                    /* We probably need to ask for permissions, so start the intent if there is none pending */
               if (!mGoogleIntentInProgress) {
                  mGoogleIntentInProgress = true;
                  Intent recover = e.getIntent();
                  startActivityForResult(recover, RC_GOOGLE_LOGIN);
               }
            } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
               Log.e(TAG, "Error authenticating with Google: " + authEx.getMessage(), authEx);
               errorMessage = "Error authenticating with Google: " + authEx.getMessage();
            }
            return token;
         }

         @Override
         protected void onPostExecute(String token) {
            mGoogleLoginClicked = false;
            if (token != null) {
                    /* Successfully got OAuth token, now login with Google */
               mFirebaseRef.authWithOAuthToken("google", token, new AuthResultHandler("google"));
            } else if (errorMessage != null) {
               mAuthProgressDialog.hide();
               showErrorDialog(errorMessage);
            }
         }
      };
      task.execute();
   }

   /**
    * Once a user is logged in, take the mAuthData provided from Firebase and "use" it.
    */
   private void setAuthenticatedUser(AuthData authData) {
      if (authData != null) {
            /* Hide all the login buttons */
         mGoogleLoginButton.setVisibility(View.GONE);
/*
         mFacebookLoginButton.setVisibility(View.GONE);
         mTwitterLoginButton.setVisibility(View.GONE);
         mPasswordLoginButton.setVisibility(View.GONE);
         mAnonymousLoginButton.setVisibility(View.GONE);
*/
         mLoggedInStatusTextView.setVisibility(View.VISIBLE);
            /* show a provider specific status text */
         String name = null;
         if (authData.getProvider().equals("facebook")
            || authData.getProvider().equals("google")
            || authData.getProvider().equals("twitter")) {
            name = (String) authData.getProviderData().get("displayName");
         } else if (authData.getProvider().equals("anonymous")
            || authData.getProvider().equals("password")) {
            name = authData.getUid();
         } else {
            Log.e(TAG, "Invalid provider: " + authData.getProvider());
         }
         if (name != null) {
            mLoggedInStatusTextView.setText("Logged in as " + name + " (" + authData.getProvider() + ")");
         }
      } else {
            /* No authenticated user show all the login buttons */
         mGoogleLoginButton.setVisibility(View.VISIBLE);
/*
         mFacebookLoginButton.setVisibility(View.VISIBLE);
         mTwitterLoginButton.setVisibility(View.VISIBLE);
         mPasswordLoginButton.setVisibility(View.VISIBLE);
         mAnonymousLoginButton.setVisibility(View.VISIBLE);
*/
         mLoggedInStatusTextView.setVisibility(View.GONE);
      }
      this.mAuthData = authData;
        /* invalidate options menu to hide/show the logout button */
      // supportInvalidateOptionsMenu();
   }

   /**
    * Show errors to users
    */
   private void showErrorDialog(String message) {
      new AlertDialog.Builder(getActivity())
         .setTitle("Error")
         .setMessage(message)
         .setPositiveButton(android.R.string.ok, null)
         .setIcon(android.R.drawable.ic_dialog_alert)
         .show();
   }

   /** GOOGLE AUTH IMPLEMENTATIONS **/
   @Override
   public void onConnected(Bundle bundle) {
        /* Connected with Google API, use this to authenticate with Firebase */
      getGoogleOAuthTokenAndLogin();
   }

   @Override
   public void onConnectionSuspended(int i) {
      // ignore
   }

   @Override
   public void onConnectionFailed(ConnectionResult result) {
      if (!mGoogleIntentInProgress) {
            /* Store the ConnectionResult so that we can use it later when the user clicks on the Google+ login button */
         mGoogleConnectionResult = result;

         if (mGoogleLoginClicked) {
                /* The user has already clicked login so we attempt to resolve all errors until the user is signed in,
                 * or they cancel. */
            resolveSignInError();
         } else {
            Log.e(TAG, result.toString());
         }
      }
   }
}
