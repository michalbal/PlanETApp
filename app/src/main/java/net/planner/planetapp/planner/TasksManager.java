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
    private HashMap<String, String> coursePreferences;
    private ArrayList<PlannerTag> preferences;
    private ArrayList<PlannerTask> tasks;
    private ArrayList<String> taskInDBids;

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
    }

    public void initTasksManager(String username, String password) throws ClientProtocolException, IOException, JSONException {
        token = connector.connectToCSEMoodle(username, password);
        dBmanager = new DBmanager(username);
//        dBmanager.readTasks();
//        dBmanager.readPreferences();
//        dBmanager.readUnwantedCourses();
//        dBmanager.readUnwantedTasks();
//        //@TODO finish all of the previous before running this one
//        dBmanager.readUserMoodleCourses();
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
        dBmanager.addUserMoodleCourses(courses);
    }

    public LinkedList<PlannerTask> parseMoodleTasks(long currentTime) {
        LinkedList<PlannerTask> filteredTasks = new LinkedList<>();
        if (token != null && !token.equals("")) {
            HashMap<String, LinkedList<PlannerTask>> parsedAssignments = connector.parseFromMoodle(
                    token, false);
            // TODO add tasks to local db

            for (HashMap.Entry<String, LinkedList<PlannerTask>> parsedAssignment : parsedAssignments
                    .entrySet()) {
                if (unwantedCourseIds.contains(parsedAssignment.getKey()) ||
                    taskInDBids.contains(parsedAssignment.getKey())) {
                    continue;
                }

                for (PlannerTask task : parsedAssignment.getValue()) {
                    if (!unwantedTaskIds.contains(task.getMoodleId()) &&
                        task.getDeadline() > currentTime) {
                        if (coursePreferences.containsKey(task.getCourseId())){
                            task.setTagName(coursePreferences.get(task.getCourseId()));
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
        tasks.addAll(plannerTasks);
        // TODO add all subtasks to the task in local db

        LinkedList<PlannerEvent> subtasks = null;
        //TODO run the algorithm
        return subtasks;
    }

    public void addTaskFromDB(PlannerTask task) {
        // TODO update task in local db
        tasks.add(task);
        taskInDBids.add(task.getMoodleId());
    }

    public void removeTask(PlannerTask task) {
        // TODO remove from GC if needed? Michal - No need
        // TODO remove task from local db
        tasks.remove(task);
        dBmanager.deleteTask(task);
    }

    public void processUserAcceptedSubtasks(LinkedList<PlannerEvent> acceptedEvents) {
        //TODO write to GC, get IDs and update them in the events (will be used in db)
        // Update subtasks in local db

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
