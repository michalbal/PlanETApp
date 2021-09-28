package net.planner.planetapp.planner;

import com.brein.time.exceptions.IllegalTimeInterval;
import com.brein.time.exceptions.IllegalTimePoint;
import com.brein.time.timeintervals.collections.ListIntervalCollection;
import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder;
import com.brein.time.timeintervals.intervals.IInterval;
import com.brein.time.timeintervals.intervals.LongInterval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

/**
 * The PlannerCalendar represents a calendar that can hold and tasks and events.
 */
public class PlannerCalendar {

    // Constants
    public static final int SPACE_IN_MINUTES = 15;
    private static final int MIN_SPACE_IN_SECONDS = 1;
    private static final long DEFAULT_LENGTH = TimeUnit.DAYS.toMillis(30);
    private static final long MIN_SPACE_IN_MILLIS = MIN_SPACE_IN_SECONDS * 1000L;
    public static final long RECOMMENDED_SPACE_IN_MILLIS = SPACE_IN_MINUTES * 60000L;

    // Fields
    private long startTime, endTime; // This calendar tracks the interval [startTime, endTime] (ms).
    private long spaceBetweenEvents;
    private IntervalTree occupiedTree;
    private HashMap<String, PlannerTag> tags;

    // Constructors

    /**
     * Construct a new calendar with the current system time as the start time.
     */
    public PlannerCalendar() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + DEFAULT_LENGTH;

