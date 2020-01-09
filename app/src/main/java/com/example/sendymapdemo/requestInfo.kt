package com.example.sendymapdemo

class requestInfo{
    var image:Int = 0
    lateinit var source:String
    lateinit var destination:String
    lateinit var sourceCode:String
    lateinit var destinationCode: String
    var time:Int = 0
    var duration:Int = 0
    var reward:Int = 0

    constructor()

    constructor(image:Int,
                source:String,
                destination:String,
                time:Int,
                duration:Int,
                reward:Int,
                sourceCode:String,
                destinationCode: String){
        this.image = image
        this.source = source
        this.destination = destination
        this.time = time
        this.duration = duration
        this.reward = reward
        this.sourceCode = sourceCode
        this.destinationCode = destinationCode
    }
}