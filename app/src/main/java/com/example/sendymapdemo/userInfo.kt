package com.example.sendymapdemo

class userInfo{
    lateinit var ID:String
    lateinit var description:String
    var rank:Int = 0
    var accumulatedCredit:Int = 0
    var credit:Int = 0


    constructor()

    constructor(ID:String,accumulatedCredit:Int,credit:Int){
        this.ID = ID
        this.description = "HELLO, I AM USER"
        this.accumulatedCredit = accumulatedCredit
        this.credit = credit
        this.rank = 1
    }
}