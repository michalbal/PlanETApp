package net.planner.planetapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.planner.planetapp.adapters.TaskChoosingViewAdapter
import net.planner.planetapp.databinding.ActivityMainBinding
import net.planner.planetapp.planner.PlannerTask
import net.planner.planetapp.viewmodels.MainActivityViewModel


class MainActivity : AppCompatActivity() {

    companion object {

        private const val TAG = "MainActivity"
    }

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mViewModel: MainActivityViewModel

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
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        setupBottomNavMenu(navController)

        if (UserPreferencesManager.mainCalendarAccount == null) {
            navController.navigate(R.id.welcomeFragment)
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
            bottomNav.isVisible = false
        }

        mViewModel.showTasksDialog.observe(this, Observer {
            Log.d(TAG, "showTasksDialog - Received new tasks $it")
            it?.let {
                runOnUiThread {
                    createTaskSelectionDialog(it)
                }
            }
        })

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
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(
            findNavController(R.id.nav_host_fragment)
        ) || super.onOptionsItemSelected(item)
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

}