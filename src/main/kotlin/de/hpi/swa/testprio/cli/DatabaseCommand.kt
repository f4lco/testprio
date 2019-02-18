package de.hpi.swa.testprio.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.int
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.postgresql.ds.PGSimpleDataSource

open class DatabaseCommand(
    name: String? = null,
    help: String = ""
) : CliktCommand(name = name, help = help) {

    val host by option("--host").default("localhost")
    val port by option("--port").int().default(5432)
    val db by option("--db").default("github")
    val user by option("--user")
    val password by option("--password", envvar = "PRIO_PW").prompt("Password", hideInput = true)

    override fun run() = Unit

    protected fun makeContext(): DSLContext {
        val source = PGSimpleDataSource()
        source.setUrl("jdbc:postgresql://$host:$port/$db")
        source.user = user
        source.password = password
        source.readOnly = true
        return DSL.using(source, SQLDialect.POSTGRES)
    }
}