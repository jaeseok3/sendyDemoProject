package com.example.sendymapdemo

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker


class CustomInfoWindowAdapter(private val context: Activity) : AppCompatActivity(),InfoWindowAdapter {
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    // Defines the contents of the InfoWindow
    override fun getInfoContents(marker: Marker): View { // Getting view from the layout file info_window_layout
        val v: View = context.layoutInflater.inflate(R.layout.info_layout, null)
        // Getting the position from the marker
        val latLng = marker.position
        // Getting reference to the TextView to set latitude
        //val tvLat = v.findViewById<View>(R.id.tv_lat) as TextView
        // Getting reference to the TextView to set longitude
        //val tvLng = v.findViewById<View>(R.id.tv_lng) as TextView
        // Setting the latitude
        //tvLat.text = "Latitude:" + latLng.latitude
        // Setting the longitude
        //tvLng.text = "Longitude:" + latLng.longitude

        val tvImage = v.findViewById<View>(R.id.info_image) as ImageView
        when(marker.title){
            "동래" -> tvImage.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.dongrae, null))
            "부산대" -> tvImage.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.pnu, null))
            "광안리" -> tvImage.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.gwanganli, null))
            "해운대" -> tvImage.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.haewondae, null))
        }
        val tvName = v.findViewById<View>(R.id.infocontent_tv_name) as TextView
        tvName.text = marker.title
        //val img = v.findViewById<View>(R.id.dongraeImg)
        // Returning the view containing InfoWindow contents
        return v
    }
}
