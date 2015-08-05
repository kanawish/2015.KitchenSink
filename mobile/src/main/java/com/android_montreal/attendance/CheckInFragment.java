package com.android_montreal.attendance;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.Bind;
import hugo.weaving.DebugLog;

/**
 * Created by kanawish on 2015-05-31.
 */
public class CheckInFragment extends Fragment {



   @Bind(R.id.gdgBadgeImageView)
   ImageView gdgBadgeImageView;

   @Bind(R.id.meetTitleLabel)
   TextView meetTitleLabel;
   @Bind(R.id.dateTimeLabel)
   TextView dateTimeLabel;
   @Bind(R.id.gdgNameLabel)
   TextView gdgNameLabel;

   @Bind(R.id.checkInButton)
   Button checkInButton;

   @Override
   @DebugLog
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_checkin, container, false);

      ButterKnife.bind(this, view);

      return view;
   }

   @Override
   @DebugLog
   public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      // Inject the fragment.
      AttendanceApp.get(this.getActivity()).component().inject(this);
   }

   @Override
   @DebugLog
   public void onResume() {
      super.onResume();

      // Sub to model.
   }

   @Override
   @DebugLog
   public void onPause() {
      super.onPause();

      // Unsub. from model.
   }

   @Override
   @DebugLog
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

   }

   @Override
   public void onDestroyView() {
      super.onDestroyView();
      ButterKnife.unbind(this);
   }
}