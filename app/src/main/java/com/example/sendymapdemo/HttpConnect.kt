package com.example.sendymapdemo

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

var httpArray = ArrayList<ArrayList<String>>() //http 커넥션으로 받은 JSON 데이터를 모은 ArrayList
fun getLocationDB():ArrayList<String>{
    var Location2=ArrayList<String>()
    val test = "http://15.164.103.195/httpLocation.php"
    val task = URLConnector(test)
    task.start()
    try{
        task.join()
    } catch (e : InterruptedException){
        e.printStackTrace()
    }

    val result: String? = task.getResult()
    val JO = JSONObject(result)
    val JA: JSONArray = JO.getJSONArray("result")
    for(i in 0 until JA.length()){
        val jo = JA.getJSONObject(i)
        Location2.add(jo.getString("Location"))
    }
    return Location2
}
fun login(test1:String){ //Login 후 사용자의 정보를 들고오는 함수
//    println("login 함수")
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
    val Jrank = JO.getString("rank")

    println(Jrank)
    val JA: JSONArray = JO.getJSONArray("result")

    for(i in 0 until JA.length()){
        val jo = JA.getJSONObject(i)
        UserInfo.add(jo.getString("ID"))
        UserInfo.add(jo.getString("Credit"))
        UserInfo.add(jo.getString("Property"))
        UserInfo.add(jo.getString("Car"))
    }
    headerName.text = UserInfo.get(0)
    Log.e("ID",UserInfo.get(0))
    headerRank.text = Jrank
    headerCredit.text = UserInfo.get(2)
    Log.e("accum",UserInfo.get(2))
    headerAccum.text = UserInfo.get(1)
    Log.e("credit",UserInfo.get(1))
    while(task.isAlive){}
    UserInfo.clear()
    httpConnect()

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

    userList.clear()
    boardAdapter = leaderBoardAdapter(userList)
    boardAdapter.notifyDataSetChanged()
    val result: String? = task.getResult()
    val JO: JSONObject = JSONObject(result)
    val JA: JSONArray = JO.getJSONArray("result")
//    println(JA.getJSONObject(0))
    for (i in 0 until JA.length()) {

        val jo = JA.getJSONObject(i)
        val httpUser = ArrayList<String>()
        httpUser.add(jo.getString("ID"))
        httpUser.add(jo.getString("Credit"))
        httpUser.add(jo.getString("Property"))
        httpUser.add(jo.getString("Car"))
        httpUser.add(jo.getString("rank"))
        httpArray.add(httpUser)
        val newUser = userInfo(httpArray[i][0],Integer.parseInt(httpArray[i][2]),Integer.parseInt(httpArray[i][1]),Integer.parseInt(httpArray[i][4]))
        println("http User : " + httpUser.get(2) + "http Array[0][2] : " + httpArray[i][2])
//        println("http Array[0][2] : " + httpArray[i][0])
        userList.add(newUser)
//        httpArray.clear()
//            println("first ID : "+ (httpArray?.get(i)))
    }
    httpArray.clear()
    ia=JA.length()
    println("ia : "+ia + "JA length : "+JA.length())
    boardAdapter = leaderBoardAdapter(userList)

    boardAdapter.notifyDataSetChanged()
//    println("first ID : " + httpArray[0][0] + " First Property " + httpArray[0][2])
//    println("second ID : " + httpArray[1][0] + " Second Property " + httpArray[1][2])
//    println("third ID : " + httpArray[2][0] + " Third Property " + httpArray[2][2])

//    var a:Int=Integer.parseInt(httpArray[0][1])
}