package io.dedyn.engineermantra.botchelper.bot

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class SpectatorListenerAdapter: ListenerAdapter() {
    /**
     * Detect when a user joins or leaves a VC so that we can give points on leave yet not allow AFKing for points.
     */
    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        //Moved between VCs
        if(event.channelJoined != null && event.channelLeft != null)
        {
            println("${event.member.effectiveName} left ${event.channelLeft!!.name} and joined ${event.channelJoined!!.name}")
            if(BotMain.spectatorMap.containsKey(event.member.idLong) && !BotMain.spectatorMap[event.member.idLong].isNullOrEmpty()){
               for(follower in BotMain.spectatorMap[event.member.idLong]!!){
                   val followerMember = event.guild.getMemberById(follower)
                   if (followerMember != null && follower != event.member.idLong) {
                       event.guild.moveVoiceMember(followerMember, event.channelJoined).queue()
                   }
               }
            }
            //Ignore this case for now, we just need to catch it at the start to not mistake this as a DC.
            return
        }
        //User Disconnected
        else if(event.channelJoined == null && event.channelLeft != null)
        {
            //If the user is a spectator, remove them from the map.
            if(BotMain.spectatorMap.values.any({it.contains(event.member.idLong)})){
                BotMain.spectatorMap.values.forEach { it.remove(event.member.idLong) }
            }
            return
        }
        //User Joined
        else
        {
            return
        }
    }

}