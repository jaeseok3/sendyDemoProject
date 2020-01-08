package com.example.sendymapdemo

import org.json.JSONArray
import org.json.JSONObject

var httpArray = ArrayList<ArrayList<String>>() //http 커넥션으로 받은 JSON 데이터를 모은 ArrayList

fun login(test1:String){ //Login 후 사용자의 정보를 들고오는 함수
    println("login 함수")
    val UserInfo = ArrayList<String>()
    val test = "http://15.164.103.195/login.php?user=$test1"
    val task = URLConnector(test)
    task.start()
    try {
        task.join()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }

    val result: String? = task.getResult()
    val JO = JSONObject(result)
    val JA: JSONArray = JO.getJSONArray("result")
    for(i in 0 until JA.length()){
        val jo = JA.getJSONObject(i)
        UserInfo.add(jo.getString("ID"))
        UserInfo.add(jo.getString("Credit"))
        UserInfo.add(jo.getString("Property"))
        UserInfo.add(jo.getString("Car"))
    }
//    UserInfo[0]
//    println(UserInfo[0] + "  " + UserInfo[1] + "  " + UserInfo[2] + "  " + UserInfo[3])
}
fun httpConnect(){ //Login 후에 Http connection을 통해 리더보드에 들어갈 데이터 호출
    println("http connect 함수")
    val test = "http://15.164.103.195/httpConnection.php"
    val task = URLConnector(test)
    task.start()
    try {
        task.join()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }

    val result: String? = task.getResult()
    val JO: JSONObject = JSONObject(result)
    val JA: JSONArray = JO.getJSONArray("result")
//    println(JA.getJSONObject(0))
    for (i in 0 until JA.length()) {
        val jo = JA.getJSONObject(i)
        val httpUser = ArrayList<String>()
        httpUser?.add(jo.getString("ID"))
        httpUser?.add(jo.getString("Credit"))
        httpUser?.add(jo.getString("Property"))
        httpUser?.add(jo.getString("Car"))
        httpArray?.add(httpUser)
        val newUser = userInfo(httpArray[i][0],Integer.parseInt(httpArray[i][2]),Integer.parseInt(httpArray[i][1]))
        userList.add(newUser)

//            println("first ID : "+ (httpArray?.get(i)))
    }
    boardAdapter.notifyDataSetChanged()
//    println("first ID : " + httpArray[0][0] + " First Property " + httpArray[0][2])
//    println("second ID : " + httpArray[1][0] + " Second Property " + httpArray[1][2])
//    println("third ID : " + httpArray[2][0] + " Third Property " + httpArray[2][2])

    var a:Int=Integer.parseInt(httpArray[0][1])
}