package net.planner.planetapp.planner;

import com.google.android.gms.common.api.internal.ListenerHolder;

import net.planner.planetapp.App;
import net.planner.planetapp.IOnPlanCalculatedListener;
import net.planner.planetapp.IOnTasksReceivedListener;
import net.planner.planetapp.UserPreferencesManager;
import net.planner.planetapp.UtilsKt;
import net.planner.planetapp.database.DBmanager;
import net.planner.planetapp.database.local_database.LocalDBManager;
import net.planner.planetapp.database.local_database.PreferencesLocalDB;
import net.planner.planetapp.networking.GoogleCalenderCommunicator;
import net.planner.planetapp.networking.MoodleCommunicator;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.planner.planetapp.UtilsKt.FRIDAY;
import static net.planner.planetapp.UtilsKt.MONDAY;
import static net.planner.planetapp.UtilsKt.SATURDAY;
import static net.planner.planetapp.UtilsKt.SUNDAY;
import static net.planner.planetapp.UtilsKt.TUESDAY;
import static net.planner.planetapp.UtilsKt.WEDNESDAY;
import static net.planner.planetapp.UtilsKt.THURSDAY;

public class TasksManager {
    private String token;
    private long furthestDeadline;
    private MoodleCommunicator connector;
    private DBmanager dBmanager;
    private ArrayList<String> unwantedCourseIds;
    private ArrayList<String> unwantedTaskIds;
    private static TasksManager tasksManager;
    private HashMap<String, String> courseNames;
    private HashMap<String, String> coursePreferences;
    private ArrayList<PlannerTag> preferences;
    private ArrayList<PlannerTask> tasks;
    private ArrayList<String> taskInDBids;

    // Notifiers on processes ending
    private ArrayList<IOnTasksReceivedListener> tasksReceivedListeners = new ArrayList<>();
    private ArrayList<IOnPlanCalculatedListener> planCalculatedListeners = new ArrayList<>();

    private TasksManager(){
        connector = new MoodleCommunicator();
        unwantedCourseIds = new ArrayList<>();
        unwantedTaskIds = new ArrayList<>();
        courseNames = new HashMap<>();
        coursePreferences = new HashMap<>();
        preferences = new ArrayList<>();
        tasks = new ArrayList<>();
        taskInDBids = new ArrayList<>();
        token = UserPreferencesManager.INSTANCE.getUserMoodleToken();
        if (token != null) {
            String userName = UserPreferencesManager.INSTANCE.getMoodleUserName();
            dBmanager = new DBmanager(userName);
            dBmanager.readTasks();
            dBmanager.readPreferences();
        }
        furthestDeadline = 0L;
    }

    public void initTasksManager(String username, String password) throws ClientProtocolException, IOException, JSONException {
        token = connector.connectToCSEMoodle(username, password);
        dBmanager = new DBmanager(username);
    }

    public Boolean addTasksReceivedListener(IOnTasksReceivedListener listener) {
        if (!tasksReceivedListeners.contains(listener)) {
            tasksReceivedListeners.add(listener);
            return true;
        }
        return false;
    }

    public Boolean removeTasksReceivedListener(IOnTasksReceivedListener listener) {
        if (!tasksReceivedListeners.contains(listener)) {
            tasksReceivedListeners.remove(listener);
            return true;
        }
        return false;
    }

    public Boolean addPlanCalculatedListener(IOnPlanCalculatedListener listener) {
        if (!planCalculatedListeners.contains(listener)) {
            planCalculatedListeners.add(listener);
            return true;
        }
        return false;
    }

    public Boolean removePlanCalculatedListener(IOnPlanCalculatedListener listener) {
        if (!planCalculatedListeners.contains(listener)) {
            planCalculatedListeners.remove(listener);
            return true;
        }
        return false;
    }

    public void pullDataFromDB(){
        // chain of listeners triggers reading of the blacklists and tasks from Moodle
        dBmanager.readUserMoodleCourses();
    }

    public static TasksManager getInstance(){
        if (tasksManager == null){
            tasksManager = new TasksManager();
        }
        return tasksManager;
    }

