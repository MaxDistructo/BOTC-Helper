package io.dedyn.engineermantra.botchelper.bot

import io.dedyn.engineermantra.botchelper.bot.BotMain.managerStoryteller
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command
import kotlin.io.path.Path


class SlashCommandListenerAdapter: ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val time = System.currentTimeMillis()
        when(event.name){
            "ping"-> event.reply("Pong!").setEphemeral(true).flatMap{
                event.hook.editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)} // then edit original
                .queue()
            "summon" -> summonToVC(event)
            "goto" -> gotoMember(event)
            "spec" -> spectateMember(event)
            "grim" -> handleGrim(event)
            "storyteller" -> handleStoryteller(event)
            "cottage" -> handleCottage(event)
            else -> println("Command not found ${event.name}")
        }
    }

    enum class Position{
        PREFIX,
        POSTFIX
    }

    //Oldschool message parsing!!!! YAYYYYY /s
    override fun onMessageReceived(event: MessageReceivedEvent){
        if(event.message.contentRaw.startsWith("*")){
            var splitMsg = event.message.contentRaw.split(" ");
            when(splitMsg[0]){
                "*spec" -> spectateMember(event)
                "*!" -> spectateMember(event)
                "*st" -> nicknameMember(event.member, "ST", Position.PREFIX)
                "*co-st" -> nicknameMember(event.member, "Co-ST", Position.PREFIX)
                "*brb" -> nicknameMember(event.member, "BRB", Position.POSTFIX)
                "*afk" -> nicknameMember(event.member, "AFK", Position.POSTFIX)
                "*count" -> countPlayers(event)
            }
        }
        if(event.message.contentRaw.lowercase().contains(" thanks") || event.message.contentRaw.lowercase().contains(" ty ") || event.message.contentRaw.lowercase().contains("thank you")){
            //TODO: Implement thanks feature. Needs DB of some kind
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

    fun spectateMember(event: MessageReceivedEvent){
        if(!event.member!!.effectiveName.startsWith('!')){
            event.member!!.modifyNickname("!" + event.member!!.effectiveName).complete()
            event.channel.sendMessage("You have been marked as a spectator").complete()
        }
        var optionalUser: Member? = null
        if(event.message.mentions.membersBag.count() == 1) {
            optionalUser = event.message.mentions.membersBag.first()
        }
        if(optionalUser != null){
            BotMain.spectatorMap[event.member!!.idLong] = optionalUser.idLong
            event.channel.sendMessage("You are now spectating ${optionalUser.asMention}").complete()
        }
    }

    fun spectateMember(event: SlashCommandInteractionEvent){
        if(!event.member!!.effectiveName.startsWith('!')){
            event.member!!.modifyNickname("!" + event.member!!.effectiveName).complete()
            event.reply("You have been marked as a spectator").complete()
        }
        if(event.getOption("user") != null){
            BotMain.spectatorMap[event.member!!.idLong] = event.getOption("user")!!.asMember!!.idLong
            event.reply("You are now spectating ${event.getOption("user")!!.asMember!!.asMention}").complete()
        }
    }

    fun handleGrim(event: SlashCommandInteractionEvent){
        if(BotMain.grimLink == ""){
            event.reply("There is no grim currently set").queue()
        }
        else{
            event.reply("The current grim is: ${BotMain.grimLink}").queue()
        }
    }

    fun handleStoryteller(event: SlashCommandInteractionEvent){
        when(event.subcommandName){
            "claim" -> {
                if(managerStoryteller != 0L && !event.member!!.hasPermission(Permission.MANAGE_ROLES)){
                    event.reply("You cannot claim to be the storyteller as it is currently claimed by <@${managerStoryteller}>. Please ask them to release the role.").queue()
                }
                else{
                    val storytellerRole = event.guild!!.getRolesByName("Storyteller", true).first()
                    val membersWithRole = event.guild!!.getMembersWithRoles(storytellerRole)
                    for(member in membersWithRole){
                        event.guild!!.removeRoleFromMember(member, storytellerRole).queue()
                    }
                    if(event.member!!.idLong != event.guild!!.ownerIdLong) {
                        event.member!!.modifyNickname("(ST) " + event.member!!.effectiveName).complete()
                        event.guild!!.addRoleToMember(event.member!!, storytellerRole).queue()
                    }
                    event.reply("You are now the storyteller").queue()
                }
            }
            "release" -> {
                if(event.member!!.idLong == managerStoryteller || event.member!!.hasPermission(Permission.MANAGE_ROLES)){
                    managerStoryteller = 0L
                    event.reply("You are no longer the storyteller").queue()
                }
                if(event.member!!.hasPermission(Permission.MANAGE_ROLES)){
                    managerStoryteller = 0L
                    event.reply("The storyteller role has been released").queue()
                }
            }
            "promote" -> {
                if(event.member!!.idLong == managerStoryteller){
                    val storytellerRole = event.guild!!.getRolesByName("Storyteller", true).first()
                    event.guild!!.addRoleToMember(event.getOption("cohost")!!.asMember!!, storytellerRole).queue()
                    event.reply("You have added <@${event.getOption("cohost")!!.asMember!!.idLong}>").queue()
                }
                else{
                    event.reply("You are not the storyteller!").queue()
                }
            }
            "set_grim" -> {
                val storytellerRole = event.guild!!.getRolesByName("Storyteller", true).first()
                val membersWithRole = event.guild!!.getMembersWithRoles(storytellerRole)
                if(membersWithRole.contains(event.member)){
                    BotMain.grimLink = event.getOption("grim")!!.asString
                    event.reply("Link has been set").queue()
                    return
                }
                event.reply("You are not the storyteller!").queue()
            }
            "announce" -> {
                val storytellerRole = event.guild!!.getRolesByName("Storyteller", true).first()
                val membersWithRole = event.guild!!.getMembersWithRoles(storytellerRole)
                if(membersWithRole.contains(event.member)){
                    event.channel.sendMessage(event.getOption("message")!!.asString).queue()
                    return
                }
                event.reply("You are not the storyteller!").queue()
            }
        }
    }

    fun handleCottage(event: SlashCommandInteractionEvent){
        when(event.subcommandName){
            "send" -> moveToNight(event)
            "retrieve" -> moveToDay(event)
        }
    }

    fun nicknameMember(member: Member?, addition: String, position: Position){
        if(member == null) return
        if(position == Position.PREFIX){
            member.modifyNickname("($addition) ${member.nickname}").queue()
        }
        else if(position == Position.POSTFIX){
            member.modifyNickname("${member.nickname} [$addition]").queue()
        }
    }

    fun countPlayers(event: MessageReceivedEvent){

    }

}


