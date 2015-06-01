package com.android_montreal.attendance.domain;

import com.android_montreal.attendance.R;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

import java.util.Map;

import javax.inject.Inject;

/**
 * Created by kanawish on 15-05-11.
 *
 * A layer of indirection on top of Firebase allows for mocking.
 *
 * Also illustrates how to avoid or mitigate 'vendor lock-in', a common concern when adopting
 * this kind of solution.
 *
 */
public class FirebaseManagerImpl implements FirebaseManager {

   // NOTE: We don't inject it, since we don't want to expose it in the graph or elsewhere.
   private Firebase firebase;

   public FirebaseManagerImpl(Firebase firebase) {
      this.firebase = firebase;
   }

   @Override
   public void removeAuthStateListener(Firebase.AuthStateListener listener) {
      firebase.removeAuthStateListener(listener);
   }

   @Override
   public Firebase.AuthStateListener addAuthStateListener(Firebase.AuthStateListener listener) {
      return firebase.addAuthStateListener(listener);
   }

   @Override
   public AuthData getAuth() {
      return firebase.getAuth();
   }

   @Override
   public void unauth() {
      firebase.unauth();
   }

   @Override
   public void authAnonymously(Firebase.AuthResultHandler handler) {
      firebase.authAnonymously(handler);
   }

   @Override
   public void authWithPassword(String email, String password, Firebase.AuthResultHandler handler) {
      firebase.authWithPassword(email, password, handler);
   }

   @Override
   public void authWithCustomToken(String token, Firebase.AuthResultHandler handler) {
      firebase.authWithCustomToken(token, handler);
   }

   @Override
   public void authWithOAuthToken(String provider, String token, Firebase.AuthResultHandler handler) {
      firebase.authWithOAuthToken(provider, token, handler);
   }

   @Override
   public void authWithOAuthToken(String provider, Map<String, String> options, Firebase.AuthResultHandler handler) {
      firebase.authWithOAuthToken(provider, options, handler);
   }

   @Override
   public void createUser(String email, String password, Firebase.ResultHandler handler) {
      firebase.createUser(email, password, handler);
   }

   @Override
   public void removeUser(String email, String password, Firebase.ResultHandler handler) {
      firebase.removeUser(email, password, handler);
   }

   @Override
   public void changePassword(String email, String oldPassword, String newPassword, Firebase.ResultHandler handler) {
      firebase.changePassword(email, oldPassword, newPassword, handler);
   }

   @Override
   public void resetPassword(String email, Firebase.ResultHandler handler) {
      firebase.resetPassword(email, handler);
   }

   // TODO: Add the storage methods
}
