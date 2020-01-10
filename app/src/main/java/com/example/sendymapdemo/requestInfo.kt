package com.example.sendymapdemo

class requestInfo{
    var image:Int = 0
    lateinit var source:String
    lateinit var destination:String
    lateinit var sourceCode:String
    lateinit var destinationCode: String
    var time:String = ""
    var distance:String = ""
    var reward:Double = 0.0

    constructor()

    constructor(image:Int,
                source:String,
                destination:String,
                time:String,
                distance:String,
                reward:Double,
                sourceCode:String,
                destinationCode: String){
        this.image = image
        this.source = source
        this.destination = destination
        this.time = time
        this.distance = distance
        this.reward = reward
        this.sourceCode = sourceCode
        this.destinationCode = destinationCode
    }
}