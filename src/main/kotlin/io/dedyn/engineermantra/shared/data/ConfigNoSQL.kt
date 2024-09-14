package io.dedyn.engineermantra.shared.data

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import java.net.InetSocketAddress

object ConfigNoSQL: ConfigFileInterface {

    var session: CqlSession? = null

    override fun load() {
        session = CqlSession.builder()
            .addContactPoint(InetSocketAddress("cassandra", 9042))
            .withLocalDatacenter("datacenter")
            .withKeyspace(CqlIdentifier.fromCql("botchelper"))
            .build()
    }

    override fun save() {
        session?.close()
    }

    override fun get(token: String): String? {
        session?.execute("SELECT $token FROM thanks")
        return ""
    }

    override fun set(token: String, value: String) {
        TODO("Not yet implemented")
    }
}