package net.planner.planetapp

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.planner.planetapp.adapters.SubtaskPlanDayRep
import net.planner.planetapp.adapters.SubtasksDayAdapter
import net.planner.planetapp.adapters.TaskChoosingViewAdapter
import net.planner.planetapp.databinding.ActivityMainBinding
import net.planner.planetapp.fragments.WelcomeFragment
import net.planner.planetapp.fragments.WelcomeFragmentDirections
import net.planner.planetapp.networking.GoogleCalenderCommunicator
import net.planner.planetapp.planner.PlannerEvent
import net.planner.planetapp.planner.PlannerTask
import net.planner.planetapp.viewmodels.MainActivityViewModel


class MainActivity : AppCompatActivity() {

    companion object {

        private const val TAG = "MainActivity"
    }

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mViewModel: MainActivityViewModel
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        val binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        mViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        // Adding navigation
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment
        ) as NavHostFragment
        navController = navHostFragment.navController

        // Setting App bar
        appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar = binding.toolbar
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setupActionBarWithNavController(navController, appBarConfiguration)
        setupBottomNavMenu(navController)

        if (!UserPreferencesManager.didFinishFirstSeq) {
            navController.navigate(R.id.welcomeFragment)
            hideBottomNavigation()
        }

        mViewModel.showTasksDialog.observe(this, Observer {
            Log.d(TAG, "showTasksDialog - Received new tasks $it")
            it?.let {
                runOnUiThread {
                    createTaskSelectionDialog(it)
                }
            }
        })

        mViewModel.showPlanCalculatedDialog.observe(this, Observer {
            Log.d(TAG, "showPlanCalculatedDialog - Received new subTasks $it")
            it?.let {
                runOnUiThread {
                    createPlanApprovalDialog(it)
                }
            }
        })

        // This defines what happens after requesting permission for Google Calendar write access
        requestPermissionLauncher = this.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            Log.d(TAG, "Received response from write permission request. isGranted is: $isGranted")
            if (isGranted) {
                // Save plan
                Log.d(TAG, "Permission was granted, writing events to calendar")
                mViewModel.savePlan()

            } else {
                // Move to Moodle Screen
                Log.d(TAG, "Permission was denied, saving events only to our db")
                mViewModel.savePlan()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called")
        mViewModel.onStart()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called")
        mViewModel.onStop()
    }


    private fun setupBottomNavMenu(navController: NavController) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNav?.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {

        val navController = this.findNavController(R.id.nav_host_fragment)
        return when(navController.currentDestination?.id) {
            R.id.welcomeFragment -> {
                false
            }
            R.id.createPreferenceFragment, R.id.createTaskFragment -> {
                createChangesNotSavedDialog()
                true
            }
            else -> navController.navigateUp(appBarConfiguration)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(
            findNavController(R.id.nav_host_fragment)
        ) || super.onOptionsItemSelected(item)
    }

    fun hideBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNav.isVisible = false
    }

    fun returnBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNav.isVisible = true
    }

    private fun createTaskSelectionDialog(tasks: Collection<PlannerTask>) {
        Log.d(TAG, "createTaskSelectionDialog called")

        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_tasks_to_plan_selection, null)
        val adapter = TaskChoosingViewAdapter(tasks.toList())
        val recyclerView = dialogView.findViewById(R.id.tasks_to_plan_list) as RecyclerView
        recyclerView.layoutManager  = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        Log.d(TAG, "createTaskSelectionDialog: Creating and showing the dialog")
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(R.string.select_tasks_dialog_title)
            .setNegativeButton(android.R.string.cancel){ dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(R.string.select_tasks_positive_button) { dialog, _ ->
                val tasksChosen = adapter.tasksToPlan
                Toast.makeText(this, App.context.getText(R.string.starting_to_plan_message), Toast.LENGTH_SHORT).show()
                mViewModel.startPlanningScheduleForTasks(tasksChosen)
                dialog.cancel()
            }
            .create()
            .show()

    }


    private fun createPlanApprovalDialog(subTasks: List<SubtaskPlanDayRep>) {
        Log.d(TAG, "createPlanApprovalDialog called")

        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_plan_approval, null)
        val adapter = SubtasksDayAdapter(subTasks, this)
        val recyclerView = dialogView.findViewById(R.id.sub_tasks_per_day_list) as RecyclerView
        recyclerView.layoutManager  = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
        recyclerView.enforceSingleScrollDirection()

        Log.d(TAG, "createPlanApprovalDialog: Creating and showing the dialog")
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("")
            .setNegativeButton(android.R.string.cancel){ dialog, _ ->
                dialog.cancel()
            }
            .setNeutralButton(R.string.plan_approval_neutral_button) { dialog, _ ->
                // Call for task choosing dialog with all tasks and have user choose the tasks to recalculate
                dialog.cancel()
            }
            .setPositiveButton(R.string.plan_approval_positive_button) { dialog, _ ->
                val eventsChosen = adapter.getEventsApproved()
                Toast.makeText(this, App.context.getText(R.string.saving_your_schedule), Toast.LENGTH_SHORT).show()
                if (GoogleCalenderCommunicator.haveCalendarWritePermissions(App.context)) {
                    mViewModel.savePlan(eventsChosen)
                } else {
                    mViewModel.saveSubTasksForLater(eventsChosen)
                    requestPermissionLauncher.apply { launch(Manifest.permission.WRITE_CALENDAR) }
                }

                dialog.cancel()
            }
            .create()
            .show()

    }

    private fun createChangesNotSavedDialog() {
        Log.d(TAG, "createChangesNotSavedDialog: Creating and showing the dialog")
        val dialog = AlertDialog.Builder(this)
            .setMessage(R.string.changes_detected_message)
            .setTitle(R.string.are_you_sure_dialog_title)
            .setNegativeButton(android.R.string.cancel){ dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                returnBottomNavigation()
                findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
                dialog.cancel()
            }
            .create()
            .show()
    }

}