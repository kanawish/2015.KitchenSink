package com.android_montreal.attendance.domain;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Patterns;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;

/**
 * Created by kanawish on 15-05-11.
 *
 * Very generic Local Interactor class, we'll put all the device-local code here, since we're starting
 * a very small example app. Be mindful to create a split when a sub-domain emerges.
 *
 * It would be interesting if this pattern allows us to organically handle growing
 * complexity in our app.
 *
 */
public class LocalInteractorImpl implements LocalInteractor {

   @Inject
   AccountManager accountManager;

   /**
    * @return an observable on a list of accounts that user emails as a username.
    */
   @Override
   @DebugLog
   public Observable<List<Account>> getAccountEmailsObservable() {
      return Observable
         .from(accountManager.getAccounts())
         .filter(new Func1<Account, Boolean>() {
            @Override
            public Boolean call(Account account) {
               return Patterns.EMAIL_ADDRESS.matcher(account.name).matches();
            }
         })
         .toList();
   }

}
