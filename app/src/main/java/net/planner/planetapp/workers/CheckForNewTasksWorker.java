package net.planner.planetapp.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.planner.planetapp.planner.TasksManager;

import org.jetbrains.annotations.NotNull;


public class CheckForNewTasksWorker extends Worker {

    public CheckForNewTasksWorker(@NonNull @NotNull Context context,
                                  @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {

        TasksManager.getInstance().pullDataFromDB();

        return Result.success();
    }
}