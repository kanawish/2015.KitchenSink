package com.android_montreal.attendance;

import android.app.Fragment;

/**
 * Created by kanawish on 15-05-11.
 */
public interface AttendanceComponent {
   void inject(AttendanceApp app);
   void inject(AttendanceActivity activity);
   void inject(Fragment fragment);
}
