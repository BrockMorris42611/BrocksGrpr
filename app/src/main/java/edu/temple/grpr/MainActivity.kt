package edu.temple.grpr

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginRight
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.messaging.*
import com.google.rpc.Help
import org.json.JSONObject


class MainActivity : AppCompatActivity(), DashboardFragment.DashboardInterface {

    var serviceIntent: Intent? = null
    val grprViewModel : GrPrViewModel by lazy {
        ViewModelProvider(this).get(GrPrViewModel::class.java)
    }

    // Update ViewModel with location data whenever received from LocationService
    var locationHandler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            grprViewModel.setLocation(msg.obj as LatLng)
        }
    }

    var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {

            // Provide service with handler
            (iBinder as LocationService.LocationBinder).setHandler(locationHandler)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {}
    }
    val broadRec = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("HELLO THIS IS YOUR RECEIVER HERE::: ", FROM_GROUPLOCSERVICE)
        }
    }
    val FROM_GROUPLOCSERVICE = "6785cd177zc"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //registerReceiver(broadRec, IntentFilter(FROM_GROUPLOCSERVICE))

        createNotificationChannel()
        serviceIntent = Intent(this, LocationService::class.java)

        grprViewModel.getGroupId().observe(this) {
            if (!it.isNullOrEmpty())
                supportActionBar?.title = "GRPR - $it"
            else
                supportActionBar?.title = "GRPR"
        }

        Helper.user.getGroupId(this)?.run {
            grprViewModel.setGroupId(this)
            startLocationService()
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )
        }
    }

    override fun onDestroy() {
        //unregisterReceiver(broadRec)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel("default", "Active Convoy", NotificationManager.IMPORTANCE_HIGH)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun createGroup() {
        Log.d("HERE IS WHERE I AM TRYING TO create AS:: ",Helper.user.get(this).username + " "
                + Helper.user.getCreatorFlag(this))
        Helper.user.saveCreatorFlag(this, true)
        Log.d("AGAIN: HERE IS WHERE I AM TRYING TO create AS:: ",Helper.user.get(this).username + " "
                + Helper.user.getCreatorFlag(this))
        Helper.api.createGroup(this, Helper.user.get(this), Helper.user.getSessionKey(this)!!, object: Helper.api.Response {
            override fun processResponse(response: JSONObject) {
                if (Helper.api.isSuccess(response)) {
                    grprViewModel.setGroupId(response.getString("group_id"))
                    Helper.user.saveGroupId(this@MainActivity, grprViewModel.getGroupId().value!!)
                    startLocationService()
                } else {
                    Toast.makeText(this@MainActivity, Helper.api.getErrorMessage(response) + "hello", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun joinGrp() {
        Log.d("HERE IS WHERE I AM TRYING TO join AS:: ",Helper.user.get(this).username + " "
                + Helper.user.getCreatorFlag(this))
        if(!Helper.user.getCreatorFlag(this)) { //WE CANT BE THE OWNER AND JUST JOIN ANOTHER GROUP
            Log.d("AGAIN : HERE IS WHERE I AM TRYING TO join AS:: ",Helper.user.get(this).username + " "
                    + Helper.user.getCreatorFlag(this))
            val v = EditText(this)
            AlertDialog.Builder(this).setTitle("Join New Group")
                .setMessage("Enter Group Code Below").setView(v).setPositiveButton(
                    "Join"
                ) { _, _ ->
                    Log.d("HERE IS THE GROUP CODE: ", v.text.toString())
                    Log.d("HERE IS THE GROUP CODE: ", grprViewModel.getLocation().value.toString())

                    Helper.api.joinGroup(this, Helper.user.get(this),
                        Helper.user.getSessionKey(this)!!, v.text.toString(),
                        object : Helper.api.Response {
                            override fun processResponse(response: JSONObject) {
                                if (Helper.api.isSuccess(response)) {
                                    grprViewModel.setGroupId(response.getString("group_id"))
                                    Helper.user.saveGroupId(this@MainActivity, grprViewModel.getGroupId().value!!
                                    )
                                    startLocationService()
                                } else
                                    Toast.makeText(this@MainActivity,
                                        Helper.api.getErrorMessage(response), Toast.LENGTH_LONG).show()
                            }
                        })
                }.setNegativeButton("Cancel") { p0, _ -> p0.cancel() }.show()
        }
        else
            Toast.makeText(this, "Cannot join a group if you're the creator of one.", Toast.LENGTH_LONG).show()
    }

    override fun leaveGrp() {
        Log.d("HERE IS THE leave GROUP CHECK FOR CREATOR FLAG:: ", Helper.user.getCreatorFlag(this).toString())
        if(!Helper.user.getCreatorFlag(this)) { // is this user a creator of a group?? WE CANT BE TO LEAVE
            Log.d("2HERE IS THE leave GROUP CHECK FOR CREATOR FLAG:: ", Helper.user.getCreatorFlag(this).toString())
            AlertDialog.Builder(this)
                .setTitle("Leave Group " + Helper.user.getGroupId(this).toString() + "?")
                .setMessage("Are you sure you want to leave this group?").setPositiveButton("Yes")
                { _, _ ->
                    Log.d("Leave: ", "NOWOWOWOWOWOWOOWOWOW")
                    Helper.api.leaveGroup(this,
                        Helper.user.get(this),
                        Helper.user.getSessionKey(this)!!,
                        Helper.user.getGroupId(this)!!,
                        object : Helper.api.Response {
                            override fun processResponse(response: JSONObject) {
                                if (Helper.api.isSuccess(response)) {
                                    grprViewModel.setGroupId("")
                                    Helper.user.clearGroupId(this@MainActivity)
                                    stopLocationService()
                                } else
                                    Toast.makeText(this@MainActivity, Helper.api.getErrorMessage(response),
                                        Toast.LENGTH_LONG).show()
                            }
                        })
                }.setNegativeButton("Cancel") { p0, _ -> p0.cancel() }.show()
        }else{
            Toast.makeText(this, "You cannot leave a group you created", Toast.LENGTH_LONG).show()
        }
    }

    override fun endGroup() {
        Log.d("HERE IS WHERE I AM TRYING TO end AS:: ",Helper.user.get(this).username + " "
                + Helper.user.getCreatorFlag(this))
        if(Helper.user.getCreatorFlag(this)) { // is this user a creator of a group?? WE NEED TO BE TO END
            AlertDialog.Builder(this).setTitle("Close Group")
                .setMessage("Are you sure you want to close the group?")
                .setPositiveButton("Yes")
                { _, _ ->
                    Helper.api.closeGroup(this, Helper.user.get(this),
                        Helper.user.getSessionKey(this)!!, grprViewModel.getGroupId().value!!,
                        object : Helper.api.Response {
                            override fun processResponse(response: JSONObject) {
                                if (Helper.api.isSuccess(response)) {
                                    grprViewModel.setGroupId("")
                                    Helper.user.clearGroupId(this@MainActivity)
                                    stopLocationService()
                                } else
                                    Toast.makeText(this@MainActivity,
                                        Helper.api.getErrorMessage(response), Toast.LENGTH_LONG).show()
                            }
                        })
                    Helper.user.saveCreatorFlag(this, false) // we just ended the group so no longer a creator
                }.setNegativeButton("Cancel") { p0, _ -> p0.cancel() }.show()
        }else {
            Toast.makeText(this, "You cannot end a group you do not own.", Toast.LENGTH_LONG).show()
        }
    }

    override fun logout() {
        Helper.user.clearSessionData(this)
        Navigation.findNavController(findViewById(R.id.fragmentContainerView))
            .navigate(R.id.action_dashboardFragment_to_loginFragment)
    }

    private fun startLocationService() {

        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        startService(serviceIntent)
    }
    private fun stopLocationService() {
        unbindService(serviceConnection)
        stopService(serviceIntent)
    }
}