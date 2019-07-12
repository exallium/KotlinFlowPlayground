package com.turner.demos.kotlinflowplayground

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.lang.Exception

fun printThreadInfo(tag: String) {
    println("$tag Thread: ${Thread.currentThread().name}")
}

object Api {
    interface Listener {
        fun valueReceived(i: Int)
    }

    fun addListener(listener: Listener) {
        (1..5).forEach { listener.valueReceived(it) }
    }

}

class KotlinFlowExamples {

    @Test
    fun creatingASimpleLinearFlow() {

        val f = flowOf(1, 2, 3, 4, 5).take(2)
        val v = (1..5).asFlow()

        runBlocking {
            f.collect { println(it) }
        }

    }

    @Test
    fun creatingACustomFlow() {

        val f = flow {
            println("ASDF")
            emit(1)
        }

        runBlocking {
            f.collect { println(it) }
            f.collect { println(it) }
            f.collect { println(it) }
        }
    }

    @Test
    fun creatingACallbackFlow() {
        // Observable.create
        val f = callbackFlow {
            val listener = object : Api.Listener {
                override fun valueReceived(i: Int) {
                    runBlocking { send(i)
                      //if (i == 5) close()
                    }
                }
            }
            Api.addListener(listener)
        }

        runBlocking {
            f.collect { println(it) }
        }
    }

    @Test
    fun creatingAnInfiniteFlow() {
        val inf = generateSequence { 0 }.asFlow()
            .scan(Pair(0, 1)) { (a, b), _ -> Pair(b, a + b) }
            .map { (a, b) -> b }

        runBlocking {
            inf.drop(10).take(10).collect { println(it) }
        }

    }

    @Test
    fun combiningFlows() {
        val f = (1..100).asFlow()
        val x = (1..10).asFlow()

        f.flatMapConcat { x }
    }

    @Test
    fun schedulingFlows() {

        val f = flow {
            printThreadInfo("Flow")
            emit(1)
        }.flowOn(Dispatchers.IO)

        runBlocking {
            printThreadInfo("RunBlocking")

            try {
                f.onCompletion { printThreadInfo("Completion") }.collect()
                f.collect { printThreadInfo("Collect") }
            } catch (e: Exception) {
                //...
            }
        }

    }

}
