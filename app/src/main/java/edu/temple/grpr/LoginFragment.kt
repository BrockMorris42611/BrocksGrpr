package edu.temple.grpr

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.messaging.FirebaseMessaging
import com.google.rpc.Help
import org.json.JSONObject
import kotlin.properties.Delegates

class LoginFragment : Fragment() {

    lateinit var layout : View
    lateinit var sess_key : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout = inflater.inflate(R.layout.fragment_login, container, false)

        val usernameEditText = layout.findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = layout.findViewById<EditText>(R.id.passwordEditText)

        // Navigate to fragment to create account
        layout.findViewById<TextView>(R.id.createAccountTextView)
            .setOnClickListener{
                Navigation
                    .findNavController(layout)
                    .navigate(R.id.action_loginFragment_to_registerFragment)}

        // Navigate to Dashboard if login successful
        layout.findViewById<Button>(R.id.loginButton)
            .setOnClickListener{
                Helper.api.login(requireContext(), User(usernameEditText.text.toString(), null, null), passwordEditText.text.toString(), object: Helper.api.Response {
                    override fun processResponse(response: JSONObject) {
                        if (Helper.api.isSuccess(response)) {
                            Log.d(
                                "RESPONSE TO THE LOOOOGIN REQUEST IS HERE:         ",
                                response.toString() +"           " + FirebaseMessaging.getInstance().token.toString()
                            )
                            sess_key = response.getString("session_key")
                            Helper.user.saveSessionData(requireContext(), sess_key)
                            Helper.user.saveUser(requireContext(), User(
                                usernameEditText.text.toString(),
                                null,
                                null
                            ))
                            Helper.api.registerFCMtoken(requireContext(), User(usernameEditText.text.toString(), null, null), Helper.user.getSessionKey(requireContext())!!,
                                FirebaseMessaging.getInstance().token.toString(), object: Helper.api.Response {
                                    override fun processResponse(response: JSONObject) {
                                        if (Helper.api.isSuccess(response)) {
                                            Log.d(
                                                "THIS IS THE RESPONSE TO THE REGISTERFCMTOKEN CALL HEREREREEEE        : ",
                                                response.toString()
                                            )
                                        } else {
                                            Toast.makeText(requireContext(), Helper.api.getErrorMessage(response), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                })
                            goToDashboard()
                        } else {
                            Toast.makeText(requireContext(), Helper.api.getErrorMessage(response), Toast.LENGTH_LONG).show()
                        }

                    }

                })
            }
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If we're already signed in (saved session key)
        // then go straight to Dashboard
        Helper.user.getSessionKey(requireContext())?.run {
            goToDashboard()
        }
    }

    private fun goToDashboard() {
        Navigation
            .findNavController(layout)
            .navigate(R.id.action_loginFragment_to_dashboardFragment)
    }

}