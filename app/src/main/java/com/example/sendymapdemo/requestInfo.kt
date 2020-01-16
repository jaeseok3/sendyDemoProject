package com.example.sendymapdemo

class requestInfo{
    var image:Int = 0
    lateinit var source:String
    lateinit var destination:String
    lateinit var sourceCode:String
    lateinit var destinationCode: String
    lateinit var responseData: PathData
    var time:String = ""
    var distance:String = ""
    var reward:Int = 0

    constructor()

    constructor(image:Int,
                source:String,
                destination:String,
                time:String,
                distance:String,
                reward:Int,
                sourceCode:String,
                destinationCode: String,
                responseData: PathData){
        this.image = image
        this.source = source
        this.destination = destination
        this.time = time
        this.distance = distance
        this.reward = reward
        this.sourceCode = sourceCode
        this.destinationCode = destinationCode
        this.responseData = responseData
    }
}