package de.hpi.swa.testprio.strategy

/**
 * Output the untreated test order.
 */
class UntreatedStrategy : PrioritisationStrategy {

    override fun reorder(p: Params) = p.testResults
}
