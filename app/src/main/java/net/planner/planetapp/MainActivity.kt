package net.planner.planetapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.databinding.ActivityMainBinding
import net.planner.planetapp.planner.PlannerTag
import net.planner.planetapp.planner.TasksManager


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

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

//        lifecycleScope.launch {
//            withContext(Dispatchers.IO) {
//                try {
//                    // TODO no need to do this here since initTasksManager will be called after moodlelogin.
//                   //  TODO If you do not want that, after google login just press skip in moodle login the save
//                   // TODO Best place to move it to is GoogleAccountsFragment inside mBinding.continueToMoodleButton.setOnClickListener {
//                   // TODO right before findNavController()
//                    val manager = TasksManager.getInstance()
//                    manager.initTasksManager() //TODO: add your credentials
//                    var plannerTag = PlannerTag("tag1")
//                    plannerTag.addNewForbiddenTIsetting(SUNDAY, "18:30", "19:00")
//                    plannerTag.addNewForbiddenTIsetting(TUESDAY, "18:30", "19:00")
//                    plannerTag.addNewPreferredTIsetting(MONDAY, "18:30", "23:00")
//                    plannerTag.addNewPreferredTIsetting(FRIDAY, "08:30", "09:00")
//                    manager.addPreferenceTag(plannerTag, true)
//
//                    plannerTag = PlannerTag("tag2")
//                    plannerTag.addNewForbiddenTIsetting(WEDNESDAY, "18:30", "23:00")
//                    plannerTag.addNewForbiddenTIsetting(TUESDAY, "08:30", "09:00")
//                    manager.addPreferenceTag(plannerTag, true)
//
//                    plannerTag.addNewPreferredTIsetting(SATURDAY, "18:30", "19:00")
//                    plannerTag.addNewPreferredTIsetting(FRIDAY, "18:30", "19:00")
//                    manager.addPreferenceTag(plannerTag, true)
//
////                    var plannerTag = PlannerTag("tag1")
////                    plannerTag.addNewForbiddenTIsetting(SUNDAY, "18:30", "19:00")
////                    manager.addPreferenceTag(plannerTag, true)
//
//
//                    manager.addCoursePreference("67118", "SleepInstead", true)
//                    manager.addCoursePreference("67625", "get100", true)
////                manager.addPreference("67420", "secondRun", true)
//
//                    manager.parseMoodleCourses()
//
//                    val parsedMoodleTasks = manager.parseMoodleTasks(0L)
//                    manager.planSchedule(parsedMoodleTasks)
//
//                    manager.addCourseToUnwanted("112233")
//                    manager.addCourseToUnwanted("445566")
//                    manager.addCourseToUnwanted("778899")
//
//
//                    manager.addTaskToUnwanted("995511")
//                    manager.addTaskToUnwanted("884433")
//                    manager.addTaskToUnwanted("662277")
//
//                }  catch (e: Exception) {
//                    Log.e("MainActivity", "Retrieving from Moodle failed, received error ${e.message}")
//                }
//            }
//        }

    }

//    private fun setupNavigationMenu(navController: NavController){
//        val sideNavView = findViewById<NavigationView>(R.id.nav_view)
//        sideNavView?.setupWithNavController(navController)
//    }

    private fun setupBottomNavMenu(navController: NavController) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNav?.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(
            findNavController(R.id.nav_host_fragment)
        ) || super.onOptionsItemSelected(item)
    }

    fun returnBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNav.isVisible = true
    }

}