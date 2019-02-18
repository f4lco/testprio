package de.hpi.swa.testprio.strategy

import kotlin.random.Random

/**
 * Execute test cases in randomized order.
 */
class RandomStrategy(seed: Int) : PrioritisationStrategy {

    private val random = Random(seed)

    override fun apply(p: Params) = p.testResults.shuffled(random)
}
