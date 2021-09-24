package net.planner.planetapp.database;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import net.planner.planetapp.planner.PlannerEvent;
import net.planner.planetapp.planner.PlannerTask;
import net.planner.planetapp.planner.TasksManager;

import java.util.ArrayList;
import java.util.LinkedList;

public class DBmanager {
    FirebaseFirestore db;
    String username;

    public DBmanager(String username){
        db = FirebaseFirestore.getInstance();
        this.username = username;

        db.collection("users").document(username).get().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                db.collection("users").document(username).set(
                        new UserDB(username, new ArrayList<>(), new ArrayList<>()), SetOptions.merge());
            }
        });
    }

    public void writeAcceptedTasks(LinkedList<PlannerTask> acceptedTasks){
        for (PlannerTask task : acceptedTasks) {
            TaskDB taskDB = new TaskDB(task.getMoodleId(), task.getTitle(),
                                       "course id", "course name",
                                       task.getDescription(), task.getLocation(),
                                       task.isExclusiveForItsTimeSlot(), task.getReminder(),
                                       task.getTagName(), task.getDeadline(), task.getPriority(),
                                       task.getMaxSessionTimeInMinutes(),
                                       task.getMaxDivisionsNumber(), task.getDurationInMinutes());


            db.collection("users").document(username).collection("tasks")
                    .document(task.getMoodleId()).set(taskDB, SetOptions.merge());
        }
    }
    public void writeNewSubtasks(LinkedList<PlannerEvent> acceptedEvents){
        for (PlannerEvent subtask : acceptedEvents) {
            SubtaskDB subtaskDB = new SubtaskDB(Long.toString(subtask.getEventId()),
                                                subtask.getStartTime(),
                                                subtask.getEndTime(), subtask.isAllDay());
            String moodleId = subtask.getParentTaskID();

            db.collection("users").document(username).collection("tasks")
                    .document(moodleId).collection("subtasks")
                    .document(subtaskDB.getEventIdGC()).set(subtaskDB, SetOptions.merge());

        }
    }

    public void addUnwantedCourse(String courseId){
        db.collection("users").document(username).update("unwantedCourses", FieldValue
                .arrayUnion(courseId));
    }

    public void removeUnwantedCourse(String courseId){
        db.collection("users").document(username).update("unwantedCourses", FieldValue
                .arrayRemove(courseId));
    }

    public void addUnwantedTask(String moodleTaskId){
        db.collection("users").document(username).update("unwantedTasks", FieldValue
                .arrayUnion(moodleTaskId));
    }

    public void removeUnwantedTask(String moodleTaskId){
        db.collection("users").document(username).update("unwantedTasks", FieldValue
                .arrayRemove(moodleTaskId));
    }

    public void getUnwantedCourses(TasksManager tasksManager){
        DocumentReference docRef = db.collection("users").document(username);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserDB userDB = documentSnapshot.toObject(UserDB.class);
                assert userDB != null;
                tasksManager.setUnwantedCourseIds(userDB.getUnwantedCourses());
            }
        });
    }

    public void getUnwantedTasks(TasksManager tasksManager){
        DocumentReference docRef = db.collection("users").document(username);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserDB userDB = documentSnapshot.toObject(UserDB.class);
                assert userDB != null;
                tasksManager.setUnwantedTaskIds(userDB.getUnwantedTasks());
            }
        });
    }

}
