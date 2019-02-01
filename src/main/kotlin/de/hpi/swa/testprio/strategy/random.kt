package de.hpi.swa.testprio.strategy

import kotlin.random.Random

class RandomStrategy(seed: Int) : PrioritisationStrategy {

    private val random = Random(seed)

    override fun apply(p: Params) = p.testResults.shuffled(random)
}
