package io.dedyn.engineermantra.botchelper.bot

import ch.qos.logback.classic.Logger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

object BotMain {
    val imageProcessing: Boolean = false
    val bot_name = "BOTCHelper"
    lateinit var jda: JDA
    lateinit var logger: Logger
    val voiceCache = mutableMapOf<Long, Long>()
    var managerStoryteller: Long = 0L

    fun run()
    {
        logger.info("Adding slash commands")
        jda.addEventListener(SlashCommandListenerAdapter())
        //Parse the known commands that we have and register any new ones but not do the old
        val commandNames = mutableListOf<String>()
        val commands = jda.retrieveCommands().complete()
        if(!commandNames.contains("give_all_role")) {
            jda.upsertCommand(
                Commands.slash("give_all_role", "Give all members a role")
                    .setGuildOnly(true)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
                    .addOption(OptionType.ROLE, "role", "The role to give", true, false)
                    .addOption(OptionType.ROLE, "role2", "1st restriction", false, false)
                    .addOption(OptionType.ROLE, "role3", "2nd restriction", false, false)
                    .addOption(OptionType.ROLE, "role4", "3rd restriction", false, false)
                    .addOption(OptionType.ROLE, "role5", "4th restriction", false, false)
                    .addOption(
                        OptionType.BOOLEAN,
                        "test",
                        "Enable test mode to calculate without actually doing the role add",
                        false,
                        false
                    )
            ).complete()
        }
        jda.getGuildById(1165357291629989979)!!.upsertCommand(
            Commands.slash("goodnight", "Sends members to SLEEP")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .setGuildOnly(true)
        ).complete()
        jda.getGuildById(1165357291629989979)!!.upsertCommand(
            Commands.slash("goodmorning", "Wakes everyone up")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .setGuildOnly(true)
        ).complete()
        jda.getGuildById(1165357291629989979)!!.upsertCommand(
            Commands.slash("summon", "Summon all VC members")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .setGuildOnly(true)
        ).complete()
        jda.getGuildById(1165357291629989979)!!.upsertCommand(
            Commands.slash("goto", "Goto Person in VC")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .setGuildOnly(true)
                .addOption(OptionType.USER, "user", "The user to go to")
        ).complete()
        logger.info("Finished adding slash commands ")
    }

}