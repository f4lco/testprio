package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult
import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.GraphDatabase

class Graph(
    graphHost: String,
    graphUser: String,
    graphPassword: String
) : PrioritizationStrategy, AutoCloseable {

    private val driver = GraphDatabase.driver(graphHost, AuthTokens.basic(graphUser, graphPassword))
    private val session = driver.session()

    init {
        updateSchema()
    }

    private fun updateSchema() {
        session.writeTransaction { tx ->
            tx.run("CREATE CONSTRAINT ON (file: File) ASSERT file.name IS UNIQUE")
            tx.run("CREATE CONSTRAINT ON (tc: TestCase) ASSERT tc.name IS UNIQUE")
            tx.success()
        }
    }

    override fun reorder(p: Params): List<TestResult> {
        val priorities = p.testResults.associateWith { tc ->

            val statement = """
                UNWIND {changedFiles} AS fileName
                MATCH (changedFile:File), (tc:TestCase)
                WHERE tc.name = {tc} AND changedFile.name = fileName
                MATCH p = shortestPath((changedFile)--(tc))
                RETURN
                  fileName AS origin,
                  reduce(failures = 0.0, r in relationships(p) | failures + r.counter) AS failureCount,
                  length(p) AS edgeCount
            """.trimIndent()

            val result = session.run(statement, mapOf("changedFiles" to p.changedFiles, "tc" to tc.name))
            result.list { record ->
                val failureCount = record["failureCount"].asDouble()
                val edgeCount = record["edgeCount"].asDouble()
                failureCount / edgeCount
            }.sum()
        }

        devalue()
        // interesting: can the current prioritization already know how the current files are connected?
        createFileGraph(p.changedFiles)
        createTestNodes(p.testResults, p.changedFiles)
        return p.testResults.sortedByDescending { priorities[it] }
    }

    private fun devalue() {
        session.writeTransaction { tx ->
            tx.run("MATCH ()-[e:CHANGED_WITH]-() SET e.counter = e.counter * 0.8")
            tx.run("MATCH ()-[e:AFFECTED]-() SET e.counter = e.counter * 0.8")
            tx.run("MATCH ()-[e:FAILED_WITH]-() SET e.counter = e.counter * 0.8")
            tx.success()
        }
    }

    private fun createFileGraph(changedFiles: List<String>) {
        session.writeTransaction { tx ->
            val created = mutableSetOf<String>()
            for (path in changedFiles) {
                tx.run("MERGE (:File {name: {path}})", mapOf("path" to path))
                for (prior in created) {
                    val statement = """
                        MATCH (current:File {name: {current}}), (prior:File {name: {prior}})
                        MERGE (current)-[change:CHANGED_WITH]-(prior)
                        ON CREATE SET change.counter = 1
                        ON MATCH SET change.counter = change.counter + 1
                    """.trimIndent()
                    tx.run(statement, mapOf("current" to path, "prior" to prior))
                }
                created.add(path)
            }
        }
    }

    private fun createTestNodes(testResults: List<TestResult>, changedFiles: List<String>) {
        session.writeTransaction { tx ->
            val created = mutableSetOf<TestResult>()
            for (tc in testResults) {
                tx.run("MERGE (:TestCase {name: {name}})", mapOf("name" to tc.name))
                for (prior in created) {
                    val statement = """
                        MATCH (current:TestCase {name: {current}}), (prior:TestCase {name: {prior}})
                        MERGE (current)-[failure:FAILED_WITH]-(prior)
                        ON CREATE SET failure.counter = 1
                        ON MATCH SET failure.counter = failure.counter + 1
                    """.trimIndent()
                    tx.run(statement, mapOf("current" to tc.name, "prior" to prior.name))
                }
                created.add(tc)

                for (file in changedFiles) {
                    val statement = """
                        MATCH (tc:TestCase {name: {name}}), (file:File {name: {file}})
                        MERGE (file)-[edge:AFFECTED]->(tc)
                        ON CREATE SET edge.counter = 1
                        ON MATCH SET edge.counter = edge.counter + 1
                    """.trimIndent()
                    tx.run(statement, mapOf("name" to tc.name, "file" to file))
                }
            }
        }
    }

    override fun close() {
        session.close()
        driver.close()
    }
}
