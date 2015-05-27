package com.android_montreal.attendance.domain;

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by kanawish on 15-05-11.
 */
public interface GoogleAuthManager {
   // Request code used to invoke sign in user interactions for Google+
   int RC_FRAG_GOOGLE_LOGIN = 42;

   public enum State { NONE, CONNECTING, CONNECTED }

   @DebugLog
   void signin();

   @DebugLog
   void signout();

   @DebugLog
   void revoke();

   @DebugLog
   void authentifyToFirebase();

   // TODO: This is where Activities or fragments will register to observe and attempt to recover from issues.
   @DebugLog
   Subscription subscribeToState(Action1<? super State> onNext);

   // TODO: This is where Activities or fragments will register to observe and attempt to recover from issues.
   @DebugLog
   Subscription subscribeToRecoverableIssue(Action1<? super RecoverableIssue> onNext);
}
