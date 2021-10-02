package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.planner.planetapp.R
import net.planner.planetapp.viewmodels.ViewAndEditPreferenceViewModel

class ViewAndEditPreferenceFragment : Fragment() {

    companion object {
        fun newInstance() = ViewAndEditPreferenceFragment()
    }

    private lateinit var viewModel: ViewAndEditPreferenceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.view_and_edit_preference_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ViewAndEditPreferenceViewModel::class.java)
        // TODO: Use the ViewModel
    }

}