    public void setUnwantedCourseIds(ArrayList<String> unwantedCourseIds) {
        this.unwantedCourseIds = unwantedCourseIds;
    }

    public void setUnwantedTaskIds(ArrayList<String> unwantedTaskIds) {
        this.unwantedTaskIds = unwantedTaskIds;
    }

    public HashMap<String, String> getCourseNames() {
        return courseNames;
    }

    public void addMoodleCourse(String courseID, String courseName, Boolean writeToDb) {
        courseNames.put(courseID, courseName);
        if (writeToDb) {
            dBmanager.addMoodleCourseName(courseID, courseName);
        }
    }

    public void addCoursePreference(String courseID, String preferenceId, Boolean writeToDb) {
        // Currently used when retrieving from DB
        coursePreferences.put(courseID, preferenceId);
        if (writeToDb) {
            dBmanager.addMoodleCoursePreference(courseID, preferenceId);
        }
    }

    public HashMap<String, String> getCoursePreferences() {
        return coursePreferences;
    }

    public void addPreferenceTag(PlannerTag plannerTag, Boolean  writeToDb) {
        preferences.add(plannerTag);
        if (writeToDb) {
            dBmanager.addPreference(plannerTag);
        }
    }

    public HashMap<String, String> parseMoodleCourses() {
        if (token != null && !token.equals("")) {
            HashMap<String, String> parsedCourseNames = connector.parseFromMoodle(token, true);
            return parsedCourseNames;
        }
        return null;
    }

    public void saveChosenMoodleCourses(HashMap<String, String> courses) {
        // Create general preference with all course names
        HashMap<String, ArrayList<String>> forbiddenSettings = new HashMap();
        ArrayList allDays = new ArrayList(Arrays.asList(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY));
        forbiddenSettings.put("00:00-08:30", allDays);
        forbiddenSettings.put("23:00-23:59", allDays);
        HashMap<String, ArrayList<String>> preferredSettings = new HashMap();
        PlannerTag generalTag = new PlannerTag(PlannerObject.GENERAL_TAG, 9 ,forbiddenSettings, preferredSettings);

        // Save tag in local DB
        PreferencesLocalDB preference = new PreferencesLocalDB(PlannerObject.GENERAL_TAG, 9, forbiddenSettings, preferredSettings, courses.keySet());
        LocalDBManager.INSTANCE.insertOrUpdatePreference(preference);

        // Save courses in both firebase db and local db
        dBmanager.addUserMoodleCourses(courses);

        // Save general tag in firebase db
        addPreferenceTag(generalTag, true);
    }

    public LinkedList<PlannerTask> parseMoodleTasks(long currentTime) {
        LinkedList<PlannerTask> filteredTasks = new LinkedList<>();
        if (token != null && !token.equals("")) {
            HashMap<String, LinkedList<PlannerTask>> parsedAssignments = connector.parseFromMoodle(
                    token, false);

            for (HashMap.Entry<String, LinkedList<PlannerTask>> parsedAssignment : parsedAssignments
                    .entrySet()) {
                if (unwantedCourseIds.contains(parsedAssignment.getKey()) ||
                    taskInDBids.contains(parsedAssignment.getKey())) {
                    continue;
                }

                for (PlannerTask task : parsedAssignment.getValue()) {
                    Boolean isTaskInDb = LocalDBManager.INSTANCE.isTaskInDbAndUnchanged(task.getMoodleId(), task.getDeadline());
                    if (!unwantedTaskIds.contains(task.getMoodleId())
                            && task.getDeadline() > currentTime
                            && !isTaskInDb) {

                        String tagName = findTagOfCourse(task.getCourseId());
                        task.setTagName(tagName);

                        // Set expected duration and max session time from settings
                        task.setDurationInMinutes((int) UserPreferencesManager.INSTANCE.getAvgTaskDurationMinutes());
                        task.setMaxSessionTimeInMinutes((int) UserPreferencesManager.INSTANCE.getPreferredSessionTime());

                        filteredTasks.add(task);
                        if (task.getDeadline() > furthestDeadline){
                            furthestDeadline = task.getDeadline();
                        }
                        // Add task to local db
                        LocalDBManager.INSTANCE.insertOrUpdateTask(task);
                    }
                }
            }
        }

        // Inform task listeners that new tasks arrived
        for(IOnTasksReceivedListener listener : tasksReceivedListeners) {
            listener.onTasksReceivedFromMoodle(filteredTasks);
        }

        return filteredTasks;
    }

