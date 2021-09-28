package net.planner.planetapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccountsFragmentViewModel : ViewModel() {

    var googleAccounts = MutableLiveData<MutableCollection<String>>()

    fun updateAccounts() {

    }
}