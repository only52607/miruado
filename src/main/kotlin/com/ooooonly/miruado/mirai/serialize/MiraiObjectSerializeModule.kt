package com.ooooonly.miruado.mirai.serialize

import com.fasterxml.jackson.databind.module.SimpleModule
import com.ooooonly.miruado.mirai.serialize.serializer.BotSerializer
import com.ooooonly.miruado.mirai.serialize.serializer.FriendSerializer
import com.ooooonly.miruado.mirai.serialize.serializer.GroupSerializer
import com.ooooonly.miruado.mirai.serialize.serializer.MemberSerializer
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member

class MiraiObjectSerializeModule: SimpleModule() {
    init {
        addSerializer(Bot::class.java, BotSerializer())
        addSerializer(Member::class.java,MemberSerializer())
        addSerializer(Friend::class.java,FriendSerializer())
        addSerializer(Group::class.java,GroupSerializer())
    }
}