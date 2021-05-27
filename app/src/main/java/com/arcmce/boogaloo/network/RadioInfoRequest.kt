package com.arcmce.boogaloo.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest

class RadioInfoRequest(val context: Context) {
    private val url = "https://public.radio.co/stations/sb88c742f0/status"

    fun getRadioInfo(callback: (response: String) -> Unit) {
        sendRadioInfoRequest(callback)
    }

    fun sendRadioInfoRequest(callback: (response: String) -> Unit) {
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                callback(response)
            },
            {}
        )

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest, false)
    }

}