package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult

class TestParams(override var jobId: String,
                 override var jobIds: List<String> = emptyList(),
                 override var jobIndex: Int = 0,
                 override var changedFiles: List<String> = emptyList(),
                 override var testResults: List<TestResult> = emptyList()) : Params