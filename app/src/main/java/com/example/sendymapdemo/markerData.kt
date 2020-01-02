package com.example.sendymapdemo

class markerData{
    var lati:Double= 1.0
    var longi:Double=1.0
    var nameBy:String?=null

    constructor()

    constructor(lati:Double,longi:Double,nameBy:String){
        this.lati=lati
        this.longi=longi
        this.nameBy=nameBy
    }
}