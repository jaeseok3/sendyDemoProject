package com.example.sendymapdemo.dataClass

import org.koin.dsl.module

val nMapModule = module {
    single { ID() }
    single { nMap() }
    single { pathOverlay()}
}