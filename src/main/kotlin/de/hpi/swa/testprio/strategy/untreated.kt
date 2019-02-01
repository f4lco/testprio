package de.hpi.swa.testprio.strategy

class UntreatedStrategy : PrioritisationStrategy {

    override fun apply(p: Params) = p.testResults
}

