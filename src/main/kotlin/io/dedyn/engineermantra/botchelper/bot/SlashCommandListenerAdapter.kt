package io.dedyn.engineermantra.botchelper.bot

import io.dedyn.engineermantra.shared.data.ConfigMySQL
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command


class SlashCommandListenerAdapter: ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val time = System.currentTimeMillis()
        when(event.name){
            "ping"-> event.reply("Pong!").setEphemeral(true).flatMap{
                event.hook.editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)} // then edit original
                .queue()
            "echo" -> event.reply(event.getOption("message")!!.asString).queue()
            "goodmorning" -> moveToDay(event)
            "goodnight" -> moveToNight(event)
            "summon" -> summonToVC(event)
            "goto" -> gotoMember(event)
            "promote" -> promoteMember(event)
            else -> println("Command not found ${event.name}")
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        when(event.focusedOption.name)
        {
            "permission" -> event.replyChoices(choices(event.focusedOption.value, "permissions")).queue()
            "strike_type" -> event.replyChoices(choices(event.focusedOption.value, "strike_type")).queue()
            "game" -> event.replyChoices(choices(event.focusedOption.value, "game")).queue()
            else -> println("Autocomplete not found. Please check your command configuration. Missing: ${event.focusedOption.name}")
        }
    }

    //This is part of the AutoComplete code
    fun choices(partial_word: String, autocompleteName: String): List<Command.Choice>
    {
        var words: List<String>
        val type = listOf("Server", "Event", "Soundboard")
        val games = listOf("Town of Salem 2", "Town of Salem", "Minecraft", "Steam", "VR Chat", "Blizzard", "Epic")

        when(autocompleteName){
            "strike_type" -> words = type
            "game" -> words = games
            else -> words = listOf()
        }
        val outputList = mutableListOf<Command.Choice>()
        words.forEach{word -> if(word.startsWith(partial_word)) {outputList.add(Command.Choice(word, word))}}
        return outputList
    }

    /**
     * Catches when someone is given the Booster role so and DMs them with the information on how to use the perks
     * we provide to them for doing so.
     */
    override fun onGuildMemberRoleAdd(event: GuildMemberRoleAddEvent)
    {
        if(event.roles.contains(event.guild.boostRole))
        {
            //New Booster! Send them the information about the booster perks
            val dms = event.member.user.openPrivateChannel().complete()
            dms.sendMessage("**Thank You for boosting Salem Central!**\n\n" +
                    "While you are boosting Salem Central, you will get the following perks. If there is another perk " +
                    "added, it will be announced.\n" +
                    "**Access to a Custom Role**\n" +
                    "   - You may create your own custom role using the ```/role``` command with" +
                    "${event.jda.selfUser.asMention}. This role may be updated as many times as you please with a custom color" +
                    "and icon.\n" +
                    "**Server Emoji/Soundboard**\n"+
                    "   - Upon request, we will add almost any emoji or soundboard sound you wish. This is subject to staff" +
                    "approval though as we cannot automate it."
            ).queue()
            val booster_info = ConfigMySQL.getBoosterItem(event.member.idLong, event.guild.idLong)
            if(booster_info == null)
            {
                val role = event.guild.createRole().setName(event.member.effectiveName).complete()
                event.guild.addRoleToMember(event.member, role).queue()
                ConfigMySQL.addBoosterItem(event.member.idLong, event.guild.idLong, role.idLong)
            }
        }
    }
    /**
     * Blood on the Clocktower Server commands
     * These are NOT enabled in most servers.
     */
    fun moveToNight(event: SlashCommandInteractionEvent){
        event.reply("GO TO SLEEP!").queue()
        val vc_members = event.guild!!.getVoiceChannelById(1165358627209617588L)!!.members
        val cottages = event.guild!!.getVoiceChannelsByName("\uD83D\uDECC Cottage", false)
        var cottageNum = 0
        for (i in vc_members.indices)
        {
            if(!vc_members[i].user.isBot) {
                println("moving ${vc_members[i].effectiveName}")
                event.guild!!.moveVoiceMember(vc_members[i], cottages[cottageNum]).queue()
                cottageNum++
            }
        }
    }
    fun moveToDay(event: SlashCommandInteractionEvent)
    {
        event.reply("Wake Up!").queue()
        val cottages = event.guild!!.getVoiceChannelsByName("\uD83D\uDECC Cottage", false)
        val dayChannel = event.guild!!.getVoiceChannelById(1165358627209617588L)
        for(cottage in cottages){
            for(member in cottage.members){
                event.guild!!.moveVoiceMember(member, dayChannel).queue()
            }
        }
    }

    fun summonToVC(event: SlashCommandInteractionEvent)
    {
        event.reply("Summoning").queue()
        var author_vc: VoiceChannel? = null;
        val membersToMove = mutableListOf<Member>()
        for(vc in event.guild!!.voiceChannels) {
            if (vc.members.contains(event.member))
            {
                author_vc = vc
            }
            else{
                if(author_vc != null)
                {
                    for(member in vc.members) {
                        event.guild!!.moveVoiceMember(member, author_vc).queue()
                    }
                }
                else{
                    membersToMove.addAll(vc.members)
                }
            }
        }
        for(member in membersToMove)
        {
            event.guild!!.moveVoiceMember(member, author_vc).queue()
        }
    }
    fun gotoMember(event: SlashCommandInteractionEvent)
    {
        for(vc in event.guild!!.voiceChannels)
        {
            if(vc.members.contains(event.getOption("user")!!.asMember))
            {
                event.guild!!.moveVoiceMember(event.member!!, vc)
                return
            }
        }
    }

    fun promoteMember(event: SlashCommandInteractionEvent) {
        if(event.guild == null)
        {
            return
        }
        val storytellerRole = event.guild!!.getRoleById(1165387353787990147L)!!
        val frequentStoryteller = event.guild!!.getRoleById(1167701898212683786L)!!
        if((event.member!!.hasPermission(Permission.MANAGE_ROLES) || event.member!!.roles.contains(frequentStoryteller)) && (!(event.member!!.roles.contains(storytellerRole)) || event.getOption("force")?.asBoolean == true))
        {
            for (member in event.guild!!.getMembersWithRoles(storytellerRole)) {
                if (member.idLong != event.member!!.idLong) {
                    event.guild!!.removeRoleFromMember(member, storytellerRole).queue()
                }
            }
            if(event.getOption("user") == null) {
                event.reply("You have taken control as the primary storyteller").queue()
                event.guild!!.addRoleToMember(event.member!!, storytellerRole).queue()
                BotMain.managerStoryteller = event.member!!.idLong
                event.guild!!.modifyNickname(event.member!!, "[GM] ${event.member!!.effectiveName}").queue()
            }
            else{
                event.reply("You have made ${event.getOption("user")!!.asMember!!.effectiveName} the primary storyteller").queue()
                event.guild!!.addRoleToMember(event.getOption("user")!!.asMember!!, storytellerRole).queue()
                BotMain.managerStoryteller = event.getOption("user")!!.asMember!!.idLong
                event.guild!!.modifyNickname(event.getOption("user")!!.asMember!!, "[GM] ${event.getOption("user")!!.asMember!!.effectiveName}").queue()
            }
        }
        else if(event.member!!.roles.contains(storytellerRole) && event.member!!.idLong == BotMain.managerStoryteller)
        {
            val mentionedUser = event.getOption("user")!!.asMember
            if(mentionedUser != null)
            {
                if(mentionedUser.roles.contains(storytellerRole)){
                    BotMain.managerStoryteller = mentionedUser.idLong
                    event.reply("You have made ${mentionedUser.effectiveName} the primary storyteller").queue()
                    event.guild!!.modifyNickname(mentionedUser, "[GM] ${mentionedUser.effectiveName}").queue()
                    for(member in event.guild!!.getMembersWithRoles(storytellerRole))
                    {
                        if(member.idLong != mentionedUser.idLong)
                        {
                            event.guild!!.removeRoleFromMember(member, storytellerRole).queue()
                        }
                    }
                }
                else {
                    event.guild!!.addRoleToMember(mentionedUser, storytellerRole).queue()
                    event.guild!!.modifyNickname(mentionedUser, "[Helper] ${event.getOption("user")!!.asMember!!.effectiveName}").queue()
                    event.reply("You have added ${mentionedUser.effectiveName} as a co-host").queue()
                }
            }
            else{
                event.reply("You must specify someone to use this command on").queue()
            }
        }
    }
}


