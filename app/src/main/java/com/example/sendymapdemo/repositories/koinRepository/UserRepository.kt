package com.example.sendymapdemo.repositories.koinRepository

interface UserRepository {
    fun giveHello(): String
}

class UserRepositoryImpl(): UserRepository{
    override fun giveHello(): String = "Hello Koin"
}