package de.hpi.swa.testprio.strategy

/**
 * Output the untreated test order.
 */
class UntreatedStrategy : PrioritisationStrategy {

    override fun apply(p: Params) = p.testResults
}
