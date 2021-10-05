package net.planner.planetapp;

import static net.planner.planetapp.UtilsKt.FRIDAY;
import static net.planner.planetapp.UtilsKt.MONDAY;
import static net.planner.planetapp.UtilsKt.SATURDAY;
import static net.planner.planetapp.UtilsKt.SUNDAY;
import static net.planner.planetapp.UtilsKt.THURSDAY;
import static net.planner.planetapp.UtilsKt.TUESDAY;
import static net.planner.planetapp.UtilsKt.WEDNESDAY;

import net.planner.planetapp.networking.GoogleCalenderCommunicator;
import net.planner.planetapp.planner.PlannerCalendar;
import net.planner.planetapp.planner.PlannerEvent;
import net.planner.planetapp.planner.PlannerObject;
import net.planner.planetapp.planner.PlannerTag;
import net.planner.planetapp.planner.PlannerTask;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import kotlin.Pair;

public class CalendarInsertionTest {

    @Test
    public void noGoogleCalendarGroupOfTasks() throws ParseException {
        // Create general preference with all course names
        HashMap<String, ArrayList<String>> forbiddenSettings = new HashMap<>();
        ArrayList<String> allDays = new ArrayList<>(Arrays.asList(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY));
        forbiddenSettings.put("00:00-08:30", allDays);
        forbiddenSettings.put("23:00-23:59", allDays);
        HashMap<String, ArrayList<String>> preferredSettings = new HashMap<>();
        PlannerTag generalTag = new PlannerTag(PlannerObject.GENERAL_TAG, 9 ,forbiddenSettings, preferredSettings);

        ArrayList<PlannerTag> preferences = new ArrayList<>();
        preferences.add(generalTag);

        List<PlannerTask> plannerTasks = getPlannerTasks(generalTag);

        LinkedList<PlannerEvent> subtasks = planSchedule(plannerTasks, preferences);

        for (PlannerEvent subtask: subtasks) {
            System.out.println(subtask);
        }

    }

    private List<PlannerTask> getPlannerTasks(PlannerTag generalTag) throws ParseException {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd H:mm");
        LinkedList<PlannerTask> tasks = new LinkedList<>();
        PlannerTask newTask;
        long deadline;

        deadline = Objects.requireNonNull(ft.parse("2021-05-13 22:00")).getTime();
        newTask = new PlannerTask("First deadline", deadline, 4 * 60);
        newTask.setTagName((PlannerObject.GENERAL_TAG));
        tasks.add(newTask);

        deadline = Objects.requireNonNull(ft.parse("2021-05-15 18:00")).getTime();
        newTask = new PlannerTask("Third deadline", deadline, 3 * 60);
        newTask.setTagName((PlannerObject.GENERAL_TAG));
        tasks.add(newTask);

        deadline = Objects.requireNonNull(ft.parse("2021-05-13 23:59")).getTime();
        newTask = new PlannerTask("Second deadline", deadline, 6 * 60);
        newTask.setTagName((PlannerObject.GENERAL_TAG));
        tasks.add(newTask);

        deadline = Objects.requireNonNull(ft.parse("2021-05-16 16:00")).getTime();
        newTask = new PlannerTask("Fourth deadline", deadline, 2 * 60);
        newTask.setTagName((PlannerObject.GENERAL_TAG));
        tasks.add(newTask);

        return tasks;
    }

    private LinkedList<PlannerEvent> planSchedule(List<PlannerTask> plannerTasks, List<PlannerTag> preferences) throws ParseException {
        System.out.println("planSchedule called");
        ArrayList<PlannerEvent> events = new ArrayList<>();

        // Get start and end times.
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd H:mm");
        long startTime = Objects.requireNonNull(ft.parse("2021-05-13 06:00")).getTime();
        long endTime = Objects.requireNonNull(ft.parse("2021-05-20 06:00")).getTime();

        // Get user Settings for scheduling
        long spaceBetweenEvents = PlannerCalendar.RECOMMENDED_SPACE_IN_MILLIS;
        PlannerCalendar calendar = new PlannerCalendar(startTime, endTime, spaceBetweenEvents, events, preferences);

        LinkedList<PlannerEvent> subtasks = new LinkedList<>();
        subtasks = calendar.insertTasks(plannerTasks);
        return subtasks;
    }

}