package com.android_montreal.attendance.domain;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by kanawish on 15-05-12.
 *
 * A generic error bus. To be used for errors we want to surface to users. Activity/Fragment can chose to
 * respond as appropriate.
 *
 */
@Singleton
public class ErrorBus {

   private final Subject<String,String> bus;
   {
      PublishSubject<String> subject = PublishSubject.create();
      bus = subject.toSerialized();
   }

   @Inject
   public ErrorBus() {
   }

   /**
    * Publish your error messages here.
    * @param error the description of the error.
    */
   public void send(String error) {
      bus.onNext(error);
   }

   /**
    * Subscribe here to get error messages, useful mostly for debugging.
    * @param onNext will be called on main thread.
    * @return subscription to the error messages bus.
    */
   public Subscription subscribeToErrorBus( Action1<String> onNext ) {
      return bus
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(onNext);
   }

}
