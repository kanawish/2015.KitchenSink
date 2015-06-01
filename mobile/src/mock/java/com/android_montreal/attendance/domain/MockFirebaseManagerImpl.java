package com.android_montreal.attendance.domain;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

import java.util.Map;

/**
 * Created by kanawish on 2015-05-31.
 */
public class MockFirebaseManagerImpl implements FirebaseManager {
   @Override
   public void removeAuthStateListener(Firebase.AuthStateListener listener) {

   }

   @Override
   public Firebase.AuthStateListener addAuthStateListener(Firebase.AuthStateListener listener) {
      return null;
   }

   @Override
   public AuthData getAuth() {
      return null;
   }

   @Override
   public void unauth() {

   }

   @Override
   public void authAnonymously(Firebase.AuthResultHandler handler) {

   }

   @Override
   public void authWithPassword(String email, String password, Firebase.AuthResultHandler handler) {

   }

   @Override
   public void authWithCustomToken(String token, Firebase.AuthResultHandler handler) {

   }

   @Override
   public void authWithOAuthToken(String provider, String token, Firebase.AuthResultHandler handler) {

   }

   @Override
   public void authWithOAuthToken(String provider, Map<String, String> options, Firebase.AuthResultHandler handler) {

   }

   @Override
   public void createUser(String email, String password, Firebase.ResultHandler handler) {

   }

   @Override
   public void removeUser(String email, String password, Firebase.ResultHandler handler) {

   }

   @Override
   public void changePassword(String email, String oldPassword, String newPassword, Firebase.ResultHandler handler) {

   }

   @Override
   public void resetPassword(String email, Firebase.ResultHandler handler) {

   }
}
