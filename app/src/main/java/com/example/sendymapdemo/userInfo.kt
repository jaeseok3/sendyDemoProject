package com.example.sendymapdemo

class userInfo{
    lateinit var ID:String
    lateinit var description:String
    var rank:Int = 0
    var accumulatedCredit:Int = 0
    var credit:Int = 0
    var level:Int = 0


    constructor()

    constructor(ID:String,description:String,rank:Int,accumulatedCredit:Int,credit:Int,level:Int){
        this.ID = ID
        this.description = description
        this.accumulatedCredit = accumulatedCredit
        this.credit = credit
        this.level = level
        this.rank = rank
    }
}