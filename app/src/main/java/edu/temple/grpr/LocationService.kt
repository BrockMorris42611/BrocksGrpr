package edu.temple.grpr

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

class LocationService : Service() {

    var locationManager: LocationManager? = null
    var locationListener: LocationListener? = null
    var notification: Notification? = null
    var handler: Handler? = null

    // Define a binder to accept a handler
    inner class LocationBinder : Binder() {
        fun setHandler(handler: Handler) {
            this@LocationService.handler = handler
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return LocationBinder()
    }
    /*override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("TAG", "From: ${remoteMessage.from}")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("TAG", "Message data payload: ${remoteMessage.data}")
        }
    }*/
    override fun onCreate() {
        super.onCreate()

        // Fetch location manager and define location listener to report
        // user location updates to connected client
        var msg: Message
        var z: LatLng
        locationManager = getSystemService(LocationManager::class.java)
        locationListener = LocationListener { location: Location ->
            if (handler != null) {
                msg = Message.obtain()
                msg.obj = LatLng(location.latitude, location.longitude)
                //Log.d("IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII: ", (msg.obj == null).toString())
                handler!!.sendMessage(msg)
                //Log.d("LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL: ", (msg.obj == null).toString())
                z = msg.obj as LatLng
                val myLatLng : LatLng = msg.obj as LatLng
                //Log.d("----------------INSIDE THE SERVICE", myLatLng.latitude
                    //.toString() + " " + myLatLng.longitude.toString())
                Helper.api.updateLoc(this, Helper.user.get(this), Helper.user.getSessionKey(this)!!,
                Helper.user.getGroupId(this)!!, myLatLng.latitude.toString() + " " + myLatLng.longitude.toString(), object : Helper.api.Response {
                        override fun processResponse(response: JSONObject) {
                            if (Helper.api.isSuccess(response)) {
                                //Log.d("SUCCESS IN update FROM THE LOCATION SERVICE: ", z.toString())
                                if(msg.obj != null)
                                    Log.d("++++++++++++++INSIDE THE SERVICE", msg.obj.toString())
                            } else{
                                Log.d("ERROR ERROR ERROR", Helper.api.getErrorMessage(response))
                                Toast.makeText(this@LocationService,
                                    Helper.api.getErrorMessage(response), Toast.LENGTH_LONG).show()}
                        }
                    })
            }
        }

        // Notification for Foreground Service
        notification = NotificationCompat.Builder(this)
            .setContentTitle("Group active")
            .setContentText("Currently in group []")
            .setSmallIcon(R.drawable.ic_menu_directions)
            .setChannelId("default")
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Start requesting location updates when service Started
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startForeground(1, notification);
            Log.d("Location Service", "Started")

            locationListener?.apply {
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5f, this)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        handler = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(locationListener!!)
    }
}