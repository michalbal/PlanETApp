package net.planner.planetapp.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.planner.TasksManager

class MoodleSignInFragmentViewModel : ViewModel() {

    companion object {

        private const val TAG = "MoodleSignInModel"

    }

    var canContinue = MutableLiveData<Boolean>()

    fun getToken(userName: String, password: String){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    TasksManager.getInstance().connectToMoodle(userName, password)
                    Log.d(TAG, "Found token successfully! Moving to selection screen")
                    canContinue.postValue(true)
                } catch (e: Exception) {
                    Log.e(TAG, "Retrieving from Moodle failed, received error ${e.message}")
                    canContinue.postValue(false)
                }
            }
        }

    }
}