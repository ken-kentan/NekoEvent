package jp.kentan.minecraft.nekoevent.component.model

import java.util.*

data class Bonus(
        val id: String,
        val start: Date,
        val end: Date,
        val command: String,
        val message: String?
) {

    fun inRange(now: Date) = start.before(now) && end.after(now)

}