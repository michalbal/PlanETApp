package net.planner.planetapp.planner;

import net.planner.planetapp.UserPreferencesManager;
import net.planner.planetapp.database.DBmanager;
import net.planner.planetapp.networking.MoodleCommunicator;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class TasksManager {
    private String token;
    private MoodleCommunicator connector;
    private DBmanager dBmanager;
    private ArrayList<String> unwantedCourseIds;
    private ArrayList<String> unwantedTaskIds;
    private static TasksManager tasksManager;
    private HashMap<String, String> courseNames;
    private HashMap<String, String> preferences;

    private TasksManager(){
        connector = new MoodleCommunicator();
        unwantedCourseIds = new ArrayList<>();
        unwantedTaskIds = new ArrayList<>();
        courseNames = new HashMap<>();
        preferences = new HashMap<>();
        token = UserPreferencesManager.INSTANCE.getUserMoodleToken();
    }

    public void initTasksManager(String username, String password) throws ClientProtocolException, IOException, JSONException {
        token = connector.connectToCSEMoodle(username, password);
        dBmanager = new DBmanager(username);
        dBmanager.readUnwantedCourses();
        dBmanager.readUnwantedTasks();
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

    public HashMap<String, String> getPreferences() {
        return preferences;
    }

    public void addPreference(String courseID, String preferenceId, Boolean writeToDb) {
        preferences.put(courseID, preferenceId);
        if (writeToDb) {
            dBmanager.addMoodleCoursePreference(courseID, preferenceId);
        }
    }

    public HashMap<String, String> parseMoodleCourses() {
        if (token != null && !token.equals("")) {
            HashMap<String, String> parsedCourseNames = connector.parseFromMoodle(token, true);
            dBmanager.addUserMoodleCourses(parsedCourseNames);
            return parsedCourseNames;
        }
        return null;
    }

    public LinkedList<PlannerTask> parseMoodleTasks(long currentTime) {
        // TODO normal moodle as an option as well
        LinkedList<PlannerTask> filteredTasks = new LinkedList<>();
        if (token != null && !token.equals("")) {
            HashMap<String, LinkedList<PlannerTask>> parsedAssignments = connector.parseFromMoodle(
                    token, false);

            for (HashMap.Entry<String, LinkedList<PlannerTask>> parsedAssignment : parsedAssignments
                    .entrySet()) {
                if (unwantedCourseIds.contains(parsedAssignment.getKey())) {
                    continue;
                }

                for (PlannerTask task : parsedAssignment.getValue()) {
                    if (!unwantedTaskIds.contains(task.getMoodleId()) &&
                        task.getDeadline() > currentTime) {
                        if (preferences.containsKey(task.getCourseId())){
                            task.setTagName(preferences.get(task.getCourseId()));
                        }
                        filteredTasks.add(task);
                    }
                }
            }
        }
        return filteredTasks;
    }

    public LinkedList<PlannerEvent> planSchedule(LinkedList<PlannerTask> plannerTasks) {
        dBmanager.writeAcceptedTasks(plannerTasks);

        LinkedList<PlannerEvent> subtasks = null;
        //TODO run the algorithm
        return subtasks;
    }

    public void processUserAcceptedSubtasks(LinkedList<PlannerEvent> acceptedEvents) {
        //TODO write to GC, get IDs and update them in the events (will be used in db)

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
