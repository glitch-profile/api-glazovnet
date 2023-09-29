package net.glazov.data

import io.ktor.server.application.*
import io.ktor.server.config.*
import net.glazov.data.model.TariffModel
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

private val client = KMongo.createClient()
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<TariffModel>("Tariffs")