        init(startTime, endTime, RECOMMENDED_SPACE_IN_MILLIS, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Construct a new calendar with the given start time.
     */
    public PlannerCalendar(long startTime) {
        long endTime = startTime + DEFAULT_LENGTH;

        init(startTime, endTime, RECOMMENDED_SPACE_IN_MILLIS, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Construct a new calendar with the given start and end time.
     */
    public PlannerCalendar(long startTime, long endTime) {
        if (endTime <= startTime + RECOMMENDED_SPACE_IN_MILLIS)
            endTime = startTime + DEFAULT_LENGTH;

        init(startTime, endTime, RECOMMENDED_SPACE_IN_MILLIS, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Construct a new calendar with the given time interval and the given space to leave between tasks.
     */
    public PlannerCalendar(long startTime, long endTime, long spaceBetweenEvents) {
        if (spaceBetweenEvents < MIN_SPACE_IN_MILLIS) spaceBetweenEvents = MIN_SPACE_IN_MILLIS;
        if (endTime <= startTime + spaceBetweenEvents) endTime = startTime + DEFAULT_LENGTH;

        init(startTime, endTime, spaceBetweenEvents, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Construct a new calendar with the given time interval, the given space between tasks and with the given events.
     */
    public PlannerCalendar(long startTime, long endTime, long spaceBetweenEvents, List<PlannerEvent> eventList) {
        if (spaceBetweenEvents < MIN_SPACE_IN_MILLIS) spaceBetweenEvents = MIN_SPACE_IN_MILLIS;
        if (endTime <= startTime + spaceBetweenEvents) endTime = startTime + DEFAULT_LENGTH;
        if (eventList == null) eventList = Collections.emptyList();

        init(startTime, endTime, spaceBetweenEvents, eventList, Collections.emptyList());
    }

    /**
     * Construct a new calendar with the given time interval, given space between tasks, given events and given tags.
     */
    public PlannerCalendar(long startTime, long endTime, long spaceBetweenEvents, List<PlannerEvent> eventList, List<PlannerTag> newTags) {
        if (spaceBetweenEvents < MIN_SPACE_IN_MILLIS) spaceBetweenEvents = MIN_SPACE_IN_MILLIS;
        if (endTime <= startTime + spaceBetweenEvents) endTime = startTime + DEFAULT_LENGTH;
        if (eventList == null) eventList = Collections.emptyList();
        if (newTags == null) newTags = Collections.emptyList();

        init(startTime, endTime, spaceBetweenEvents, eventList, newTags);
    }

    /**
     * Helper function: Actual constructor (receives default values from other constructors).
     */
    private void init(long startTime, long endTime, long spaceBetweenEvents, List<PlannerEvent> eventList, List<PlannerTag> tagList) {
        // Get time at start of day.
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(startTime);
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);
        this.startTime = startDate.getTimeInMillis();
        this.endTime = endTime;

        // Add tags.
        tags = new HashMap<>(tagList.size());
        for (PlannerTag tag : tagList) {
            String tagName = tag.getTagName();
            if (!tagName.equals(PlannerObject.NO_TAG)) {
                tags.put(tagName, tag);
            }
        }

        // Create occupiedTree and add events.
        occupiedTree = IntervalTreeBuilder.newBuilder()
                .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG)
                .collectIntervals(interval -> new ListIntervalCollection()).build();
        for (PlannerEvent event : eventList) {
            insertEvent(event);
        }

        // Define space between tasks.
        this.spaceBetweenEvents = spaceBetweenEvents;
    }

    // Methods

    /**
     * Returns the start time for this calendar (all dates have to be after this one).
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns all intervals in the calendar that overlap with [startDate, endDate].
     */
    public Collection<IInterval> getCollisions(long startDate, long endDate) {
        return occupiedTree.overlap(new LongInterval(startDate, endDate));
    }

    /**
     * Returns true if the interval [startDate, endDate] doesn't overlap with any interval in this calendar.
     */
    public boolean isIntervalAvailable(long startDate, long endDate) {
        return getCollisions(startDate, endDate).isEmpty();
    }

    /**
     * Returns true if the interval [startDate, endDate] is tagged as forbidden by a tag named tagName in this calendar.
     */
    public boolean isIntervalTaggedForbidden(String tagName, long startDate, long endDate) {
        PlannerTag tag = safeGetTag(tagName);
        if (tag == null) {
            return false;
        }

        return tag.isIntervalForbidden(startDate, endDate);
    }

    /**
     * Returns true if the interval [startDate, endDate] is tagged as preferred by a tag named tagName in this calendar.
     */
    public boolean isIntervalTaggedPreferred(String tagName, long startDate, long endDate) {
        PlannerTag tag = safeGetTag(tagName);
        if (tag == null) {
            return false;
        }

        return tag.isIntervalPreferred(startDate, endDate);
    }

    /**
     * Attempts to insert the given event into this calendar (interval must not overlap). Returns true if successful.
     */
    public boolean insertEvent(PlannerEvent event) {
        if (isInvalidDate(event.getStartTime()) || isInvalidDate(event.getEndTime())) {
            return false;
        }

        OccupiedInterval toInsert = new OccupiedInterval(event);
        return !occupiedTree.contains(toInsert) && occupiedTree.add(toInsert);
    }

    /**
     * Attempts to insert the given event into this calendar (can overlap with others). Returns true if successful.
     */
    public boolean forceInsertEvent(PlannerEvent event) {
        if (isInvalidDate(event.getStartTime()) || isInvalidDate(event.getEndTime())) {
            return false;
        }

        OccupiedInterval toInsert = new OccupiedInterval(event);
        return occupiedTree.add(toInsert);
    }

    /**
     * Inserts a task into the calendar at best possible time. Returns events it was assigned to. On failure, returns empty list.
     */
    public List<PlannerEvent> insertTask(PlannerTask task) {
        List<PlannerEvent> events = splitTask(task);
        for (PlannerEvent event : events) {
            occupiedTree.add(new OccupiedInterval(event));
        }
        return events;
    }

    /**
     * Inserts a list of tasks into the calendar at the best possible time. Returns events they were assigned to.
     */
    public LinkedList<PlannerEvent> insertTasks(List<PlannerTask> tasks) {
        ArrayList<PlannerTask> sorted = new ArrayList<>(tasks);
        Collections.sort(sorted, new TaskByDeadlineComparator());

        LinkedList<List<PlannerEvent>> firstTry = tryToInsertTasks(sorted, 0);
        LinkedList<List<PlannerEvent>> attempt = firstTry;
        int full_size = tasks.size();
        int size = attempt.size();
        int fail_count = 0;

        while (size != full_size) {
            ++fail_count;
            // System.out.println("FAILED " + fail_count + " TIMES WITH (SIZE, FULL_SIZE) = (" + size + ", " + full_size + ")");

            int shift = full_size - fail_count - 1;
            if (shift < 0) {
                attempt = firstTry;
                break;
            }

            // Backtrack one step with the problematic task.
            Collections.swap(sorted, shift + 1, shift);
            ListIterator<List<PlannerEvent>> removalIt = attempt.listIterator(size);
            for (int i = 0; i < fail_count; ++i) {
                List<PlannerEvent> eventsToRemove = removalIt.previous();
                for (PlannerEvent toRemove : eventsToRemove) {
                    removeEvent(toRemove);
                }
            }

            attempt = tryToInsertTasks(sorted, shift);
            size = attempt.size();
        }

        // Flatten list of lists.
        LinkedList<PlannerEvent> result = new LinkedList<>();
        for (List<PlannerEvent> events : attempt) {
            result.addAll(events);
        }
        return result;
    }

    /**
     * Removes the given event from this calendar. Return true if found.
     */
    public boolean removeEvent(PlannerEvent event) {
        OccupiedInterval toRemove = new OccupiedInterval(event);
        return occupiedTree.remove(toRemove);
    }

    /**
     * Removes the given task from this calendar. Return true if found.
     */
    public boolean removeTask(PlannerTask task) {
        List<PlannerEvent> childEvents = task.getChildEvents();
        if (childEvents == null) {
            return false;
        }

        // Remove all child events even if not all found so we can "clean" the tree.
        boolean success = true;
        for (PlannerEvent child : childEvents) {
            success &= removeEvent(child);
        }
        return success;
    }

    /**
     * Returns true if this calendar contains a tag with the given name.
     */
    public boolean containsTag(String tagName) {
        return tags.containsKey(tagName);
    }

    /**
     * Returns true if this calendar contains a tag with the given name.
     */
    public boolean containsTag(PlannerTag tag) {
        return tags.containsKey(tag.getTagName());
    }

    /**
     * Attempts to add the given tag to the calendar. Returns true if successful (if name wasn't given already).
     */
    public boolean addTag(PlannerTag tag) {
        String tagName = tag.getTagName();
        if (tags.containsKey(tagName)) {
            return false;
        }

        tags.put(tagName, tag);
        return true;
    }

    /**
     * Removes the tag with the given name from this calendar. Return true if found.
     */
    public boolean removeTag(String tagName) {
        if (!tags.containsKey(tagName)) {
            return false;
        }

        tags.remove(tagName);
        return true;
    }

    /**
     * Returns the tag with the given name from this calendar. Return null if not found.
     */
    public PlannerTag getTag(String tagName) {
        return tags.get(tagName);
    }

    /**
     * Returns the names of all the tags in this calendar.
     */
    public List<String> getTagNames() {
        return new ArrayList<>(tags.keySet());
    }

    /**
     * Returns all the tags in this calendar.
     */
    public List<PlannerTag> getTags() {
        return new ArrayList<>(tags.values());
    }

    // Helper functions

    /**
     * Inserts a list of tasks into the calendar at the best possible time. Returns events they were assigned to.
     */
    private LinkedList<List<PlannerEvent>> tryToInsertTasks(ArrayList<PlannerTask> sorted, int startIndex) {
        LinkedList<List<PlannerEvent>> newEvents = new LinkedList<>();
        int size = sorted.size();

        for (int i = startIndex; i < size; ++i) {
            PlannerTask current = sorted.get(i);
            List<PlannerEvent> currentResult = insertTask(current);

            if (currentResult.isEmpty()) {
                return newEvents;
            }
            newEvents.add(currentResult);
        }

        return newEvents;
    }

    /**
     * Returns best possible times for task insertion. On failure, returns empty list.
     */
    private List<PlannerEvent> splitTask(PlannerTask task) {
        OccupiedInterval.setMaxSessionTime(task.getMaxSessionTimeInMillis());
        PriorityQueue<OccupiedInterval> options = new PriorityQueue<>(task.getMaxDivisionsNumber(), new IntervalByLengthComparator());
        FreeTimeIterator freeTimeIt = new FreeTimeIterator();

        // If there is no tag then use specific function for tag-less tasks.
        PlannerTag tag = safeGetTag(task.getTagName());
        if (tag == null) {
            findIntervalForUntaggedTask(task, freeTimeIt, options);

            return task.splitIntoEvents(options, spaceBetweenEvents);
        }

        // Attempt to insert only in preferred time.
        findIntervalForTask(task, tag.getPreferredTimeIntervalsIterator(), occupiedTree, options);
        List<PlannerEvent> events = task.splitIntoEvents(options, spaceBetweenEvents);
        if (!events.isEmpty()) {
            return events;
        }

        // Attempt to insert only in non-forbidden free time.
        findIntervalForTask(task, freeTimeIt, tag.getForbiddenTimeIntervalsTree(), options);
        return task.splitIntoEvents(options, spaceBetweenEvents);
    }

    /**
     * Helper function: Returns the first time in the calendar where the start doesn't overlap with others.
     */
    private long getSpacedStartTime(LongInterval interval) {
        long startTime = interval.getStart();
        long maxEnd = startTime - spaceBetweenEvents;
        Collection<IInterval> untaggedCollisions = getCollisions(maxEnd, startTime);
        for (IInterval untaggedGeneric : untaggedCollisions) {
            LongInterval untagged = (LongInterval) untaggedGeneric;
            long end = untagged.getEnd();
            if (end > maxEnd) {
                maxEnd = end;
            }
        }
        return maxEnd + spaceBetweenEvents;
    }

    /**
     * Helper function: Returns a collection where all overlapping intervals have been merged.
     */
    private Collection<IInterval> mergeOverlapping(Collection<IInterval> intervals) {
        if (intervals.size() <= 1) {
            return intervals;
        }

        Iterator<IInterval> it = intervals.iterator();
        LinkedList<IInterval> merged = new LinkedList<>();

        LongInterval previous = (LongInterval) it.next();
        while (it.hasNext()) {
            LongInterval current = (LongInterval) it.next();
            if (previous.irBefore(current)) {
                merged.add(previous);
                previous = current;
            } else {
                previous = new LongInterval(previous.getStart(), current.getEnd());
            }

        }
        merged.add(previous);

        return merged;
    }

    /**
     * Helper function: Inserts an untagged task into the calendar at the first free time. Returns events it was assigned to.
     */
    private void findIntervalForUntaggedTask(PlannerTask task, Iterator<IInterval> possibleIterator,
                                             PriorityQueue<OccupiedInterval> options) {
        long minimalDuration = task.getMinSessionTimeInMillis();
        long minimalSlot = minimalDuration + spaceBetweenEvents;

        // Iterate over possible intervals.
        while (possibleIterator.hasNext()) {
            LongInterval possibleInterval = (LongInterval) possibleIterator.next();

            // Find first possible starting time in possible interval
            long startTime = getSpacedStartTime(possibleInterval);
            long endTime = possibleInterval.getEnd();

            // Check if tagged interval is long enough.
            long possibleDuration = endTime - startTime;
            if (possibleDuration >= minimalSlot) {
                options.add(new OccupiedInterval(startTime, endTime - spaceBetweenEvents));
            }
        }
    }

    /**
     * Helper function: Inserts a task into the calendar at the first possible time that doesn't collide. Returns events it was assigned to.
     */
    private void findIntervalForTask(PlannerTask task, Iterator<IInterval> possibleIterator,
                                     IntervalTree collisionTree, PriorityQueue<OccupiedInterval> options) {
        long minimalDuration = task.getMinSessionTimeInMillis();
        long minimalSlot = minimalDuration + spaceBetweenEvents;

        // Iterate over possible intervals.
        while (possibleIterator.hasNext()) {
            LongInterval possibleInterval = (LongInterval) possibleIterator.next();

            // Find first possible starting time in possible interval
            long startTime = getSpacedStartTime(possibleInterval);
            long endTime = possibleInterval.getEnd();

            // Check if tagged interval is long enough.
            long possibleDuration = endTime - startTime;
            if (possibleDuration < minimalSlot) {
                continue;
            }

            // Find collisions in possible interval.
            Collection<IInterval> collisions = mergeOverlapping(collisionTree.overlap(possibleInterval));
            if (collisions.isEmpty()) {
                // The tagged interval is free and its long enough so we can push here.
                options.add(new OccupiedInterval(startTime, endTime - spaceBetweenEvents));
            }

            //  Check if we can push task in between a pair of collision intervals.
            for (IInterval generic_collision : collisions) {
                LongInterval collision = (LongInterval) generic_collision;
                endTime = collision.getStart();
                if (endTime - startTime >= minimalSlot) {
                    // Found free interval before some event/task so we can push here.
                    options.add(new OccupiedInterval(startTime, endTime - spaceBetweenEvents));
                }
                startTime = collision.getEnd() + spaceBetweenEvents;
            }
        }
    }

    /**
     * Helper function: Returns the tag if it exists in this calendar. Return null if name is null or NO_TAG.
     */
    private PlannerTag safeGetTag(String tagName) {
        if (tagName == null || tagName.equals(PlannerObject.NO_TAG)) {
            return null;
        }
        return tags.get(tagName);
    }

    /**
     * Helper function: Returns true if the given date is not within the calendar's valid time interval.
     */
    private boolean isInvalidDate(long time) {
        return startTime > time || time > endTime;
    }

    // Inner classes

    /**
     * Iterator the iterates over the non-occupied time intervals in this calendar.
     */
    private class FreeTimeIterator implements Iterator<IInterval> {

        private final Iterator<IInterval> occupiedIt;
        private long startTime, lastEndTime;
        private LongInterval nextOccupied;
        private boolean oneMore;

        /**
         * Constructor for this iterator. Generates free time from the enclosing class' Interval Tree.
         */
        public FreeTimeIterator() {

            occupiedIt = occupiedTree.iterator();
            startTime = PlannerCalendar.this.startTime + spaceBetweenEvents;

            oneMore = true;
            if (occupiedIt.hasNext()) {
                nextOccupied = (LongInterval) occupiedIt.next();
            } else {
                lastEndTime = startTime;
                nextOccupied = null;
            }
        }

        /**
         * Returns true if the iteration has more elements.
         */
        @Override
        public boolean hasNext() {
            return nextOccupied != null || oneMore;
        }

        // On-the-fly interval merging

        /**
         * Helper function: On-the-fly interval merging of free time intervals.
         */
        private LongInterval getNextOccupied() {
            LongInterval previous = nextOccupied;
            if (!occupiedIt.hasNext()) {
                nextOccupied = null;
                lastEndTime = previous.getEnd() + spaceBetweenEvents;
                return previous;
            }

            while (occupiedIt.hasNext()) {
                nextOccupied = (LongInterval) occupiedIt.next();
                if (nextOccupied.irAfter(nextOccupied)) {
                    return previous;
                } else {
                    previous = new LongInterval(previous.getStart(), nextOccupied.getEnd());
                }
            }

            return nextOccupied;
        }

        /**
         * Returns the next element in the iteration.
         */
        @Override
        public IInterval next() {
            while (true) {
                if (nextOccupied == null) {
                    if (oneMore) {
                        oneMore = false;
                        return new LongInterval(lastEndTime, PlannerCalendar.this.endTime);
                    }
                    return null;
                }

                LongInterval current = getNextOccupied();
                if (current == null) {
                    return null;
                }

                long endTime = current.getStart() - MIN_SPACE_IN_MILLIS;
                if (endTime > startTime) {
                    LongInterval free = new LongInterval(startTime, endTime);
                    startTime = current.getEnd() + spaceBetweenEvents;
                    return free;
                }
            }
        }
    }

    /**
     * Closed interval that contains an event.
     */
    static class OccupiedInterval extends LongInterval {

        private static long maxSessionTime = 120 * 60000L;
        private static long next_id = 0;

        public PlannerEvent event;
        private final long id;
        private final boolean isPreferred;

        /**
         * Create an interval with no start or end time and no event.
         */
        public OccupiedInterval() {
            super();
            this.event = null;
            this.id = next_id++;
            this.isPreferred = false;
        }

        /**
         * Create the closed interval [event.getStartTime(), event.getEndTime()] that point to the given event.
         */
        public OccupiedInterval(PlannerEvent event) throws IllegalTimeInterval, IllegalTimePoint {
            super(event.getStartTime(), event.getEndTime(), false, false);
            this.event = event;
            this.id = next_id++;
            this.isPreferred = false;
        }

        /**
         * Create the closed interval [startTime, endTime] that point to the given event.
         */
        public OccupiedInterval(long startTime, long endTime) throws IllegalTimeInterval, IllegalTimePoint {
            super(startTime, endTime, false, false);
            this.event = null;
            this.id = next_id++;
            this.isPreferred = false;
        }

        /**
         * Create the closed interval [startTime, endTime] that point to the given event.
         */
        public OccupiedInterval(long startTime, long endTime, long id, boolean isPreferred) throws IllegalTimeInterval, IllegalTimePoint {
            super(startTime, endTime, false, false);
            this.event = null;
            this.id = id;
            this.isPreferred = isPreferred;
        }

        public long getId() {
            return id;
        }

        public boolean isPreferred() {
            return isPreferred;
        }

        public static long getMaxSessionTime() {
            return maxSessionTime;
        }

        public static void setMaxSessionTime(long maxSessionTime) {
            OccupiedInterval.maxSessionTime = maxSessionTime;
        }

        /**
         * Returns true if both intervals are equal and both events are equal.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            OccupiedInterval that = (OccupiedInterval) o;
            return event.equals(that.event);
        }
    }

    /**
     * Comparator that is used to implement max-heap according to status, length and id.
     */
    static class IntervalByLengthComparator implements Comparator<OccupiedInterval> {

        @Override
        public int compare(OccupiedInterval o1, OccupiedInterval o2) {
            // First make sure to always choose preferred interval.
            int preferredComparison = Boolean.compare(o2.isPreferred(), o1.isPreferred());
            if (preferredComparison != 0) {
                return preferredComparison;
            }

            // If both have the same status then decide according to length (length over the max session time is irrelevant).
            long comparison = length(o2) - length(o1);
            if (comparison != 0) {
                return Long.signum(comparison);
            }
            return Long.signum(o1.getId() - o2.getId()); // Tie-breaking by creation order.
        }

        private long length(OccupiedInterval o) {
            long actualLength = o.getEnd() - o.getStart();
            return Math.min(actualLength, OccupiedInterval.getMaxSessionTime());
        }
    }

    /**
     * Comparator that is used to sort tasks for insertion (done according to deadline, priority and duration).
     */
    static class TaskByDeadlineComparator implements Comparator<PlannerTask> {

        @Override
        public int compare(PlannerTask task1, PlannerTask task2) {
            // First try to order by deadline. (earliest first)
            int comparison = Long.compare(task1.getDeadline(), task2.getDeadline());
            if (comparison != 0) {
                return comparison;
            }

            // If they share the same deadline then order by priority. (biggest first)
            comparison = Integer.compare(task2.getPriority(), task1.getPriority());
            if (comparison != 0) {
                return comparison;
            }

            // If they share the same priority then order by duration length. (biggest first)
            return Integer.compare(task2.getDurationInMinutes(), task1.getDurationInMinutes());
        }
    }
}
