package jp.kentan.minecraft.nekoevent.manager

import jp.kentan.minecraft.nekoevent.NekoEvent
import jp.kentan.minecraft.nekoevent.config.provider.SignConfigProvider
import jp.kentan.minecraft.nekoevent.listener.SignListener
import jp.kentan.minecraft.nekoevent.util.*
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent

class ReviveManager(
        private val signConfig: SignConfigProvider
) : SignListener {

    companion object {
        private val SIGN_INDEX = "&8&l[&a&l復活&8&l]".formatColorCode()
        val SIGN_KEY = Pair("[revive]", SIGN_INDEX)

        private const val LOCATION_X_METADATA_KEY = "reviveX"
        private const val LOCATION_Y_METADATA_KEY = "reviveY"
        private const val LOCATION_Z_METADATA_KEY = "reviveZ"
        private const val EXP_METADATA_KEY = "reviveExp"
    }

    private fun revive(player: Player, location: Location, exp: Int) {
        val totalExp = player.getExperience()

        if (totalExp < exp) {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "経験値が ${exp - totalExp}Exp 足りません.")
            return
        }

        player.takeExperience(exp)

        player.teleport(location)
        player.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 1.0f)

        player.world.spawnParticle(Particle.VILLAGER_HAPPY, location, 100, 0.6, 0.6, 0.6)
    }

    /**
     * 看板フォーマット
     * 0: [revive]
     * 1: [x y z]
     * 2: [exp]
     * 3:
     */
    override fun onSignChanged(event: SignChangeEvent) {
        val player = event.player

        val location = event.getLine(1).orEmpty().split(" ").let {
            if (it.size < 3) {
                Log.error("座標パラメータが不足しています.")
                return
            }

            return@let Location(
                    player.world,
                    it[0].toDoubleOrError() ?: return,
                    it[1].toDoubleOrError() ?: return,
                    it[2].toDoubleOrError() ?: return
            )
        }

        val exp = event.getLine(2).orEmpty().toIntOrNull() ?: let {
            Log.error("経験値コストが不足しています..")
            return
        }
        if (exp < 1) {
            Log.error("経験値コストは 1以上 に指定してください.")
            return
        }


        val signMetadataMap = LinkedHashMap<String, Any>().apply {
            put(LOCATION_X_METADATA_KEY, location.x)
            put(LOCATION_Y_METADATA_KEY, location.y)
            put(LOCATION_Z_METADATA_KEY, location.z)
            put(EXP_METADATA_KEY, exp)
        }

        val sign = event.block.state as Sign

        if (!signConfig.save(sign.location, signMetadataMap)) {
            Log.error("看板ﾒﾀﾃﾞｰﾀの保存に失敗しました.")
            return
        }

        event.setLine(0, SIGN_INDEX)
        event.setLine(1, "")
        event.setLine(2, "⋘ §l${exp}Exp§r ⋙")
        event.setLine(3, "")
    }

    override fun onPlayerInteract(event: PlayerInteractEvent, sign: Sign) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }
        val player = event.player

        val location = player.location
        location.x = signConfig.getMetadata(sign.location, LOCATION_X_METADATA_KEY) as Double? ?: return
        location.y = signConfig.getMetadata(sign.location, LOCATION_Y_METADATA_KEY) as Double? ?: return
        location.z = signConfig.getMetadata(sign.location, LOCATION_Z_METADATA_KEY) as Double? ?: return

        val exp = signConfig.getMetadata(sign.location, EXP_METADATA_KEY) as Int? ?: return

        revive(event.player, location, exp)
    }
}