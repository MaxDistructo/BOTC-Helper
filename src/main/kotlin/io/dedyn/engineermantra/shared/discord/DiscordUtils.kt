package io.dedyn.engineermantra.shared.discord

import io.dedyn.engineermantra.shared.data.ConfigMySQL
import io.dedyn.engineermantra.shared.MessageLevel
import io.dedyn.engineermantra.shared.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import java.time.Instant
import kotlin.math.min

object DiscordUtils {
    fun channelHasPermission(channel: GuildChannel, permission: Permission): Boolean
    {
        channel.permissionContainer.permissionOverrides.forEach{override ->
            if(override.allowed.contains(permission)){
                return true
            }
        }
        return false
    }

    fun permissionFromString(permission: String): Permission?
    {
        val permission_strings = listOf("MANAGE_CHANNEL", "MANAGE_SERVER", "VIEW_AUDIT_LOGS", "VIEW_CHANNEL", "VIEW_GUILD_INSIGHTS", "MANAGE_ROLES", "MANAGE_PERMISSIONS", "MANAGE_WEBHOOKS", "MANAGE_EMOJIS_AND_STICKERS", "MANAGE_EVENTS", "CREATE_INSTANT_INVITE", "KICK_MEMBERS", "BAN_MEMBERS", "NICKNAME_CHANGE", "NICKNAME_MANAGE", "MODERATE_MEMBERS", "MESSAGE_ADD_REACTION", "MESSAGE_SEND", "MESSAGE_TTS", "MESSAGE_MANAGE", "MESSAGE_EMBED_LINKS", "MESSAGE_ATTACH_FILES", "MESSAGE_HISTORY", "MESSAGE_MENTION_EVERYONE", "MESSAGE_EXT_EMOJI", "USE_APPLICATION_COMMANDS", "MESSAGE_EXT_STICKER", "MANAGE_THREADS", "CREATE_PUBLIC_THREADS", "CREATE_PRIVATE_THREADS", "MESSAGE_SEND_IN_THREADS", "PRIORITY_SPEAKER", "VOICE_STREAM", "VOICE_CONNECT", "VOICE_SPEAK", "VOICE_MUTE_OTHERS", "VOICE_DEAF_OTHERS", "VOICE_MOVE_OTHERS", "VOICE_USE_VAD", "VOICE_START_ACTIVITIES", "REQUEST_TO_SPEAK", "ADMINISTRATOR")
        return null
    }

    /**
     *
     * Creates an embed with the provided parameters. This is essentially the simpleEmbed but with a title
     * @param user The User who we want to be the author of this embed.
     * @param title The title of the embeded message
     * @param description The description on the embed
     * @param message The message that caused us to create this embed. Used to pull other values.
     *
     */
    fun simpleTitledEmbed(user: Member, title: String, description: String, message: Message): MessageEmbed {
        return simpleTitledEmbed(user, title, description, message.guild)
    }

    /**
     *
     * Creates an embed with the provided parameters. This is essentially the simpleEmbed but with a title
     * @param user The User who we want to be the author of this embed.
     * @param title The title of the embeded message
     * @param description The description on the embed
     * @param guild The Guild object of the guild we will be putting this embed in.
     *
     */

    fun simpleTitledEmbed(user: Member, title: String, description: String, guild: Guild): MessageEmbed {
        val builder = EmbedBuilder()
        val authorAvatar = user.user.avatarUrl
        val color = user.color
        val guildImage = guild.iconUrl
        val guildName = guild.name
        builder.setAuthor(user.effectiveName, authorAvatar, authorAvatar)
        builder.setDescription(description)
        builder.setTitle(title)
        builder.setTimestamp(Instant.now())
        builder.setFooter(guildName, guildImage)
        builder.setColor(color)
        return builder.build()
    }

    /**
     *
     * Creates a simple Embeded message to send. We don't want to go overkill on this but we want to make it look not
     * horrid by personalizing the message to the guild and user who this embed is a reply to.
     *
     * @param user The User who we want to be the author of this embed.
     * @param description The description on the embed
     * @param message The message that caused us to create this embed. Used to pull other values.
     *
     */

    fun simpleEmbed(user: Member, description: String, message: Message): MessageEmbed {
        return simpleEmbed(user, description, message.guild)
    }

    fun simpleEmbed(user: User, description: String): MessageEmbed{
        val builder = EmbedBuilder()
        val authorAvatar = user.avatarUrl
        val guildImage = authorAvatar
        val guildName = user.name
        builder.setAuthor(user.name, authorAvatar, authorAvatar)
        builder.setDescription(description)
        builder.setTimestamp(Instant.now())
        builder.setFooter(guildName, guildImage)
        return builder.build()
    }

