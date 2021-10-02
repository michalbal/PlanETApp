package net.planner.planetapp.planner;

import android.util.Log;

import net.planner.planetapp.database.TaskDB;
import net.planner.planetapp.planner.PlannerCalendar.OccupiedInterval;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

public class PlannerTask extends PlannerObject {
    private static final String TAG = "PlannerTask";
    private static final int DEFAULT_MIN_SESSION_IN_MINUTES = 30;

    private String moodleId;
    private String courseId;
    private long deadline;
    protected int priority; //1-10
    private int maxSessionTimeInMinutes;
    private int minSessionTimeInMinutes;
    private int maxDivisionsNumber;
    private int durationInMinutes;
    private List<PlannerEvent> childEvents;

    /**
     * Create PlannerTask object from its title, deadline and duration
     **/
    public PlannerTask(String title, long deadline, int durationInMinutes) {
        super(title);
        if (deadline < 0) {
            Log.e(TAG, "Invalid deadline");
            return;
        }
        if (durationInMinutes <= 0) {
            Log.e(TAG, "Invalid duration");
            return;
        }
        this.deadline = deadline;
        this.maxSessionTimeInMinutes = 120; // 2 hours as a default
        this.minSessionTimeInMinutes = DEFAULT_MIN_SESSION_IN_MINUTES;
        this.maxDivisionsNumber = 3;
        this.priority = 5;
        childEvents = null;

        if (durationInMinutes < minSessionTimeInMinutes) {
            this.durationInMinutes = DEFAULT_MIN_SESSION_IN_MINUTES;
        } else {
            this.durationInMinutes = durationInMinutes;
        }
    }

    public PlannerTask(TaskDB taskDB) {
        super(taskDB.getName(), taskDB.getDescription(), taskDB.getExclusiveForItsTimeSlot(),
              taskDB.getTag(), taskDB.getLocation());
        this.moodleId = taskDB.getTaskId();
        this.courseId = taskDB.getCourseID();
        this.reminder = taskDB.getReminder();
        this.deadline = taskDB.getDeadline();
        this.priority = taskDB.getPriority();
        this.maxSessionTimeInMinutes = taskDB.getMaxSessionTimeInMinutes();
        this.maxDivisionsNumber = taskDB.getMaxDivisionsNumber();
        this.durationInMinutes = taskDB.getDurationInMinutes();
    }

    // validity check

    /**
     * Input parameters validity check
     **/
    public static boolean isValid(int reminder, int priority, long deadline,
                                  int durationInMinutes, int minSessionTimeInMinutes,
                                  int maxSessionTimeInMinutes, int maxDivisionsNumber) {
        if (!PlannerObject.isValid(reminder)) {
            return false;
        }
        if (priority < 1 || priority > 10) {
            Log.e(TAG, "Validation error: Priority should be an integer from 1 to 10");
            return false;
        }

        if (durationInMinutes <= 0) {
            Log.e(TAG, "Validation error: invalid duration");
            return false;
        }

        if (maxDivisionsNumber < 1) {
            Log.e(TAG, "Validation error: At least one instance of task should be allowed");
            return false;
        }

        if (minSessionTimeInMinutes < DEFAULT_MIN_SESSION_IN_MINUTES) {
            Log.e(TAG, "Validation error: Minimal session time has to be at least " + DEFAULT_MIN_SESSION_IN_MINUTES + " min");
            return false;
        }

        if (maxSessionTimeInMinutes < minSessionTimeInMinutes) {
            Log.e(TAG, "Validation error: Maximal session time has to be at least as big as minimal session time");
            return false;
        }

        if (maxDivisionsNumber * maxSessionTimeInMinutes < durationInMinutes) {
            Log.e(TAG, "Validation error: Under the constraints duration is unattainable");
            return false;
        }

        return true;
    }

    // methods

    /**
     * Get deadline for the task in milliseconds
     **/
    public long getDeadline() {
        return deadline;
    }

    public boolean setDeadline(long deadline) {
        if (deadline < 0) {
            Log.e(TAG, "Invalid deadline");
            return false;
        }
        this.deadline = deadline;
        return true;
    }

    /**
     * Get priority of the task
     **/
    public int getPriority() {
        return priority;
    }

    /**
     * Set priority of the task as an integer from 1 to 10
     **/
    public boolean setPriority(int priority) {
        if (priority < 1 || priority > 10) {
            Log.e("PlannerObject", "Illegal priority: Priority is an integer from 1 to 10");
            return false;
        }
        this.priority = priority;
        return true;
    }

    /**
     * Get the maximum allowed length of session in minutes
     **/
    public int getMaxSessionTimeInMinutes() {
        return maxSessionTimeInMinutes;
    }

    /**
     * Get the maximum allowed length of session in milliseconds
     **/
    public long getMaxSessionTimeInMillis() {
        return maxSessionTimeInMinutes * 60000L;
    }

