package edu.temple.grpr

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject

class DashboardFragment : Fragment() {

    lateinit var start_grp_fab: FloatingActionButton
    lateinit var join_grp_fab : FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Let the system know that this fragment
        // wants to contribute to the app menu
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val layout =  inflater.inflate(R.layout.fragment_dashboard, container, false)

        start_grp_fab = layout.findViewById(R.id.startFloatingActionButton)
        join_grp_fab = layout.findViewById(R.id.join_grp_fab)
        // Query the server for the current Group ID (if available)
        // and use it to close the group
        start_grp_fab.setOnLongClickListener {
            Helper.api.queryStatus(requireContext(),
            Helper.user.get(requireContext()),
            Helper.user.getSessionKey(requireContext())!!,
            object: Helper.api.Response {
                override fun processResponse(response: JSONObject) {
                    Helper.api.closeGroup(requireContext(), Helper.user.get(requireContext()),
                        Helper.user.getSessionKey(requireContext())!!, response.getString("group_id"), null)
                }})
            true }
        join_grp_fab.setOnLongClickListener{
            Helper.user.saveCreatorFlag(requireContext(), !Helper.user.getCreatorFlag(requireContext()))
            true  }


        layout.findViewById<View>(R.id.startFloatingActionButton)
            .setOnClickListener{
                (activity as DashboardInterface).createGroup()
            }
        layout.findViewById<View>(R.id.join_grp_fab).setOnClickListener { (activity as DashboardInterface).joinGrp() }
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Use ViewModel to determine if we're in an active Group
        // Change FloatingActionButton behavior depending on if we're
        // currently in a group
        ViewModelProvider(requireActivity()).get(GrPrViewModel::class.java).getGroupId().observe(requireActivity()) {
            if (it.isNullOrEmpty()) {
                start_grp_fab.backgroundTintList  = ColorStateList.valueOf(Color.parseColor("#03DAC5"))
                start_grp_fab.setImageResource(android.R.drawable.ic_input_add)
                start_grp_fab.setOnClickListener {(activity as DashboardInterface).createGroup()}
            } else {
                start_grp_fab.backgroundTintList  = ColorStateList.valueOf(Color.parseColor("#e91e63"))
                start_grp_fab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                start_grp_fab.setOnClickListener {(activity as DashboardInterface).endGroup()}
            }

        }
        ViewModelProvider(requireActivity()).get(GrPrViewModel::class.java).getGroupId().observe(requireActivity()){
            if(it.isNullOrBlank()){
                join_grp_fab.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#03DAC5"))
                join_grp_fab.setImageResource(R.drawable.join_existing_grp)
                join_grp_fab.setOnClickListener{(activity as DashboardInterface).joinGrp()}
            }else{
                join_grp_fab.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#e91e63"))
                join_grp_fab.setImageResource(R.drawable.leave_current_group)
                join_grp_fab.setOnClickListener{(activity as DashboardInterface).leaveGrp()}
            }
        }
    }

    // This fragment places a menu item in the app bar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.action_logout) {
            (activity as DashboardInterface).logout()
            return true
        }

        return false
    }

    interface DashboardInterface {
        fun createGroup()
        fun endGroup()
        fun logout()
        fun joinGrp()
        fun leaveGrp()
    }

}