    /**
     *
     * Creates a simple Embeded message to send. We don't want to go overkill on this but we want to make it look not
     * horrid by personalizing the message to the guild and user who this embed is a reply to.
     *
     * @param user The User who we want to be the author of this embed.
     * @param description The description on the embed
     * @param guild The guild where we will be sending this embed after creation. Could be another guild that the bot
     * is in if you wish to cross post for whatever reason.
     *
     */

    fun simpleEmbed(user: Member, description: String, guild: Guild): MessageEmbed {
        val builder = EmbedBuilder()
        val authorAvatar = user.user.avatarUrl
        val color = user.color
        val guildImage = guild.iconUrl
        val guildName = guild.name
        builder.setAuthor(user.effectiveName, authorAvatar, authorAvatar)
        builder.setDescription(description)
        builder.setTimestamp(Instant.now())
        builder.setFooter(guildName, guildImage)
        builder.setColor(color)
        return builder.build()
    }

    fun loggingEmbed(member: Member, description: String, title: String, channel: Channel, level: MessageLevel.Level) : MessageEmbed{
        val builder = EmbedBuilder()
        val authorAvatar = member.effectiveAvatarUrl
        builder.setTimestamp(Instant.now())
        builder.setColor(level.color)
        builder.setAuthor(member.effectiveName, authorAvatar, authorAvatar)


        return builder.build()
    }

    fun checkLeveledRoles(member: Member): Boolean {
        val guild = member.guild
        val leveling = ConfigMySQL.getLevelingPointsOrDefault(member.idLong, guild.idLong)
        val level = Utils.calculateLevel(leveling.levelingPoints)
        if (level >= 10) {
            guild.addRoleToMember(member, guild.getRolesByName("User", false)[0]).queue()
            if(level >= 25) {
                guild.addRoleToMember(member, guild.getRolesByName("Trusted User", false)[0]).queue()
            }
        }
        //Only send a message if they leveled up to a point where additional permissions are granted.
        if(level == 10 || level == 25)
        {
            return true
        }
        return false
    }

    fun Message.getMentionedUser(): Member?{
        if(mentions.members.isNotEmpty()){
            return mentions.members[0]
        }
        var splitStr = contentRaw.split(' ')
        var word = splitStr[1]
        var partialMatch: Member? = null
        val potentialMembers = guild.getMembersByEffectiveName(word, true)
        val potentialMembersFullName = guild.getMembersByName(word, true)
        if(potentialMembersFullName.count() > 0){
            return potentialMembersFullName[0]
        }
        if(potentialMembers.count() > 0){
            return potentialMembers[0]
        }
        for(vc in guild.voiceChannels){
            for(member in vc.members){
                if(member.effectiveName.contains(word)){
                    return member;
                }
                if(member.user.name.contains(word)){
                    return member;
                }
            }
        }
        return partialMatch
    }

    fun JDA.addRolesToUser(userId: Long, serverId: Long, roles: List<Role>){
        var guild = getGuildById(serverId)!!
        var user = guild.getMemberById(userId)
        for(role in roles){
            if(user != null){
                guild.addRoleToMember(user, role).queue()
            }
        }
    }

    fun JDA.removeRolesInServer(userId: Long, serverId: Long, roles: List<Role>){
        var guild = getGuildById(serverId)!!
        var user = guild.getMemberById(userId)
        for(role in roles){
            if(user != null){
                guild.removeRoleFromMember(user, role).queue()
            }
        }
    }

    fun JDA.getOrCreateRole(serverId: Long, role: Role): Role
    {
        val guild = getGuildById(serverId)!!
        val potentialRoles = guild.getRolesByName(role.name, false)
        if (potentialRoles.size > 0)
        {
            return potentialRoles[0]
        }
        else{
            val roleAction = guild.createRole()
            roleAction.setName(role.name)
            roleAction.setColor(role.color)
            roleAction.setPermissions(role.permissions)
            roleAction.setHoisted(role.isHoisted)
            roleAction.setMentionable(role.isMentionable)
            return roleAction.complete()
        }
    }

    fun JDA.getRoleFromServer(serverId: Long, roleName: String): Role?
    {
        val guild = getGuildById(serverId)!!
        val roles = guild.getRolesByName(roleName, false)
        if(roles.size > 0)
        {
            return roles[0]
        }
        return null
    }
}