    /**
     * Set the maximum allowed length of session in minutes
     **/
    public boolean setMaxSessionTimeInMinutes(int maxSessionTimeInMinutes) {
        if (maxSessionTimeInMinutes < minSessionTimeInMinutes) {
            Log.e(TAG, "Maximal session time has to at least as big as the minimal session time");
            return false;
        }
        this.maxSessionTimeInMinutes = maxSessionTimeInMinutes;
        return true;
    }

    /**
     * Get the minimum allowed length of session in minutes
     **/
    public int getMinSessionTimeInMinutes() {
        return minSessionTimeInMinutes;
    }

    /**
     * Get the minimum allowed length of session in milliseconds
     **/
    public long getMinSessionTimeInMillis() {
        return minSessionTimeInMinutes * 60000L;
    }

    /**
     * Set the minimum allowed length of session in minutes
     **/
    public boolean setMinSessionTimeInMinutes(int minSessionTimeInMinutes) {
        if (minSessionTimeInMinutes < DEFAULT_MIN_SESSION_IN_MINUTES || minSessionTimeInMinutes > maxSessionTimeInMinutes) {
            Log.e(TAG, "Maximal session time has to be at least" + DEFAULT_MIN_SESSION_IN_MINUTES +
                    "minutes and at most as big as the maximal session time");
            return false;
        }
        this.minSessionTimeInMinutes = minSessionTimeInMinutes;
        return true;
    }

    /**
     * Get the maximum number of instances this task may have
     **/
    public int getMaxDivisionsNumber() {
        return maxDivisionsNumber;
    }

    /**
     * Set the maximum number of instances this task may have (min 1)
     **/
    public boolean setMaxDivisionsNumber(int maxDivisionsNumber) {
        if (maxDivisionsNumber < 1) {
            Log.e(TAG, "At least one instance of task should be allowed");
            return false;
        }
        this.maxDivisionsNumber = maxDivisionsNumber;
        return true;
    }

    /**
     * Get this task's duration in minutes
     **/
    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    /**
     * Get this task's duration in milliseconds
     **/
    public long getDurationInMillis() {
        return durationInMinutes * 60000L;
    }

    /**
     * Set this task's duration in minutes
     **/
    public boolean setDurationInMinutes(int durationInMinutes) {
        if (durationInMinutes < 0) {
            Log.e(TAG, "Invalid task duration");
            return false;
        }
        this.durationInMinutes = durationInMinutes;
        return true;
    }

    /**
     * Get this task's child events (the events this tasks was split to)
     **/
    public List<PlannerEvent> getChildEvents() {
        return childEvents;
    }

    /**
     * Splits this task into events from the given option intervals.
     **/
    public List<PlannerEvent> splitIntoEvents(PriorityQueue<OccupiedInterval> options, long spaceBetweenEvents) {
        LinkedList<PlannerEvent> children = new LinkedList<>();
        PriorityQueue<OccupiedInterval> tempOptions = new PriorityQueue<>(options);

        long maxSessionTime = getMaxSessionTimeInMillis();
        long minSlotLength = getMinSessionTimeInMillis() + spaceBetweenEvents;
        long leftToFill = getDurationInMillis();

        while (true) {
            OccupiedInterval current = tempOptions.poll();
            if (current == null) {
                return new LinkedList<>();
            }

            // Take
            long startTime = current.getStart();
            long endTime = current.getEnd();
            long intervalLength = current.getEnd() - startTime;
            long lengthToTake = Math.min(Math.min(intervalLength, maxSessionTime), leftToFill);

            long newEndTime = startTime + lengthToTake;
            children.add(new PlannerEvent(this, startTime, newEndTime));

            // Check if done.
            leftToFill -= lengthToTake;
            if (leftToFill <= 0L) {
                return children;
            }

            // If not done then check if we can still use the rest of current.
            long remainingTimeInInterval = intervalLength - lengthToTake;
            if (remainingTimeInInterval >= minSlotLength) {
                tempOptions.add(new OccupiedInterval(
                        newEndTime + spaceBetweenEvents, endTime, current.getId(), current.isPreferred()));
            }
        }
    }

    @NotNull
    @Override
    public String toString() {
        String stringRep = super.toString();
        if (this.priority != -1) {
            stringRep += "; Priority: " + this.priority + "/10";
        }
        stringRep += "; Deadline is " + new Date(this.deadline) +
                "; Maximal time of one session (if divided) is " + maxSessionTimeInMinutes +
                "; Maximal number of divisions (if divided) is " + maxDivisionsNumber +
                "; Expected duration of the task in minutes is: " + durationInMinutes;
        return stringRep + ".";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlannerTask)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PlannerTask that = (PlannerTask) o;
        return getDeadline() == that.getDeadline() &&
                getMaxSessionTimeInMinutes() == that.getMaxSessionTimeInMinutes() &&
                getDurationInMinutes() == that.getDurationInMinutes() &&
                getMaxDivisionsNumber() == that.getMaxDivisionsNumber() &&
                getPriority() == that.getPriority();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDurationInMinutes(), getDeadline(),
                getMaxSessionTimeInMinutes(), getMaxDivisionsNumber(), getPriority());
    }

    public String getMoodleId() {
        return moodleId;
    }

    public void setMoodleId(String moodleId) {
        this.moodleId = moodleId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
}
