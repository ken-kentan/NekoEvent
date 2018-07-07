package jp.kentan.minecraft.nekoevent.manager

import jp.kentan.minecraft.nekoevent.NekoEvent
import jp.kentan.minecraft.nekoevent.config.provider.SignConfigProvider
import jp.kentan.minecraft.nekoevent.listener.SignListener
import jp.kentan.minecraft.nekoevent.util.Log
import jp.kentan.minecraft.nekoevent.util.formatColorCode
import jp.kentan.minecraft.nekoevent.util.toDoubleOrError
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.ceil

class SpawnManager(
        private val signConfig: SignConfigProvider
) : SignListener {

    companion object {
        private val SIGN_INDEX = "&8&l[&3&lセーブ&8&l]".formatColorCode()
        val SIGN_KEY = Pair("[setspawn]", SIGN_INDEX)

        private const val LOCATION_X_METADATA_KEY = "setspawnX"
        private const val LOCATION_Y_METADATA_KEY = "setspawnY"
        private const val LOCATION_Z_METADATA_KEY = "setspawnZ"
    }

    fun setSpawn(player: Player, location: Location, isSendMessage: Boolean = true) {
        location.y = ceil(location.y)

        player.setBedSpawnLocation(location, true)

        if (isSendMessage) {
            player.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.1f)
            player.sendMessage(NekoEvent.PREFIX + ChatColor.AQUA + "セーブしました！")
        }
    }

    fun removeBedSpawnIfNeed(player: Player) {
        val spawn = player.bedSpawnLocation ?: return

        if (spawn.world.name != NekoEvent.WORLD_NAME) {
            return
        }

        player.setBedSpawnLocation(null, true)
        player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "セーブポイントが自動的に消去されました.")
    }

    /**
     * 看板フォーマット
     * 0: [setspawn ]
     * 1: [x y z]
     * 2: <message>
     * 3:
     */
    override fun onSignChanged(event: SignChangeEvent) {
        val player = event.player
        val location = event.getLine(1).split(" ").let {
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


        val signMetadataMap = LinkedHashMap<String, Any>().apply {
            put(LOCATION_X_METADATA_KEY, location.x)
            put(LOCATION_Y_METADATA_KEY, location.y)
            put(LOCATION_Z_METADATA_KEY, location.z)
        }

        val sign = event.block.state as Sign

        if (!signConfig.save(sign.location, signMetadataMap)) {
            Log.error("看板ﾒﾀﾃﾞｰﾀの保存に失敗しました.")
            return
        }

        event.setLine(0, SIGN_INDEX)
        event.setLine(1, "")
        event.setLine(2, event.getLine(2))
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

        setSpawn(event.player, location)
    }
}