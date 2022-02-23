package edu.temple.grpr

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray
import org.json.JSONObject
import java.lang.StringBuilder

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        Log.d("NEW TOKEEEEEEEEEEEN REGISTERED", p0)
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.d("HERE IN THE ONMESSAGERECEIVED FUNCTION WITH MESSAGE:::: ", p0.data.toString())
        if (p0.data.isNotEmpty()){
            var sb :String = ""
            val intent = Intent(this, MainActivity::class.java)
            val jObj:JSONObject = p0.data as JSONObject
            val arrOfUsers = jObj.getJSONArray("data") as JSONArray
            (0 until arrOfUsers.length()).forEach {
                val temp = Helper.user.getUsername(this)
                if (temp != null && !arrOfUsers.get(it).toString()
                        .contains(temp)
                ) { //if out helper does ret null AND that array obj has our user name
                    sb + arrOfUsers.get(it).toString() + " " // append the user info and add delimiter
                }
            }
            intent.putExtra("Brock_BROAD", sb)
            sendBroadcast(intent)
        }
        super.onMessageReceived(p0)
    }

}