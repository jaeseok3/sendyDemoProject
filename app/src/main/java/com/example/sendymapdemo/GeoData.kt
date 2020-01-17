package com.example.sendymapdemo

class GeoData{
    lateinit var src:String
    lateinit var dst:String

    constructor()

    constructor(list:ArrayList<String>){
        this.src = list[0]
        this.dst = list[1]
    }
}

fun getGeoName(geoInfo:String):String{
    when(geoInfo){
        "129.160341,35.158627" -> return "해운대"
        "129.077123,35.204779" -> return "동래역"
        "129.079417,35.233355" -> return "부산대"
        "129.095416,35.284732" -> return "노포역"
        "129.032824,35.100518" -> return "용두산공원"
        "129.030499,35.096679" -> return "자갈치시장"
        "128.966725,35.106189" -> return "하단역"
        "129.06012,35.154149" -> return "서면CGV"
        "129.118709,35.153169" -> return "광안리"
        "128.984343,35.163218" -> return "사상"
        "129.065825,35.147221" -> return "BIFC"
        "129.129736,35.168738" -> return "신세계"
        "128.962934,35.047147" -> return "다대포"
        "129.005241,35.210384" -> return "덕천역"
        "129.061529,35.194023" -> return "사직야구장"
        "129.114564,35.167711" -> return "수영역"
        "129.08146,35.186123" -> return "연산역"
        "129.026452,35.338783" -> return "양산역"
        "129.041513,35.114848" -> return "부산역"
        "129.100567,35.137587" -> return "경성대"
    }
    return " "
}