package net.planner.planetapp.database;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import net.planner.planetapp.planner.PlannerEvent;
import net.planner.planetapp.planner.PlannerTask;
import net.planner.planetapp.planner.TasksManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class DBmanager {
    FirebaseFirestore db;
    String username;

    public DBmanager(String username) {
        db = FirebaseFirestore.getInstance();
        this.username = username;

        db.collection("users").document(username).set(
                Collections.singletonMap("username", username), SetOptions.merge());
    }

    public void writeAcceptedTasks(LinkedList<PlannerTask> acceptedTasks) {
        for (PlannerTask task : acceptedTasks) {
            TaskDB taskDB = new TaskDB(task.getMoodleId(), task.getTitle(), task.getCourseId(),
                                       task.getDescription(), task.getLocation(),
                                       task.isExclusiveForItsTimeSlot(), task.getReminder(),
                                       task.getTagName(), task.getDeadline(), task.getPriority(),
                                       task.getMaxSessionTimeInMinutes(),
                                       task.getMaxDivisionsNumber(), task.getDurationInMinutes());


            db.collection("users").document(username).collection("tasks").document(
                    task.getMoodleId()).set(taskDB, SetOptions.merge());
        }
    }

    public void writeNewSubtasks(LinkedList<PlannerEvent> acceptedEvents) {
        for (PlannerEvent subtask : acceptedEvents) {
            SubtaskDB subtaskDB = new SubtaskDB(Long.toString(subtask.getEventId()),
                                                subtask.getStartTime(), subtask.getEndTime(),
                                                subtask.isAllDay());
            String moodleId = subtask.getParentTaskID();

            db.collection("users").document(username).collection("tasks").document(moodleId)
                    .collection("subtasks").document(subtaskDB.getEventIdGC()).set(subtaskDB,
                                                                                   SetOptions
                                                                                           .merge());

        }
    }

    public void addMoodleCourseName(String courseId, String courseName) {
        HashMap<String, String> course = new HashMap<>();
        course.put("courseId", courseId);
        course.put("courseName", courseName);

        db.collection("users").document(username).collection("courses").document(courseId).set(
                course, SetOptions.merge());
    }

    public void addMoodleCoursePreference(String courseId, String preferenceTagId) {
        HashMap<String, String> course = new HashMap<>();
        course.put("courseId", courseId);
        course.put("preferenceTagId", preferenceTagId);

        db.collection("users").document(username).collection("courses").document(courseId).set(
                course, SetOptions.merge());
    }

    public void addUserMoodleCourses(HashMap<String, String> moodleCourses) {
        for (HashMap.Entry<String, String> parsedCourseName : moodleCourses.entrySet()) {
            addMoodleCourseName(parsedCourseName.getKey(), parsedCourseName.getValue());
        }
    }

    public void readUserMoodleCourses() {
        db.collection("users").document(username).collection("courses").get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                CourseDB courseDB = document.toObject(CourseDB.class);

                                TasksManager.getInstance().addMoodleCourse(courseDB.getCourseId(),
                                                                           courseDB.getCourseName(),
                                                                           false);
                                TasksManager.getInstance().addPreference(courseDB.getCourseId(),
                                                                         courseDB.getPreferenceTagId(),
                                                                         false);
                            }
                        }

                        if (TasksManager.getInstance().getCourseNames().size() == 0) {
                            Thread thread = new Thread(new Runnable() {
                                @Override public void run() {
                                    try {
                                        TasksManager.getInstance().parseMoodleCourses();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            thread.start();
                        }
                    }
                });

    }

    public void addUnwantedCourse(String courseId) {
        db.collection("users").document(username).update("unwantedCourses",
                                                         FieldValue.arrayUnion(courseId));
    }

    public void removeUnwantedCourse(String courseId) {
        db.collection("users").document(username).update("unwantedCourses",
                                                         FieldValue.arrayRemove(courseId));
    }

    public void addUnwantedTask(String moodleTaskId) {
        db.collection("users").document(username).update("unwantedTasks",
                                                         FieldValue.arrayUnion(moodleTaskId));
    }

    public void removeUnwantedTask(String moodleTaskId) {
        db.collection("users").document(username).update("unwantedTasks",
                                                         FieldValue.arrayRemove(moodleTaskId));
    }

    public void readUnwantedCourses() {
        DocumentReference docRef = db.collection("users").document(username);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserDB userDB = documentSnapshot.toObject(UserDB.class);
                if (userDB != null) {
                    TasksManager.getInstance().setUnwantedCourseIds(userDB.getUnwantedCourses());
                }
            }
        });
    }

    public void readUnwantedTasks() {
        DocumentReference docRef = db.collection("users").document(username);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserDB userDB = documentSnapshot.toObject(UserDB.class);
                if (userDB != null) {
                    TasksManager.getInstance().setUnwantedTaskIds(userDB.getUnwantedTasks());
                }
            }
        });
    }

}
