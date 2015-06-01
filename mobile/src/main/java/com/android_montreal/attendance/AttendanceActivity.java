package com.android_montreal.attendance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.android_montreal.attendance.domain.GoogleAuthManager;

import hugo.weaving.DebugLog;


public class AttendanceActivity extends Activity {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_attendance);
   }

   @Override
   @DebugLog
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if(requestCode == GoogleAuthManager.RC_FRAG_GOOGLE_LOGIN) {
         getFragmentManager().findFragmentById(R.id.attendanceFragment).onActivityResult(requestCode,resultCode,data);
      } else {
         super.onActivityResult(requestCode, resultCode, data);
      }
   }

}