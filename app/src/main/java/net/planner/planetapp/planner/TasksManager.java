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

    public HashMap<String, LinkedList<PlannerTask>> getMoodleCourses() {
        if (token != null && !token.equals("")) {
            HashMap<String, LinkedList<PlannerTask>> parsedCourseNames = connector.parseAssignments(
                    token, true);

//            for (HashMap.Entry<String, LinkedList<PlannerTask>> parsedCourseName : parsedCourseNames
//                    .entrySet()) {
//                System.out.print(parsedCourseName.getKey() + "\t-\t");
//                System.out.println(parsedCourseName.getValue());
//            }
            return parsedCourseNames;
        }
        return null;
    }

    public HashMap<String, LinkedList<PlannerTask>> getMoodleTasks() {
        // TODO normal moodle as an option as well
        if (token != null && !token.equals("")) {
            HashMap<String, LinkedList<PlannerTask>> parsedAssignments = connector.parseAssignments(
                    token, false);

//            for (HashMap.Entry<String, LinkedList<PlannerTask>> parsedAssignment : parsedAssignments
//                    .entrySet()) {
//                System.out.print(parsedAssignment.getKey() + "\t-\t");
//                System.out.println(parsedAssignment.getValue());
//            }
            return parsedAssignments;
        } else {
            // TODO handle couldn't log in
        }
        return null;
    }

}
