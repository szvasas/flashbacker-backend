package dev.vasas.flashbacker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FlashbackerApplication

fun main(args: Array<String>) {
	runApplication<FlashbackerApplication>(*args)
}
