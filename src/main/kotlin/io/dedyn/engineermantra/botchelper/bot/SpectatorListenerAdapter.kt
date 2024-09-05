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
            if(BotMain.spectatorMap.containsKey(event.member.idLong)){
                for(vc in event.guild.voiceChannels)
                {
                    if(vc.members.any{m -> m.idLong == BotMain.spectatorMap[event.member.idLong]})
                    {
                        event.guild.moveVoiceMember(event.member, vc)
                        return
                    }
                }
            }
            //Ignore this case for now, we just need to catch it at the start to not mistake this as a DC.
            return
        }
        //User Disconnected
        else if(event.channelJoined == null && event.channelLeft != null)
        {
            return
        }
        //User Joined
        else
        {
            return
        }
    }

}