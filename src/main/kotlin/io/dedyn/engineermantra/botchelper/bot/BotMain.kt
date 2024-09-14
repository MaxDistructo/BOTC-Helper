package io.dedyn.engineermantra.botchelper.bot

import ch.qos.logback.classic.Logger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

object BotMain {
    val bot_name = "BOTCHelper"
    lateinit var jda: JDA
    lateinit var logger: Logger
    var managerStoryteller: Long = 0L
    val spectatorMap = mutableMapOf<Long,MutableList<Long>>()
    var grimLink = ""

    fun run()
    {
        logger.info("Adding slash commands")
        jda.addEventListener(SlashCommandListenerAdapter())
        jda.addEventListener(SpectatorListenerAdapter())
        //Parse the known commands that we have and register any new ones but not do the old
        val commandNames = mutableListOf<String>()
        val commands = jda.retrieveCommands().complete()
        commands.stream().forEach { command -> commandNames.add(command.name) }
        for(command in commands){
            if(command.name == "storyteller"){
                command.delete().complete()
            }
        }
        if(!commandNames.contains("cottage"))
        {
            jda.upsertCommand(
                Commands.slash("cottage", "Bring members to and from a channel")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
                    .addSubcommands(
                        SubcommandData("send", "Send all active players to the cottages"),
                        SubcommandData("retrieve", "Retrieve all active players from the cottages")
                    )
            ).complete()
        }
        if(!commandNames.contains("storyteller"))
        {
            jda.upsertCommand(
                Commands.slash("storyteller", "Manage the current storyteller")
                .setGuildOnly(true)
                    .addSubcommands(
                        SubcommandData("claim", "Claim the storyteller role if one is not in existence"),
                        SubcommandData("release", "Stop being a storyteller (host or co-host)"),
                        SubcommandData("promote", "Promote a player to be a co-host")
                            .addOption(OptionType.USER, "cohost", "The player to be co-host", true),
                        SubcommandData("set_grim", "Set the URL to the current grim")
                            .addOption(OptionType.STRING, "grim", "The grim link", true),
                        SubcommandData("announce", "Announce a message to the town")
                            .addOption(OptionType.STRING, "message", "The message to announce", true)
                    )
            ).complete()
        }
        if(!commandNames.contains("grim"))
        {
            jda.upsertCommand(
                Commands.slash("grim", "Get the current grim link")
                .setGuildOnly(true)
            ).complete()
        }
        if(!commandNames.contains("spec"))
        {
            jda.upsertCommand(
                Commands.slash("spec", "Mark yourself as a spectator. If a user is provided, you will follow them around")
                    .setGuildOnly(true)
                    .addOption(OptionType.USER, "user", "The user that you will be spectating", false)
            ).complete()
        }
        if(!commandNames.contains("delete_history")){
            jda.upsertCommand(
                Commands.slash("delete_history", "Add this channel to have it's history deleted")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
            ).complete()
        }
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