package edu.temple.grpr

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.type.LatLng
import org.json.JSONObject

/**
 * A helper class to store all functions relating to:
 * API Control
 * User Management
 */
class Helper {

    object api {

        val ENDPOINT_GROUP = "group.php"
        val ENDPOINT_USER = "account.php"

        val API_BASE = "https://kamorris.com/lab/grpr/"

        interface Response {
            fun processResponse(response: JSONObject)
        }

        fun createAccount(context: Context, user: User, password: String, response: Response?){
            val params = mutableMapOf(
                Pair("action", "REGISTER"),
                Pair("username", user.username),
                Pair("password", password),
                Pair("firstname", user.firstname!!),
                Pair("lastname", user.lastname!!)
            )
            makeRequest(context, ENDPOINT_USER, params, response)
        }

        /*
          “action” : “UPDATE”
          “username” : A string representing the user’s chosen username
          “session_key” : The session key received from the last successful log in
          “fcm_token” : The FCM token received from the FCM service
        */

        fun login(context: Context, user: User, password: String, response: Response?) {
            val params = mutableMapOf(
                Pair("action", "LOGIN"),
                Pair("username", user.username),
                Pair("password", password)
            )
            makeRequest(context, ENDPOINT_USER, params, response)
        }
        fun registerFCMtoken(context: Context, user: User, session_key: String, fcm_token : String, response: Response?){
            val params = mutableMapOf(
                Pair("action", "UPDATE"),
                Pair("username", user.username),
                Pair("session_key", session_key),
                Pair("fcm_token", fcm_token)
            )
            makeRequest(context, ENDPOINT_USER, params, response)
        }

        fun createGroup(context: Context, user: User, sessionKey: String, response: Response?) {
            val params = mutableMapOf(
                Pair("action", "CREATE"),
                Pair("username", user.username),
                Pair("session_key", sessionKey)
            )
            makeRequest(context, ENDPOINT_GROUP, params, response)
        }
        /*
          “action” : “UPDATE”
          “username” : A string representing the user’s chosen username
          “session_key” : The session key received from the last successful log in
          “group_id” : A string representing the ID of the group to join or leave
          “latitude” : The current latitude of the user
          “longitude : The current longitude of the user
        */
        fun joinGroup(
            context: Context, user: User, sessionKey: String, group_id: String, response: Response?){
            val params = mutableMapOf(
                Pair("action", "JOIN"),                     Pair("username", user.username),
                Pair("session_key", sessionKey),              Pair("group_id", group_id)
            );makeRequest(context, ENDPOINT_GROUP, params, response)
        }
        fun leaveGroup(
            context: Context, user: User, sessionKey: String, group_id: String, response: Response?){
            val params = mutableMapOf(
                Pair("action", "LEAVE"),                     Pair("username", user.username),
                Pair("session_key", sessionKey),              Pair("group_id", group_id)
            );makeRequest(context, ENDPOINT_GROUP, params, response)
        }
        fun updateLoc(context : Context, user: User, sessionKey: String, group_id: String,
                      latLng: String, response: Response?){
            val l = latLng.split(" ")
            val params = mutableMapOf(
                Pair("action", "UPDATE"),             Pair("username", user.username),
                Pair("session_key", sessionKey),      Pair("group_id", group_id),
                Pair("latitude", l[0]),               Pair("longitude", l[1]),
            );
            //Log.d("LOOK LOOK ON ME HERE IS THE CALL TO UPDATE++ ", "$user $sessionKey $group_id ($latLng) " + l[0] + " " + l[1])
            makeRequest(context, ENDPOINT_GROUP, params, response)
        }

        fun closeGroup(context: Context, user: User, sessionKey: String, groupId: String, response: Response?) {
            val params = mutableMapOf(
                Pair("action", "CLOSE"),
                Pair("username", user.username),
                Pair("session_key", sessionKey),
                Pair("group_id", groupId)
            )
            makeRequest(context, ENDPOINT_GROUP, params, response)
        }

        fun queryStatus(context: Context, user:User, sessionKey: String, response: Response?) {
            val params = mutableMapOf(
                Pair("action", "QUERY"),
                Pair("username", user.username),
                Pair("session_key", sessionKey),
            )
            makeRequest(context, ENDPOINT_GROUP, params, response)
        }

        private fun makeRequest(context: Context, endPoint: String, params: MutableMap<String, String>, responseCallback: Response?) {
            Volley.newRequestQueue(context)
                .add(object: StringRequest(Request.Method.POST, API_BASE + endPoint, {
                    Log.d("Server Response", it)
                    responseCallback?.processResponse(JSONObject(it))
                }, {}){
                    override fun getParams(): MutableMap<String, String> {
                            return params;
                    }
                })
        }

        fun isSuccess(response: JSONObject): Boolean {
            return response.getString("status").equals("SUCCESS")
        }

        fun getErrorMessage(response: JSONObject): String {
            return response.getString("message")
        }

    }

    object user {
        private val SHARED_PREFERENCES_FILE = "shared_prefs"
        private val KEY_SESSION_KEY = "session_key"
        private val KEY_USERNAME = "username"
        private val KEY_FIRSTNAME = "firstname"
        private val KEY_LASTNAME = "lastname"
        private val KEY_GROUP_ID = "group_id"
        private val KEY_CREATOR_STATUS = "is_creator"

        fun saveSessionData(context: Context, sessionKey: String) {
            getSP(context).edit()
                .putString(KEY_SESSION_KEY, sessionKey)
                .apply()
        }

        fun saveCreatorFlag(context: Context, isCreator : Boolean){
            getSP(context).edit().putBoolean(KEY_CREATOR_STATUS, isCreator).apply()
        }

        fun saveGroupId(context: Context, groupId: String) {
            getSP(context).edit()
                .putString(KEY_GROUP_ID, groupId)
                .apply()
        }

        fun getGroupId(context: Context): String? {
            return getSP(context).getString(KEY_GROUP_ID, null)
        }

        fun clearGroupId(context: Context) {
            getSP(context).edit().remove(KEY_GROUP_ID)
                .apply()
        }

        fun clearSessionData(context: Context) {
            getSP(context).edit().remove(KEY_SESSION_KEY)
                .apply()
        }

        fun getSessionKey(context: Context): String? {
            return getSP(context).getString(KEY_SESSION_KEY, null)
        }

        fun getCreatorFlag(context: Context) : Boolean{
            return getSP(context).getBoolean(KEY_CREATOR_STATUS, false)
        }

        fun getUsername(context: Context): String? {
            return getSP(context).getString(KEY_USERNAME, null)
        }

        fun saveUser(context: Context, user: User) {
            getSP(context).edit()
                .putString(KEY_USERNAME, user.username)
                .putString(KEY_FIRSTNAME, user.firstname)
                .putString(KEY_LASTNAME, user.lastname)
                .apply()
        }

        fun get(context: Context) : User {
            return User (
                        getSP(context).getString(KEY_USERNAME, "")!!,
                        getSP(context).getString(KEY_FIRSTNAME, ""),
                        getSP(context).getString(KEY_LASTNAME, ""),
                    )
        }
        private fun getSP (context: Context) : SharedPreferences {
            return context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        }
    }


}