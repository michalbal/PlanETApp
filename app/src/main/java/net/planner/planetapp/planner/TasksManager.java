package net.planner.planetapp.planner;

import net.planner.planetapp.networking.MoodleCommunicator;

import java.util.HashMap;
import java.util.LinkedList;

public class TasksManager {
    private final String token;
    private final MoodleCommunicator connector;

    public TasksManager(String username, String password) {
        connector = new MoodleCommunicator();
        token = connector.connectToCSEMoodle(username, password);
    }

    public TasksManager(String userToken) {
        connector = new MoodleCommunicator();
        token = userToken;
    }

    public HashMap<String, String> parseMoodleCourses() {
        if (token != null && !token.equals("")) {
            HashMap<String, String> parsedCourseNames = connector.parseFromMoodle(
                    token, true);

            for (HashMap.Entry<String, String> parsedCourseName : parsedCourseNames
                    .entrySet()) {
                System.out.print(parsedCourseName.getKey() + "\t-\t");
                System.out.println(parsedCourseName.getValue());
            }
            return parsedCourseNames;
        }
        return null;
    }

    public HashMap<String, LinkedList<PlannerTask>> parseMoodleTasks() {
        // TODO normal moodle as an option as well
        if (token != null && !token.equals("")) {
            HashMap<String, LinkedList<PlannerTask>> parsedAssignments = connector.parseFromMoodle(
                    token, false);

            for (HashMap.Entry<String, LinkedList<PlannerTask>> parsedAssignment : parsedAssignments
                    .entrySet()) {
                System.out.print(parsedAssignment.getKey() + "\t-\t");
                System.out.println(parsedAssignment.getValue());
            }
            // TODO remove unwanted courses and tasks
            return parsedAssignments;
        }
        return null;
    }

    public LinkedList<PlannerEvent> planSchedule(LinkedList<PlannerTask> plannerTasks){
        LinkedList<PlannerEvent> subtasks = null;
        //TODO run the algorithm
        return subtasks;
    }

    public void processUserAcceptedSubtasks(LinkedList<PlannerEvent> acceptedEvents){
        //TODO write to GC and to the db
    }

    public void addTaskToUnwanted(PlannerTask task){
        // TODO upd db
    }

    public void removeTaskFromUnwanted(PlannerTask task){
        // TODO upd db
    }
}
