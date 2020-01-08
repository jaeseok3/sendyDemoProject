package com.example.sendymapdemo

class request{
    lateinit var ID:String
    lateinit var description:String
    var rank:Int = 0
    var accumulatedCredit:Int = 0
    var credit:Int = 0
    var level:Int = 0

    lateinit var src:String
    lateinit var dst:String
    var reward:Int = 0
    var duration:Int = 0



    constructor()

    constructor(src:String,dst:String,reward:Int,duration:Int){
        this.src = src
        this.dst = dst
        this.reward = reward
        this.duration = duration
    }
}