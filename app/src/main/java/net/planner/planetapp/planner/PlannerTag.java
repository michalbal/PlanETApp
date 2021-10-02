package net.planner.planetapp.planner;

import android.util.Log;
import android.util.Pair;

import com.brein.time.timeintervals.collections.ListIntervalCollection;
import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder;
import com.brein.time.timeintervals.intervals.IInterval;
import com.brein.time.timeintervals.intervals.LongInterval;

import net.planner.planetapp.UtilsKt;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PlannerTag {

    // Constants
    private static final long ONE_WEEK = TimeUnit.DAYS.toMillis(7);
    private static final String TAG = "PlannerTag";

    // Fields
    private String tagName;
    private IntervalTree forbiddenTimeIntervals;
    private IntervalTree preferredTimeIntervals;
    private int priority;
    private HashMap<Pair<String, String>, ArrayList<String>> forbiddenTIsettings;
    private HashMap<Pair<String, String>, ArrayList<String>> preferredTIsettings;
    private long cacheForbiddenStartTime, cacheForbiddenEndTime;
    private long cachePreferredStartTime, cachePreferredEndTime;

    // constructor
    /** Create PlannerTag from its title **/
    public PlannerTag(String tagName) {
        this.tagName = tagName;
        this.forbiddenTimeIntervals = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG)
                .collectIntervals(interval -> new ListIntervalCollection()).build();
        this.preferredTimeIntervals = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG)
                .collectIntervals(interval -> new ListIntervalCollection()).build();
        this.forbiddenTIsettings = new HashMap<>();
        this.preferredTIsettings = new HashMap<>();
        invalidateForbiddenCache();
        invalidatePreferredCache();
    }

    public PlannerTag(String tagName,int priority,
                      HashMap<String, ArrayList<String>> forbiddenSettings,
                      HashMap<String, ArrayList<String>> preferredSettings){
        this(tagName);
        this.priority = priority;
        unflattenPair(forbiddenSettings, this.forbiddenTIsettings);
        unflattenPair(preferredSettings, this.preferredTIsettings);
    }

    private void unflattenPair(HashMap<String, ArrayList<String>> stringKeyMap, HashMap<Pair<String,
            String>, ArrayList<String>> pairKeyMap){
        for (HashMap.Entry<String, ArrayList<String>> entry : stringKeyMap.entrySet()){
            //            String first, second = entry.getKey().split("-", 2);
            Pair<String, String> pair = new Pair<>(entry.getKey().substring(0,5),
                                                   entry.getKey().substring(6));
            pairKeyMap.put(pair, entry.getValue());
        }
    }

    public void addNewForbiddenTIsetting(String day, String startTime, String endTime){
        Pair<String, String> pair = new Pair<>(startTime, endTime);
        if (forbiddenTIsettings.containsKey(pair)){
            if (!forbiddenTIsettings.get(pair).contains(day)) {
                forbiddenTIsettings.get(pair).add(day);
                invalidateForbiddenCache();
            }
        } else {
            ArrayList<String> days = new ArrayList<>();
            days.add(day);
            forbiddenTIsettings.put(pair, days);
            invalidateForbiddenCache();
        }
    }

    public HashMap<Pair<String, String>, ArrayList<String>> getForbiddenTIsettings() {
        return forbiddenTIsettings;
    }

    public void addNewPreferredTIsetting(String day, String startTime, String endTime){
        Pair<String, String> pair = new Pair<>(startTime, endTime);
        if (preferredTIsettings.containsKey(pair)){
            if (!preferredTIsettings.get(pair).contains(day)) {
                preferredTIsettings.get(pair).add(day);
                invalidatePreferredCache();
            }
        } else {
            ArrayList<String> days = new ArrayList<>();
            days.add(day);
            preferredTIsettings.put(pair, days);
            invalidatePreferredCache();
        }
    }

    public HashMap<Pair<String, String>, ArrayList<String>> getPreferredTIsettings() {
        return preferredTIsettings;
    }

    //methods
    /** Get the name of the tag **/
    public String getTagName() {
        return tagName;
    }

    /** Set the name of the tag **/
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /** Get priority of the tag **/
    public int getPriority() {
        return priority;
    }

    /** Set priority of the tag as integer from 1 to 10 **/
    public boolean setPriority(int priority) {
        if (priority < 1 || priority > 10) {
            Log.e(TAG,"Illegal priority: Priority is integer from 1 to 10");
            return false;
        }
        this.priority = priority;
        return true;
    }

    /** Get time intervals during which it's forbidden to create tasks tagged with it **/
    public List<IInterval> getForbiddenTimeIntervals() {
        return new ArrayList<>(forbiddenTimeIntervals);
    }

    /** Get iterator over time intervals in which it's forbidden to create tasks tagged with it **/
    public Iterator<IInterval> getForbiddenTimeIntervalsIterator() {
        return forbiddenTimeIntervals.iterator();
    }

    /** Get iterator over time intervals in which it's forbidden to create tasks tagged with it **/
    public Iterator<IInterval> getForbiddenTimeIntervalsIterator(long startTime, long endTime) {
        return getForbiddenTimeIntervalsTree(startTime, endTime).iterator();
    }

    /** Get tree with the time intervals in which it's forbidden to create tasks tagged with it **/
    public final IntervalTree getForbiddenTimeIntervalsTree() {
        return forbiddenTimeIntervals;
    }

    /** Get tree with the time intervals in which it's forbidden to create tasks tagged with it **/
    public final IntervalTree getForbiddenTimeIntervalsTree(long startTime, long endTime) {
        if (forbiddenTIsettings.isEmpty() ||
            (cacheForbiddenStartTime == startTime && cacheForbiddenEndTime == endTime)) {
                return forbiddenTimeIntervals;
        }

        Log.d(TAG, "New forbiddenTimeIntervals:");
        forbiddenTimeIntervals = generateTreeOutOfSettings(forbiddenTIsettings, startTime, endTime);
        cacheForbiddenStartTime = startTime;
        cacheForbiddenEndTime = endTime;
        return forbiddenTimeIntervals;
    }

    /** Add a time interval during which it's forbidden to create tasks tagged with it **/
    public boolean addForbiddenTimeInterval(long from, long until) {
        if (until < from) {
            Log.e(TAG,"Illegal time interval: Event cannot end before it starts");
            return false;
        }
        this.forbiddenTimeIntervals.add(new LongInterval(from, until));
        return true;
    }

    /** Get time intervals during which it's preferred to create tasks tagged with it **/
    public List<IInterval> getPreferredTimeIntervals() {
        return new ArrayList<>(preferredTimeIntervals);
    }

    /** Get iterator over time intervals in which it's preferred to create tasks tagged with it **/
    public Iterator<IInterval> getPreferredTimeIntervalsIterator() {
        return preferredTimeIntervals.iterator();
    }

    /** Get iterator over time intervals in which it's preferred to create tasks tagged with it **/
    public Iterator<IInterval> getPreferredTimeIntervalsIterator(long startTime, long endTime) {
        return getPreferredTimeIntervalsTree(startTime, endTime).iterator();
    }

    /** Get tree with the time intervals in which it's preferred to create tasks tagged with it **/
    public final IntervalTree getPreferredTimeIntervalsTree() {
        return preferredTimeIntervals;
    }

    /** Get tree with the time intervals in which it's preferred to create tasks tagged with it **/
    public final IntervalTree getPreferredTimeIntervalsTree(long startTime, long endTime) {
        if (preferredTIsettings.isEmpty() ||
                (cachePreferredStartTime == startTime && cachePreferredEndTime == endTime)) {
            return preferredTimeIntervals;
        }

        Log.d(TAG, "New preferredTimeIntervals:");
        preferredTimeIntervals = generateTreeOutOfSettings(preferredTIsettings, startTime, endTime);
        cachePreferredStartTime = startTime;
        cachePreferredEndTime = endTime;
        return preferredTimeIntervals;
    }

    /** Add a time interval during which it's preferred to create tasks tagged with it **/
    public boolean addPreferredTimeInterval(long from, long until) {
        if (until < from) {
            Log.e(TAG,"Illegal time interval: Event cannot end before it starts");
            return false;
        }
        this.preferredTimeIntervals.add(new LongInterval(from, until));
        return true;
    }

    /** Get forbidden for this tag time intervals that collide with the given one **/
    public Collection<?> getForbiddenCollisions(long startDate, long endDate) {
        return forbiddenTimeIntervals.overlap(new LongInterval(startDate, endDate));
    }

    /** Get preferred for this tag time intervals that collide with the given one **/
    public Collection<?> getPreferredCollisions(long startDate, long endDate) {
        return preferredTimeIntervals.overlap(new LongInterval(startDate, endDate));
    }

    /** Return whether or not the given time interval is forbidden for this tag **/
    public boolean isIntervalForbidden(long startDate, long endDate) {
        return getForbiddenCollisions(startDate, endDate).isEmpty();
    }

    /** Return whether or not the given time interval is preferred for this tag **/
    public boolean isIntervalPreferred(long startDate, long endDate) {
        return getPreferredCollisions(startDate, endDate).isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlannerTag that = (PlannerTag) o;
        return tagName.equals(that.tagName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagName);
    }

    // Helper functions
    private void invalidateForbiddenCache() {
        cacheForbiddenStartTime = cacheForbiddenEndTime = -1L;
    }

    private void invalidatePreferredCache() {
        cachePreferredStartTime = cachePreferredEndTime = -1L;
    }

    private static IntervalTree generateTreeOutOfSettings(
            HashMap<Pair<String, String>, ArrayList<String>> settings, long startTime, long endTime) {
        IntervalTree newTree = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG)
                .collectIntervals(interval -> new ListIntervalCollection()).build();

        for(HashMap.Entry<Pair<String, String>, ArrayList<String>> intervalToDays : settings.entrySet()) {
            Pair<String, String> stringInterval = intervalToDays.getKey();
            ArrayList<String> days = intervalToDays.getValue();
            Log.d(TAG, "Time: " + stringInterval);
            Log.d(TAG, "Days: " + days);

            // Translate string interval to long interval after startTime.
            Long intervalStart = UtilsKt.getMillisFromHour(stringInterval.first);
            Long intervalEnd = UtilsKt.getMillisFromHour(stringInterval.second);
            if (intervalStart == null || intervalEnd == null) {
                Log.e(TAG, "Illegal time string: " + stringInterval);
                continue;
            }
            Log.d(TAG, "intervalStart: " + intervalStart);
            Log.d(TAG, "intervalEnd: " + intervalEnd);

            for (String day : days) {
                long numDaysInMillis = UtilsKt.getMillisSinceSunday(day);
                Log.d(TAG, "numDaysInMillis: " + numDaysInMillis);

                if (numDaysInMillis == -1L) {
                    Log.e(TAG, "Illegal day string: " + day);
                    continue;
                }

                LongInterval validTime = getFirstValidTime(startTime, endTime,
                        intervalStart + numDaysInMillis, intervalEnd + numDaysInMillis);
                while (validTime != null) {
                    newTree.add(validTime);
                    validTime = getNextValidTime(endTime, validTime);
                }
            }
        }

        Log.d(TAG, "Results:");
        for (IInterval iInterval : newTree) {
            LongInterval interval = (LongInterval) iInterval;
            Log.d(TAG, "Start: " + UtilsKt.getDate(interval.getStart()));
            Log.d(TAG, "End: " + UtilsKt.getDate(interval.getEnd()));
        }

        return newTree;
    }

    private static long getTimeAtStartOfWeek(long time) {
        Calendar timeAtStartOfWeek = Calendar.getInstance();
        timeAtStartOfWeek.setTimeInMillis(time);
        timeAtStartOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        timeAtStartOfWeek.set(Calendar.HOUR_OF_DAY, 0);
        timeAtStartOfWeek.set(Calendar.MINUTE, 0);
        timeAtStartOfWeek.set(Calendar.SECOND, 0);
        timeAtStartOfWeek.set(Calendar.MILLISECOND, 0);

        return timeAtStartOfWeek.getTimeInMillis();
    }

    private static LongInterval getFirstValidTime(long validStart, long validEnd, long intervalStart, long intervalEnd) {
        Log.d(TAG, "validStart: " + validStart);
        Log.d(TAG, "validEnd: " + validEnd);
        Log.d(TAG, "intervalStart: " + intervalStart);
        Log.d(TAG, "intervalEnd: " + intervalEnd);

        long attemptStart = getTimeAtStartOfWeek(validStart) + intervalStart;
        Log.d(TAG, "attemptStart: " + attemptStart);

        while (attemptStart < validStart) {
            attemptStart += ONE_WEEK;
        }
        if (attemptStart > validEnd) {
            return null;
        }

        long duration = intervalEnd - intervalStart;
        long attemptEnd = attemptStart + duration;
        if (attemptEnd > validEnd) {
            return new LongInterval(attemptStart, validEnd);
        }
        return new LongInterval(attemptStart, attemptEnd);
    }

    private static LongInterval getNextValidTime(long validEnd, LongInterval lastValid) {
        long attemptStart = lastValid.getStart() + ONE_WEEK;
        if (attemptStart > validEnd) {
            return null;
        }

        long attemptEnd = lastValid.getEnd() + ONE_WEEK;
        if (attemptEnd > validEnd) {
            return new LongInterval(attemptStart, validEnd);
        }
        return new LongInterval(attemptStart, attemptEnd);
    }

}
