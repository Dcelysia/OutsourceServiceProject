package com.dcelysia.outsourceserviceproject

import io.reactivex.rxjava3.core.Observable

class TestDemo {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Observable.create<Int> { emitter ->
                emitter.onNext(1)
                emitter.onNext(2)
                emitter.onComplete()
            }.subscribe { println(it)}
        }
    }
}