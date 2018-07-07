package jp.kentan.minecraft.nekoevent.component

import jp.kentan.minecraft.nekoevent.util.formatColorCode

enum class TicketType(
        val path: String,
        val displayName: String
) {
    EVENT("Event", "&6&lイベントチケット&a(猫)&r".formatColorCode()),
    VOTE("Vote", "&a&l投票限定チケット&6(猫)&r".formatColorCode());

    fun getReachDayLimitMessage(limit: Int) = "$displayName&7は,&c1日${limit}枚まで&7です.".formatColorCode()
}