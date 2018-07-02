package jp.kentan.minecraft.nekoevent.util

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.logging.Level
import java.util.logging.Logger

class Log {
    companion object {
        private val INFO  = ChatColor.translateAlternateColorCodes('&', "&7[Event &fINFO&7]&r ")
        private val WARN  = ChatColor.translateAlternateColorCodes('&', "&7[Event &eWARN&7]&r ")
        private val ERROR = ChatColor.translateAlternateColorCodes('&', "&7[Event &4ERROR&7]&r ")

        private val SERVER = Bukkit.getServer()

        lateinit var logger: Logger

        fun info(msg: String) {
            logger.log(Level.INFO, msg)
            sendOp(INFO + msg)
        }

        fun warn(msg: String) {
            logger.log(Level.WARNING, msg)
            sendOp(WARN + msg)
        }

        fun error(msg: String) {
            logger.log(Level.SEVERE, msg)
            sendOp(ERROR + msg)
        }

        fun error(exception: Exception) {
            exception.message?.let { error(it) }
        }

        private fun sendOp(msg: String) {
            SERVER.onlinePlayers.filter { it.isOp }.forEach { it.sendMessage(msg) }
        }
    }
}