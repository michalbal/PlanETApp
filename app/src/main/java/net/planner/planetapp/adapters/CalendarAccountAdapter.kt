package net.planner.planetapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import net.planner.planetapp.databinding.GoogleAccountListItemBinding

class CalendarAccountAdapter(
    private var values: List<String>
    ) : RecyclerView.Adapter<CalendarAccountAdapter.ViewHolder>()  {

    private var mainCalendarName = ""
    var googleAccounts: MutableSet<String> = mutableSetOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarAccountAdapter.ViewHolder {

        return ViewHolder(
            GoogleAccountListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.account.text = item

        holder.account.setOnClickListener { view ->
            if (holder.account.isChecked) {
                googleAccounts.remove(item)
            } else {
                googleAccounts.add(item)
            }
        }

        holder.isMainRadio.setOnClickListener { view ->
            if (!holder.account.text.equals(mainCalendarName)) {
                mainCalendarName = holder.account.text.toString()
            }
        }


    }

    fun updateAccounts(accounts: List<String>) {
        values = accounts
        googleAccounts.addAll(accounts)
        notifyDataSetChanged()
    }

    fun updateMainCalendar(accountName:String) {
        mainCalendarName = accountName
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: GoogleAccountListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var account : CheckBox = binding.checkBox
        var isMainRadio: RadioButton = binding.radioButton
    }


}