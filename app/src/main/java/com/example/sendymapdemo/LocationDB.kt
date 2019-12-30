package com.example.sendymapdemo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.util.LogPrinter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class LocationDB (context : Context): SQLiteOpenHelper(context, DATABASE_NAME,null,DATABASE_VER){

    companion object{
        private val DATABASE_VER=1
        private val DATABASE_NAME="Marker.DB"

        private val TableName="MarkerLocation"
        private val Column_Lati="latitude"
        private val Column_Longi="longitude"
        private val Column_Name="Name"
    }
    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL("Create table IF NOT EXISTS $TableName ($Column_Lati double,$Column_Longi double,$Column_Name varchar(20))")

    }
    fun listMarker(mMap:GoogleMap){

        val selectQueryHandler = "Select * from $TableName"
        val db=this.writableDatabase
        val cursor = db.rawQuery(selectQueryHandler,null)
        if(cursor.moveToFirst())
        {
            do{
                val lat=markerData()
                lat.nameBy=cursor.getString(cursor.getColumnIndex(Column_Name))
                lat.lati=cursor.getDouble(cursor.getColumnIndex(Column_Lati))
                lat.longi=cursor.getDouble(cursor.getColumnIndex(Column_Longi))
                mMap.addMarker(MarkerOptions().position(LatLng(lat.lati,lat.longi)).title(lat.nameBy))

            } while (cursor.moveToNext())
        }
    }

    fun AddMarker(map: LatLng,colname : String){
        val db=this.writableDatabase
        val values = ContentValues()
        values.put(Column_Lati,map.latitude)
        values.put(Column_Longi,map.longitude)
        values.put(Column_Name,colname)

        db.insert(TableName,null,values)
        println("Latitude : "+map.latitude+"  Longitude : "+map.longitude)
        db.close()
    }





}