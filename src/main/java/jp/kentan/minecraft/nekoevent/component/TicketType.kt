package jp.kentan.minecraft.nekoevent.component

import jp.kentan.minecraft.nekoevent.util.formatColorCode

enum class TicketType(
        val displayName: String
) {
    EVENT("&6&lイベントチケット&a(猫)&r".formatColorCode()),
    VOTE("&a&l投票限定チケット&6(猫)&r".formatColorCode())
}