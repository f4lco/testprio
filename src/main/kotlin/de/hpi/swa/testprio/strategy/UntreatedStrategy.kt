package de.hpi.swa.testprio.strategy

/**
 * Output the untreated test order.
 */
class UntreatedStrategy : PrioritizationStrategy {

    override fun reorder(p: Params) = p.testResults
}
