package net.planner.planetapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillValue
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.LinearLayoutManager
import net.planner.planetapp.R
import net.planner.planetapp.adapters.CalendarAccountAdapter
import net.planner.planetapp.databinding.FragmentCreatePreferenceBinding


class CreatePreferenceFragment : Fragment() {
    companion object {

        private const val TAG = "CreatePreferenceFragment"

    }

    private lateinit var mBinding: FragmentCreatePreferenceBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentCreatePreferenceBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Priority choice adapter
        val items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
        val adapter = ArrayAdapter(requireContext(), R.layout.priority_list_item, items)
        val editPreference = mBinding.editPreferencePriority.editText as? AutoCompleteTextView
        editPreference?.setAdapter(adapter)
        editPreference?.setText("5")


        // Init Forbidden times Recycler View
        val forbiddenTimesRecycler = mBinding.forbiddenTimesList
        forbiddenTimesRecycler.layoutManager = LinearLayoutManager(context)
        forbiddenTimesRecycler.adapter = CalendarAccountAdapter(listOf())



    }

}