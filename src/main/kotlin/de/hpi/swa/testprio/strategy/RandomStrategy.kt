package de.hpi.swa.testprio.strategy

import kotlin.random.Random

/**
 * Execute test cases in randomized order.
 */
class RandomStrategy(seed: Int) : PrioritizationStrategy {

    private val random = Random(seed)

    override fun reorder(p: Params) = p.testResults.shuffled(random)
}
