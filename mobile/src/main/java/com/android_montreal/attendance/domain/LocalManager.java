package com.android_montreal.attendance.domain;

import android.accounts.Account;

import java.util.List;

import rx.Observable;

/**
 * Created by kanawish on 15-05-11.
 */
public interface LocalManager {
   Observable<List<Account>> getAccountEmailsObservable();
}
