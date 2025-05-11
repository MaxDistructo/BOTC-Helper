package io.dedyn.engineermantra.botchelper.bot

import io.dedyn.engineermantra.botchelper.bot.BotMain.managerStoryteller
import io.dedyn.engineermantra.botchelper.models.ClocktowerJson
import io.dedyn.engineermantra.shared.discord.DiscordUtils
import io.dedyn.engineermantra.shared.discord.DiscordUtils.getMentionedUser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.messages.MessagePollData
import org.json.JSONArray
import java.net.URL
import java.time.Duration
import kotlin.random.Random

class SlashCommandListenerAdapter: ListenerAdapter() {
    var grimLink: MutableMap<Long, String> = mutableMapOf()
    var stQueue: MutableList<Member> = mutableListOf()
    var rolesJson: JSONArray = JSONArray()
    var fabledJson: JSONArray = JSONArray()
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
            "admin" -> handleAdminCommands(event)
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
            when(splitMsg[0].lowercase()){
                "*spec" -> spectateMember(event)
                "*unspec" -> unspectate(event)
                "*!" -> spectateMember(event)
                "*st" -> nicknameMember(event.member, "ST", Position.PREFIX, true)
                "*co-st" -> nicknameMember(event.member, "Co-ST", Position.PREFIX, true)
                "*cost" -> nicknameMember(event.member, "Co-ST", Position.PREFIX, true)
                "*brb" -> nicknameMember(event.member, "BRB", Position.POSTFIX, true)
                "*afk" -> nicknameMember(event.member, "AFK", Position.POSTFIX, true)
                "*count" -> countPlayers(event)
                "*t" -> nicknameMember(event.member, "T", Position.PREFIX, true)
                "*grim" -> grimCommand(event)
                "*poll" -> createPoll(event, false)
                "*pollc" -> createPoll(event, true)
                "*n" -> nicknameMember(event.member, "N", Position.POSTFIX, false)
                "*consult" -> event.message.addReaction(Emoji.fromUnicode("U+2714")).queue()
                "*rp" -> randomPlayer(event)
                "*join" -> joinOrLeaveQueue(event)
                "*joinqueue" -> joinOrLeaveQueue(event)
                "*leave" -> joinOrLeaveQueue(event)
                "*leavequeue" -> joinOrLeaveQueue(event)
                "*queue" -> checkQueue(event)
                "*start" -> startGame(event)
                "*end" -> endGame(event)
                "*role" -> getRoleInfo(event)
                "<@&1273992810969698334>" -> notifyMembers(event)
            }
        }
        if(event.isFromGuild && event.message.contentDisplay.contains("https://clocktower.live/#") || event.message.contentDisplay.contains("https://clocktower.online/#")){
            if(event.message.member!!.effectiveName.startsWith("(ST)") || event.message.member!!.effectiveName.startsWith("(Co-ST)")) {
                grimLink[event.message.author.idLong] = event.message.contentDisplay
            }
        }
    }




    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if(!event.isFromGuild) return
        if(event.user!!.isBot) return
        val message = event.retrieveMessage().complete()
        if(message.contentRaw == "*consult" && event.member!!.effectiveName.startsWith("(ST)")){
            for(vc in event.guild.voiceChannels){
                if(vc.members.contains(message.member) && vc.parentCategoryId == "1165358625674510357"){
                    val stPrivate = event.guild.getVoiceChannelById(1165358638664269986)
                    event.guild.moveVoiceMember(event.member!!, stPrivate).queue()
                    event.guild.moveVoiceMember(message.member!!, stPrivate).queue()
                }
            }
        }
    }


    override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
        if(event.oldNickname == null || event.newNickname == null) return
        if ((event.oldNickname!!.startsWith("(ST)") && !event.newNickname!!.startsWith("(ST)")) ||
            (event.oldNickname!!.startsWith("(Co-ST)") && !event.newNickname!!.startsWith("(Co-ST)")))
            {
                //Storyteller role
                var role = event.guild.getRolesByName("Storyteller", true).firstOrNull()
                if(role != null){
                    event.guild.removeRoleFromMember(event.member, role).queue()
                }
                grimLink[event.member.idLong] = ""
            }
        if(event.newNickname!!.startsWith("(ST)") || event.newNickname!!.startsWith("(Co-ST)")){
            var role = event.guild.getRolesByName("Storyteller", true).firstOrNull()
            if(role != null){
                event.guild.addRoleToMember(event.member, role).queue()
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

    fun spectateMember(event: MessageReceivedEvent) {
        val messageParts = event.message.contentRaw.split(" ")
        if (messageParts.size == 1) {
            //check if user is in VC
            var memberVC: VoiceChannel? = null
            for (vc in event.guild.voiceChannels) {
                if (vc.members.contains(event.member)) {
                    memberVC = vc
                    break
                }
            }
            if (memberVC == null) {
                event.channel.sendMessage("You must be in a voice channel to spectate someone").queue()
                return
            }
            val isSpectator = event.member!!.effectiveName.startsWith('!')
            if (!isSpectator) {
                val memberName = event.member!!.effectiveName
                    .removePrefixes(knownPrefixes)
                event.member!!.modifyNickname("!$memberName").queue()
            } else {
                event.member!!.modifyNickname(event.member!!.effectiveName.substring(1)).complete()
                removeMemberFromSpectatorMap(event.member!!.idLong)
            }
        }
        else {
            val targetUser: Member? = event.message.getMentionedUser()
            if (targetUser != null) {
                addMemberToSpectatorMap(targetUser.idLong, event.member!!.idLong)
                event.channel.sendMessage("You are now spectating ${targetUser.asMention}").queue()
            } else {
                event.channel.sendMessage("I cannot find the user you have mentioned!").queue()
            }
        }
    }

    private fun notifyMembers(event: MessageReceivedEvent) {
        if(event.message.author.idLong == 872998851013918760){
            var member = event.guild.getMemberById(228111371965956099)
            val privateChannel = member?.user?.openPrivateChannel()?.complete()
            privateChannel?.sendMessage("NEW BOTC GAME. WAKE UP!!!!!!!")?.queue()
        }
    }

    private fun String.removePrefixes(prefixes: List<String>): String {
        var result = this
        for (prefix in prefixes) {
            if (result.startsWith(prefix)) {
                result = result.removePrefix(prefix)
            }
        }
        return result
    }

    private fun removeMemberFromSpectatorMap(memberId: Long) {
        for (entry in BotMain.spectatorMap) {
            if (entry.value.contains(memberId)) {
                BotMain.spectatorMap[entry.key]!!.remove(memberId)
            }
        }
    }

    private fun addMemberToSpectatorMap(targetUserId: Long, memberId: Long) {
        if (BotMain.spectatorMap[targetUserId].isNullOrEmpty()) {
            BotMain.spectatorMap[targetUserId] = mutableListOf()
        }
        BotMain.spectatorMap[targetUserId]!!.add(memberId)
        //Only allow spectating one person at a time
        for(entry in BotMain.spectatorMap){
            if(entry.value.contains(memberId) && entry.key != targetUserId){
                entry.value.remove(memberId)
            }
        }
    }

    fun spectateMember(event: SlashCommandInteractionEvent){
        if(!event.member!!.effectiveName.startsWith('!')){
            event.member!!.modifyNickname("!" + event.member!!.effectiveName).complete()
            event.reply("You have been marked as a spectator").complete()
        }
        if(event.getOption("user") != null){
            //BotMain.spectatorMap[event.member!!.idLong] = event.getOption("user")!!.asMember!!.idLong
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

    val knownPrefixes: List<String> = listOf("!", "(ST) ", "(Co-ST) ", "(T) ")
    val knownSuffixes: List<String> = listOf(" [BRB]", " [AFK]", " [N]")

    fun nicknameMember(member: Member?, addition: String, position: Position, removeOld: Boolean){
        if(member == null) return
        var memberName = member.effectiveName;

        //If removeOld is toggled on, default true)
        if(removeOld && position == Position.PREFIX){
            //Loop over all prefixes
            for(prefix in knownPrefixes){
                //If the name of the player starts with the prefix and it is not what we want to add, remove the prefix
                if(memberName.startsWith(prefix) && prefix != addition){
                    memberName = memberName.removePrefix(prefix)
                }
            }
        }
        if(removeOld && position == Position.POSTFIX){
            for(prefix in knownSuffixes) {
                if (memberName.endsWith(prefix) && prefix != addition) {
                    if(prefix != "[N]" && memberName.endsWith("[N]")){
                        //ignore new tag if we're not adding/removing the new tag
                    }
                    else{
                        memberName = memberName.removeSuffix(prefix)
                    }
                }
            }
        }
        if(position == Position.PREFIX){
            //If the memberName starts with what we want to add, remove it
            if(!member.effectiveName.startsWith("($addition) ")){
                member.modifyNickname("($addition) $memberName").queue()
            }
            else{
                member.modifyNickname(memberName).queue()
            }
        }
        else if(position == Position.POSTFIX){
            if(!member.effectiveName.endsWith(" [$addition]")){
                member.modifyNickname("$memberName [$addition]").queue()
            }
            else{
                member.modifyNickname(memberName).queue()
            }
        }
    }

    fun countPlayers(event: MessageReceivedEvent){
        val voiceChannelMembers = mutableListOf<Member>()
        var voiceCategory: Category? = null

        for (voiceChannel in event.guild.voiceChannels) {
            if (voiceChannel.members.contains(event.member)) {
                voiceCategory = voiceChannel.parentCategory
                break
            }
        }

        if (voiceCategory == null) {
            return
        }

        for (channel in voiceCategory.channels) {
            if (channel is VoiceChannel) {
                voiceChannelMembers.addAll(channel.members)
            }
        }

        val filteredMembers = voiceChannelMembers.filter { !it.effectiveName.startsWith("!") && !it.effectiveName.startsWith("(Co-ST)") && !it.effectiveName.startsWith("(ST)") && !it.effectiveName.startsWith("(T)") }
        val travelerCount = voiceChannelMembers.count { it.effectiveName.startsWith("(T)") }

        if (filteredMembers.size < 5) {
            event.channel.sendMessage("There is not enough members for a game").queue()
            return
        }

        var minionCount = 0
        var outsiderCount = 0
        var townsfolkCount = 3

        while (1 + minionCount + outsiderCount + townsfolkCount < filteredMembers.size) {
            if (outsiderCount == 2) {
                minionCount++
                outsiderCount = 0
                townsfolkCount += 2
            } else {
                outsiderCount++
            }
        }

        event.channel.sendMessage("*>> The current composition of ${filteredMembers.size + travelerCount} players should typically be:*\n- $townsfolkCount Townsfolk\n- $outsiderCount Outsider(s)\n- $minionCount Minion(s)\n- 1 Demon\n- $travelerCount Travelers").queue()
    }

    fun grimCommand(event: MessageReceivedEvent){
        val splitMessage = event.message.contentRaw.split(' ')
        if(splitMessage.size == 2){
            //Check if they are ST
            if(event.member!!.effectiveName.startsWith("(ST)")){
                grimLink[event.member!!.idLong] = splitMessage[1]
            }
        }
        else{
            for(st in grimLink.keys){
                if(grimLink[st] != null && grimLink[st] != ""){
                    event.channel.sendMessage("Current Grim: ${grimLink[st]!!}").queue()
                    return
                }
            }
            event.channel.sendMessage("Grim has not been provided").queue()
        }
    }

    fun unspectate(event: MessageReceivedEvent){
        if(event.message.contentRaw.split(" ").size == 2) {
            val optionalUser: Member? = event.message.getMentionedUser()
            if (optionalUser != null) {
                if (BotMain.spectatorMap[optionalUser.idLong].isNullOrEmpty()) {
                    BotMain.spectatorMap[optionalUser.idLong] = mutableListOf()
                }
                //Previous check enforces this is not null
                BotMain.spectatorMap[optionalUser.idLong]!!.remove(event.member!!.idLong)
                event.channel.sendMessage("You are no longer spectating spectating ${optionalUser.effectiveName}").queue()
            } else {
                event.channel.sendMessage("I cannot find the user you have mentioned!").queue()
            }
        }
        else{
            for(value in BotMain.spectatorMap){
                if(value.value.contains(event.member!!.idLong)){
                    BotMain.spectatorMap[value.key]!!.remove(event.member!!.idLong)
                }
            }
            event.channel.sendMessage("You are no longer spectating anyone").queue()
        }
    }

    fun createPoll(event: MessageReceivedEvent, addCustom: Boolean){
        val pollData = MessagePollData.builder("Which Script?")
            .addAnswer("Trouble Brewing (TB)")
            .addAnswer("Bad Moon Rising (BMR)")
            .addAnswer("Sects and Violets (S&V)")
            .setDuration(Duration.ofHours(1))
            .setMultiAnswer(true)
        if(addCustom){
            pollData.addAnswer("Custom")
        }
        event.channel.sendMessage("${event.member!!.effectiveName} Asks:")
            .setPoll(pollData.build())
            .queue()
    }

    fun randomPlayer(event: MessageReceivedEvent) {
        val channelMembers = mutableListOf<Member>()
        var category: Category? = null
        for(vc in event.guild.voiceChannels) {
            if(vc.members.contains(event.member)) {
                category = vc.parentCategory
            }
        }
        if(category == null) {
            return
        }
        for(channel in category.channels) {
            if(channel is VoiceChannel) {
                channelMembers.addAll(channel.members)
            }
        }
        var travelerCount = 0
        var members = mutableListOf<Member>();
        for(member in channelMembers){
            if(!member.effectiveName.startsWith("!") && !member.effectiveName.startsWith("(Co-ST)") && !member.effectiveName.startsWith("(ST)") && !member.effectiveName.startsWith("(T)")){
                members.add(member)
            }
            if(member.effectiveName.startsWith("(T)")) {
                travelerCount++
            }
        }
        var random = Random(System.currentTimeMillis())
        var randomMember = members[random.nextInt(members.count())]
        event.channel.sendMessage("${randomMember.effectiveName} has been selected").queue()
    }

    fun joinOrLeaveQueue(event: MessageReceivedEvent) {
        if(!event.isFromGuild) return
        if(stQueue.contains(event.member)){
            stQueue.remove(event.member)
            event.message.reply("You have left the ST Queue").queue()
        }
        else{
            stQueue.add(event.member!!)
            event.message.reply("You have joined the ST Queue").queue()
        }
    }

    fun startGame(event: MessageReceivedEvent) {
        if(!event.isFromGuild) return
        if(stQueue.first() == event.member || event.member!!.hasPermission(Permission.MESSAGE_MANAGE)){
            stQueue.remove(event.member)
            if(event.member != stQueue.first()){
                event.channel.sendMessage("${event.member!!.effectiveName}} is starting the game for ${stQueue.first().user.globalName} (${stQueue.first().asMention})").queue()
            }
            else{
                event.channel.sendMessage("${event.member!!.effectiveName} is starting the game").queue()
                createPoll(event, true)
            }
        }
    }

    fun endGame(event: MessageReceivedEvent) {
        if(!event.isFromGuild) return
        //var random = Random(System.currentTimeMillis())
        if(stQueue.first() == event.member || event.member!!.hasPermission(Permission.MESSAGE_MANAGE)){
            event.channel.sendMessage("${stQueue.first().asMention} has completed their game. Pinging next ST.").queue()
            //Make this more frequent if people keep forgetting to do this
            event.channel.sendMessage("Reminder to thank your ST!").queue()
            stQueue.remove(stQueue.first())
            event.channel.sendMessage("Next ST: ${stQueue.first().asMention}").queue()
        }
    }

    fun checkQueue(event: MessageReceivedEvent) {
        if (!event.isFromGuild) return
        if (stQueue.isEmpty()) {
            event.channel.sendMessage("ST Queue is empty").queue()
        }
        else {
            val stQueueString = StringBuilder()
            stQueueString.append("Current ST Queue:\n")
            for (member in stQueue) {
                stQueueString.append("${member.effectiveName}\n")
            }
            event.channel.sendMessageEmbeds(DiscordUtils.simpleTitledEmbed(event.member!!, "ST Queue", stQueueString.toString(), event.guild)).queue()
        }
    }


    fun getRoleDetails(roleName: String): Map<String, String?> {
        // URL for the JSON file
        val url = "https://raw.githubusercontent.com/nicholas-eden/townsquare/refs/heads/develop/src/roles.json"
        val fabledURL = "https://raw.githubusercontent.com/nicholas-eden/townsquare/refs/heads/develop/src/fabled.json"
        var jsonString = URL(url).readText()
        if(rolesJson.isEmpty){
            rolesJson = JSONArray(jsonString)
        }
        if(fabledJson.isEmpty){
            jsonString = URL(fabledURL).readText()
            fabledJson = JSONArray(jsonString)
        }
        val combinedArray = JSONArray()
        for (i in 0 until rolesJson.length()) {
            combinedArray.put(rolesJson.getJSONObject(i))
        }
        for (i in 0 until fabledJson.length()) {
            try {
                combinedArray.put(fabledJson.getJSONObject(i))
            } catch (e: Exception) {
                println("Failed to add role to combinedArray: ${fabledJson.getJSONObject(i)}")
            }
        }
        for (i in 0 until combinedArray.length()) {
            val roleObject = combinedArray.getJSONObject(i)
            if (roleObject.optString("id") == roleName.lowercase()) {
                return mapOf(
                    "edition" to when (roleObject.optString("edition", "").lowercase()) {
                        "bmr" -> "Blood Moon Rising"
                        "tb" -> "Trouble Brewing"guild
                        "snv" -> "Sects and Violets"
                        else -> if (roleObject.optString("edition", "").isBlank()) "Experimental" else roleObject.optString("edition", "")
                    },
                    "team" to roleObject.optString("team", null),
                    "setup" to roleObject.optString("setup", null),
                    "ability" to roleObject.optString("ability", null),
                    "name" to roleObject.optString("name", null)
                )
            }
        }
        return mapOf(
            "edition" to null,
            "team" to null,
            "setup" to null,
            "ability" to null,
            "name" to null
        )
    }

    fun String.capitalizeWords(): String {
        return this.split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
    }


    fun getRoleInfo(event: MessageReceivedEvent) {
        val roleName = event.message.contentRaw.split(" ").drop(1).joinToString("")
        val roleInfo = getRoleDetails(roleName)
        if(roleInfo["edition"] != null){
            val embed = EmbedBuilder()
                    .setTitle("${roleInfo["name"]} (${roleInfo["team"]?.capitalizeWords()}) - ${roleInfo["edition"]}")
                    .setDescription("${roleInfo["ability"]?.capitalizeWords()}\n\nAffects Setup: ${roleInfo["setup"]?.capitalizeWords()}")
                    .setUrl("https://wiki.bloodontheclocktower.com/$roleName")
                    .build()
            event.channel.sendMessageEmbeds(embed).queue()
        }
        else{
            event.channel.sendMessage("Role not found").queue()
        }
    }


    fun handleAdminCommands(event: SlashCommandInteractionEvent) {
        when(event.subcommandName){
            "nextst" -> {
                val user = event.getOption("user")?.asMember
                if (user != null) {
                    val frontMember = stQueue.firstOrNull()
                    if (frontMember == null || !frontMember.effectiveName.startsWith("(ST)")) {
                        stQueue.add(0, user)
                        event.reply("${user.effectiveName} has been added to the front of the queue").queue()
                    } else {
                        stQueue.add(1, user)
                        event.reply("${user.effectiveName} has been added to the second position in the queue").queue()
                    }
                } else {
                    event.reply("User not found").setEphemeral(true).queue()
                }
            }
            "removest" -> {
                val user = event.getOption("user")?.asMember
                if (user != null) {
                    if (stQueue.remove(user)) {
                        event.reply("${user.effectiveName} has been removed from the queue").queue()
                    } else {
                        event.reply("${user.effectiveName} is not in the queue").setEphemeral(true).queue()
                    }
                } else {
                    event.reply("User not found").setEphemeral(true).queue()
                }
            }
            "addst" -> {
                val user = event.getOption("user")?.asMember
                if(user != null){
                    if(stQueue.contains(user)){
                        event.reply("${user.effectiveName} is already in the queue").setEphemeral(true).queue()
                    }
                    else{
                        stQueue.add(user)
                        event.reply("${user.effectiveName} has been added to the queue").queue()
                    }
                }
                else{
                    event.reply("User not found").setEphemeral(true).queue()
                }
            }
        }

    }
    fun handleCreateGrim(event: SlashCommandInteractionEvent){
        val channelMembers = mutableListOf<Member>()
        if(event?.guild == null) event.reply("This command is guild ONLY!").queue()
        var category: Category? = null
        for(vc in event.guild!!.voiceChannels) {
            if(vc.members.contains(event.member)) {
                category = vc.parentCategory
            }
        }
        if(category == null) {
            event.reply("Category not found. Notify <@!228111371965956099> if you haven't seen this before").queue()
            return
        }
        for(channel in category.channels) {
            if(channel is VoiceChannel) {
                channelMembers.addAll(channel.members)
            }
        }
        var members = mutableListOf<Member>();
        for(member in channelMembers){
            if(!member.effectiveName.startsWith("!") && !member.effectiveName.startsWith("(Co-ST)") && !member.effectiveName.startsWith("(ST)")){
                members.add(member)
            }
        }
        var gameState = ClocktowerJson.GameState(
            bluffs = mutableListOf("null", "null", "null"),
            edition = ClocktowerJson.Edition(id = "tb"),
            roles = "",
            fabled = mutableListOf(),
            players = mutableListOf()
        )
        for(member in members){
            val player = ClocktowerJson.Player(
                name = member.effectiveName,
                id = "",
                connected = true,
                role = null,
                alignmentIndex = 0,
                reminders = emptyList(),
                isVoteless = false,
                hasTwoVotes = false,
                hasResponded = null,
                isDead = false,
                pronouns = "",
            )
            gameState.players.add(player)
        }
        event.reply("Grim JSON: ```json\n${Json.encodeToJsonElement(gameState)}```").queue()
    }

}


