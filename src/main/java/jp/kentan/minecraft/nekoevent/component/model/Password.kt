package jp.kentan.minecraft.nekoevent.component.model

import jp.kentan.minecraft.nekoevent.util.formatColorCode
import org.bukkit.Location
import org.bukkit.Material

data class Password(
        val id: String,
        val password: String,
        val blockMaterial: Material? = null,
        val blockLocation: Location? = null,
        val matchMessage: String? = "&b一致",
        val notMatchMessage: String? = "&c不一致",
        val inputMessage: String? = "現在の入力> {buff}",
        val resetMessage: String? = "入力をリセットしました."
) {

    val block: Pair<Material, Location>? = if (blockMaterial != null && blockLocation != null) Pair(blockMaterial, blockLocation) else null

    val formatMatchMessage = matchMessage?.formatColorCode()
    val formatNotMatchMessage = notMatchMessage?.formatColorCode()
    val formatInputMessage = inputMessage?.formatColorCode()
    val formatResetMessage = resetMessage?.formatColorCode()

    private val buffer = StringBuilder()

    fun reset() {
        buffer.setLength(0)
    }
}