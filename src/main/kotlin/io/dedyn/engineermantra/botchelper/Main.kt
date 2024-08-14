package io.dedyn.engineermantra.botchelper

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object Main{
    class BotScope: CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = EmptyCoroutineContext
    }
    @JvmStatic
    fun main(args: Array<String>){
        println("Hello World")
    }
}