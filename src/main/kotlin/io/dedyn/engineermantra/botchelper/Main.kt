package io.dedyn.engineermantra.botchelper

import ch.qos.logback.classic.Logger
import io.dedyn.engineermantra.botchelper.bot.BotMain
import io.dedyn.engineermantra.shared.data.ConfigFileJson
import kotlinx.coroutines.CoroutineScope
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.Compression
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object Main{
    class BotScope: CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = EmptyCoroutineContext
    }
    @JvmStatic
    fun main(args: Array<String>){
        BotMain.logger = LoggerFactory.getLogger("BOTC Helper") as Logger
        BotMain.logger.info("Setting up JDA")
        val builder: JDABuilder = JDABuilder.create(
            ConfigFileJson.getToken(),
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.DIRECT_MESSAGES)
            .setBulkDeleteSplittingEnabled(false)
            .setCompression(Compression.NONE)
            .setActivity(Activity.playing("Use /help to get commands"))

        BotMain.logger.info("JDA Init")
        BotMain.jda = builder.build()
        BotMain.jda.awaitReady()
        BotMain.logger.info("JDA Init Complete")
        BotMain.run()
    }
}