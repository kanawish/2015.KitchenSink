package com.android_montreal.attendance.domain;

import android.app.Activity;

import hugo.weaving.DebugLog;

/**
 * Created by kanawish on 15-05-13.
 */
public interface RecoverableIssue {

      boolean isProcessed();

      // NOTE (Check, is that going to be a problem?)
      // It'll be up to caller not to execute if intent already in progress.
      void recover(Activity activity, int requestCode);

      void recoveryAttempResult(int resultCode);
}
