package com.android_montreal.attendance.domain;

import com.android_montreal.attendance.domain.model.GDG;
import com.android_montreal.attendance.domain.model.Meeting;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by kanawish on 2015-05-31.
 */
public class MockGDGInfoProvider implements GDGInfoProvider {

   GDG[] mockGDGs = new GDG[] {
      new GDG("GDG London", "London", 0, 0),
      new GDG("GDG Montreal Android", "Montreal", 0, 0),
      new GDG("GDG New York", "New York", 0, 0),
      new GDG("GDG Ottawa", "Ottawa", 0, 0),
      new GDG("GDG Paris Android", "Paris", 0, 0),
   };

   Meeting[] mockMeetings = new Meeting[] {
      new Meeting("Monthly Meetup", new Date().getTime())
   };

   @Override
   public GDG findGDG() {
      return mockGDGs[1];
   }

   @Override
   public List<GDG> findGDGs(long longitude, long latitude) {
      return Arrays.asList(mockGDGs);
   }

   @Override
   public Meeting findCurrentMeeting() {
      return mockMeetings[0];
   }

   @Override
   public List<Meeting> findMeetings() {
      return Arrays.asList(mockMeetings);
   }

}
