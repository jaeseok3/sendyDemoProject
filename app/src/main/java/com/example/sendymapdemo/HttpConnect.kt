package com.example.sendymapdemo

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder


fun updateCredit(ID:String,reward:Double){
    val userID:String=ID
    val reward:Double=reward
    val query ="http://15.164.103.195/httpUpdateCredit.php?user=$userID&reward=$reward"
    val task=URLConnector(query)
    task.start()
    try{
        task.join()
    } catch (e:InterruptedException){
        e.printStackTrace()
    }
}
fun InsertHistory(newHistory: historyInfo) {
    val aIdentity:String=URLEncoder.encode(userIdentity,"UTF-8")
    val asource:String=URLEncoder.encode(newHistory.source,"UTF-8")
    val adest:String=URLEncoder.encode(newHistory.destination,"UTF-8")
    val adate:String=URLEncoder.encode(newHistory.historyDate,"UTF-8")
    val atime:String=URLEncoder.encode(newHistory.historyTime,"UTF-8")
    val adis:String=URLEncoder.encode(newHistory.distance,"UTF-8")
    val areward:String=URLEncoder.encode("${newHistory.reward}","UTF-8")
    val test = "http://15.164.103.195/httpHistoryInsert.php?"+"user=$aIdentity" +
            "&time=${newHistory.time}"+"&src=$asource"+"&dest=$adest"+"&distance=$adis&reward=$areward&htime=$atime&hdate=$adate"


    println(test)
    val task = URLConnector(test)
    task.start()
    try {
        task.join()
        Log.e("db","db삽입성공")
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }

}
fun GetHistory(userID:String){
    val test = "http://15.164.103.195/httpHistory.php?user=$userID"
    val task = URLConnector(test)
    task.start()
    try {
        task.join()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
    val result: String? =task.getResult()
    val JO=JSONObject(result)
    try{
    val JA: JSONArray = JO.getJSONArray("result")
            for (i in 0 until JA.length()) {
            val jo = JA.getJSONObject(i)
            //새로운 히스토리추가
            val newHistory = historyInfo(
                jo.getString("Src"),
                jo.getString("Dest"),
                jo.getString("Time"),
                jo.getString("Distance"),
                jo.getString("Reward").toDouble()
                ,
                jo.getString("HistoryTime"),
                jo.getString("HistoryDate")
            )
            historyList.add(newHistory) //히스토리 리스트에 추가
        }
    } catch (e:Exception){
        return
    }
}
fun getLocationDB():ArrayList<String>{
    val Location2=ArrayList<String>()
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

fun updateRanking(test1:String){
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
    while(task.isAlive){}
    UserInfo.clear()
    httpConnect()
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
    val httpArray = ArrayList<ArrayList<String>>() //http 커넥션으로 받은 JSON 데이터를 모은 ArrayList
    userList.clear()
    boardAdapter.notifyDataSetChanged()
    val result: String? = task.getResult()
    val JO: JSONObject = JSONObject(result)
    val JA: JSONArray = JO.getJSONArray("result")
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
        userList.add(newUser)
    }
    httpArray.clear()

    boardAdapter = leaderBoardAdapter(userList)
    boardAdapter.notifyDataSetChanged()
}