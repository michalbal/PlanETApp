package net.planner.planetapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.planner.planetapp.planner.TasksManager


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val thread = Thread {
            try {
                val manager = TasksManager() //TODO: add your credentials or token

                manager.getMoodleCourses()

                manager.getMoodleTasks()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
    }
}