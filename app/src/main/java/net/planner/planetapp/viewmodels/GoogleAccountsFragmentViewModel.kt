package net.planner.planetapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.App
import net.planner.planetapp.networking.GoogleCalenderCommunicator

class GoogleAccountsFragmentViewModel : ViewModel() {
    var accounts = MutableLiveData<Collection<String>>()

    fun updateAccounts() {
        viewModelScope.launch {
            getCalendarAccounts()
        }
    }

    private suspend fun getCalendarAccounts() {
        withContext(Dispatchers.IO) {
            val googleAccounts = async {
                GoogleCalenderCommunicator.findAccountCalendars(App.context)
            }.await()
            accounts.postValue(googleAccounts)
        }
    }
}