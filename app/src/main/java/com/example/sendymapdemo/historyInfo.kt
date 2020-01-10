package com.example.sendymapdemo

class historyInfo{
    lateinit var source:String
    lateinit var destination:String
//    lateinit var sourceCode:String
//    lateinit var destinationCode: String
    var time:String = ""
    var distance:String = ""
    var historyTime:String = ""
    var historyDate:String = ""
    var reward:Double = 0.0

    constructor()

    constructor(source:String,
                destination:String,
                time:String,
                distance:String,
                reward:Double,
                historyTime:String,
                historyDate:String){
        this.source = source
        this.destination = destination
        this.time = time
        this.distance = distance
        this.reward = reward
        this.historyDate = historyDate
        this.historyTime = historyTime
    }
}