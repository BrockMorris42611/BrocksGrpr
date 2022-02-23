package edu.temple.grpr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.messaging.FirebaseMessaging

// A single View Model is used to store all data we want to retain
// and observe
class GrPrViewModel : ViewModel() {
    private val location by lazy {
        MutableLiveData<LatLng>()
    }

    private val groupId by lazy {
        MutableLiveData<String>()
    }


    fun setGroupId(id: String) {
        groupId.value = id
    }

    fun setLocation(latLng: LatLng) {
        location.value = latLng
    }

    fun getLocation(): LiveData<LatLng> {
        return location
    }

    fun getGroupId(): LiveData<String> {
        return groupId
    }

}