package com.android_montreal.attendance;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action0;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by kanawish on 2015-07-20.
 */

public class RxTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // More a demo than a test.
    @Test
    public void testMergeError() {
        final Observable<String> o1 = Observable
            .from(new String[]{"One", "Two", "Three"})
            .doOnNext(new Action1<String>() {
                @Override
                @DebugLog
                public void call(String s) {
                    Timber.d("o1.doOnNext with \"%s\"", s);
                }
            })
            .doOnError(new Action1<Throwable>() {
                @Override
                @DebugLog
                public void call(Throwable e) {
                    Timber.d(e, "o1.doOnError");
                }
            })
            .doOnCompleted(new Action0() {
                @Override
                @DebugLog
                public void call() {
                    // Nothing.
                }
            });

        Observable<String> oE = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onError(new Exception("Bogus exception"));
            }
        });

        final Observable<Integer> o2 = Observable
            .from(new Integer[]{1, 2, 3, 4})
            .doOnCompleted(new Action0() {
                @Override
                public void call() {
                    Timber.d("o2.doOnCompleted");
                }
            });

        final Observable<?>[] observables = new Observable<?>[]{o1, oE, o2};

        Observable
            .merge(observables)
            .subscribe(
                new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        Timber.d("merge.onNext %s", o.toString());
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable e) {
                        Timber.d(e, "merge.onError");
                    }
                },
                new Action0() {
                    @Override
                    public void call() {
                        Timber.d("merge.onCompleted");
                    }
                }
            );

    }

    // This triggers an OnErrorNotImplementedException
    @Test
    public void testMergeUnhandledError() {
        final Observable<String> o1 = Observable
            .from(new String[]{"One", "Two", "Three"})
            .doOnNext(new Action1<String>() {
                @Override
                @DebugLog
                public void call(String s) {
                    Timber.d("o1.doOnNext with \"%s\"", s);
                }
            })
            .doOnError(new Action1<Throwable>() {
                @Override
                @DebugLog
                public void call(Throwable e) {
                    Timber.d(e, "o1.doOnError");
                }
            })
            .doOnCompleted(new Action0() {
                @Override
                @DebugLog
                public void call() {
                    // Nothing.
                }
            });

        Observable<String> oE = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onError(new Exception("Bogus exception"));
            }
        });

        final Observable<Integer> o2 = Observable
            .from(new Integer[]{1, 2, 3, 4})
            .doOnCompleted(new Action0() {
                @Override
                public void call() {
                    Timber.d("o2.doOnCompleted");
                }
            });

        final Observable<?>[] observables = new Observable<?>[]{o1, oE, o2};

        thrown.expect(OnErrorNotImplementedException.class);

        Observable.merge(observables).subscribe(
            new Action1<Object>() {
                @Override
                public void call(Object o) {
                    Timber.d("merge.onNext %s", o.toString());
                }
            }
        );
    }
}