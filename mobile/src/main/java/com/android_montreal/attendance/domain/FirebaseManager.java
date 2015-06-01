package com.android_montreal.attendance.domain;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

import java.util.Map;

/**
 * Created by kanawish on 2015-05-31.
 */
public interface FirebaseManager {
   void removeAuthStateListener(Firebase.AuthStateListener listener);

   Firebase.AuthStateListener addAuthStateListener(Firebase.AuthStateListener listener);

   AuthData getAuth();

   void unauth();

   void authAnonymously(Firebase.AuthResultHandler handler);

   void authWithPassword(String email, String password, Firebase.AuthResultHandler handler);

   void authWithCustomToken(String token, Firebase.AuthResultHandler handler);

   void authWithOAuthToken(String provider, String token, Firebase.AuthResultHandler handler);

   void authWithOAuthToken(String provider, Map<String, String> options, Firebase.AuthResultHandler handler);

   void createUser(String email, String password, Firebase.ResultHandler handler);

   void removeUser(String email, String password, Firebase.ResultHandler handler);

   void changePassword(String email, String oldPassword, String newPassword, Firebase.ResultHandler handler);

   void resetPassword(String email, Firebase.ResultHandler handler);
}
