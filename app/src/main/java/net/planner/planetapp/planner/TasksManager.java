package net.planner.planetapp.planner;

import net.planner.planetapp.database.DBmanager;
import net.planner.planetapp.networking.MoodleCommunicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class TasksManager {
    private final String token;
    private final MoodleCommunicator connector;
    private final DBmanager dBmanager;
    private ArrayList<String> unwantedCourseIds;
    private ArrayList<String> unwantedTaskIds;

    public TasksManager(String username, String password) {
        connector = new MoodleCommunicator();
        token = connector.connectToCSEMoodle(username, password);
        dBmanager = new DBmanager(username);
        dBmanager.getUnwantedCourses(this);
        dBmanager.getUnwantedTasks(this);
    }

    public void setUnwantedCourseIds(ArrayList<String> unwantedCourseIds) {
        this.unwantedCourseIds = unwantedCourseIds;
    }

    public void setUnwantedTaskIds(ArrayList<String> unwantedTaskIds) {
        this.unwantedTaskIds = unwantedTaskIds;
    }

    public HashMap<String, String> parseMoodleCourses() {
        if (token != null && !token.equals("")) {
            HashMap<String, String> parsedCourseNames = connector.parseFromMoodle(token, true);
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
            addTaskToUnwanted(course);
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