    private String findTagOfCourse(String courseName) {
        for(PreferencesLocalDB preference : LocalDBManager.INSTANCE.getDbLocalPreferencesData().getValue()) {
            if (preference.getCourses().contains(courseName)) {
                return preference.getTagName();
            }
        }
        return PlannerObject.GENERAL_TAG;
    }


    public LinkedList<PlannerEvent> planSchedule(List<PlannerTask> plannerTasks) {
        dBmanager.writeAcceptedTasks(plannerTasks);
        tasks.addAll(plannerTasks);

        ArrayList<PlannerEvent> events = new ArrayList<>();

        // TODO startTime is specific to demo
        long startTime = 1602968400000L;
        long endTime = furthestDeadline;

        // Get events from google calendar
        Collection<PlannerEvent> userEvents = GoogleCalenderCommunicator.INSTANCE.getUserEvents(App.context, startTime, endTime);
        events.addAll(userEvents);

        // Get user Settings for scheduling
        long spaceBetweenEvents = TimeUnit.MINUTES.toMillis(UserPreferencesManager.INSTANCE.getSpaceBetweenEventsMinutes());
        // TODO get preferences from Local DB here, will probably be faster

        PlannerCalendar calendar = new PlannerCalendar(startTime, endTime, spaceBetweenEvents, events, preferences);

        LinkedList<PlannerEvent> subtasks = new LinkedList<>();

        subtasks = calendar.insertTasks(plannerTasks);

        // Notify listeners that planing the schedule finished
        // TODO maybe even notifications manager
        for(IOnPlanCalculatedListener listener: planCalculatedListeners) {
            listener.onPlanCalculated(subtasks);
        }

        return subtasks;
    }

    public void addTaskFromDB(PlannerTask task) {
        // TODO update task in local db
        tasks.add(task);
        taskInDBids.add(task.getMoodleId());
    }

    public void removeTask(PlannerTask task) {
        LocalDBManager.INSTANCE.deleteTask(task.getMoodleId());
        tasks.remove(task);
        dBmanager.deleteTask(task);
    }

    public void processUserAcceptedSubtasks(LinkedList<PlannerEvent> acceptedEvents) {

        for(PlannerEvent subtask : acceptedEvents){
            // Write to selected Google Calendar and get Id
            Long eventId = GoogleCalenderCommunicator.INSTANCE.insertEvent(App.context, subtask);
            subtask.setEventId(eventId);

            // Add event to Task subtask dates in Local DB
            LocalDBManager.INSTANCE.updateTaskSubtasks(subtask.getParentTaskId(), UtilsKt.getDate(subtask.getStartTime()));
        }
        // write to db
        dBmanager.writeNewSubtasks(acceptedEvents);
    }

    public void addTasksToUnwanted(LinkedList<PlannerTask> tasks){
        for (PlannerTask task : tasks){
            addTaskToUnwanted(task.getMoodleId());
        }
    }

    public void addCoursesToUnwanted(LinkedList<String> courseIds){
        for (String course : courseIds){
            addCourseToUnwanted(course);
        }
    }

    public void addTaskToUnwanted(String moodleTaskId) {
        unwantedTaskIds.add(moodleTaskId);
        dBmanager.addUnwantedTask(moodleTaskId);
    }

    public void removeTaskFromUnwanted(String moodleTaskId) {
        unwantedTaskIds.remove(moodleTaskId);
        dBmanager.removeUnwantedTask(moodleTaskId);
    }

    public void addCourseToUnwanted(String courseId) {
        unwantedCourseIds.add(courseId);
        dBmanager.addUnwantedCourse(courseId);
    }

    public void removeCourseFromUnwanted(String courseId) {
        unwantedCourseIds.remove(courseId);
        dBmanager.removeUnwantedCourse(courseId);
    }
}
