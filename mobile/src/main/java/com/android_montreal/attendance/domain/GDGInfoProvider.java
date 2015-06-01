package com.android_montreal.attendance.domain;

import com.android_montreal.attendance.domain.model.GDG;
import com.android_montreal.attendance.domain.model.Meeting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kanawish on 2015-05-31.
 *
 * Will need to see what is available out there, it all already exists, it's a given.
 *
 * Some capabilities we'll want:
 *
 * - List of GDGs
 * - Finding a GDG closest to our location
 * - Sorting by distance
 * - Fetching dates of events from
 *    - The Google Dev Site
 *    - Meetup.com (will soon be the official source)
 * - Consider using Firebase to abstract all the updating to clients.
 *    - This implies creating a small server that would update FireBase.
 *    - Then clients would be synchronized via that channel.
 *
 */
public interface GDGInfoProvider {

   GDG findGDG();

   List<GDG> findGDGs(long longitude, long latitude);

   Meeting findCurrentMeeting();

   List<Meeting> findMeetings();

}
