package jp.kentan.minecraft.nekoevent.component.model

import jp.kentan.minecraft.nekoevent.component.PasswordResult
import jp.kentan.minecraft.nekoevent.util.formatColorCode
import org.bukkit.Location
import org.bukkit.Material

data class Password(
        val id: String,
        val default: String = "",
        val blockMaterial: Material? = null,
        val blockLocation: Location? = null,
        val matchMessage: String? = "&b一致",
        val notMatchMessage: String? = "&c不一致",
        val inputMessage: String? = "現在の入力> {buff}",
        val resetMessage: String? = "入力をリセットしました."
) {

    var passwordText: String = default
    val block: Pair<Material, Location>? = if (blockMaterial != null && blockLocation != null) Pair(blockMaterial, blockLocation) else null

    private val _formatInputMessage = inputMessage?.formatColorCode()

    val formatMatchMessage = matchMessage?.formatColorCode()
    val formatNotMatchMessage = notMatchMessage?.formatColorCode()
    val formatInputMessage
        get() = _formatInputMessage?.replace("{buff}", buffer.toString())
    val formatResetMessage = resetMessage?.formatColorCode()

    private val buffer = StringBuilder()
    val bufferText: String
        get() = buffer.toString()

    fun input(text: String): PasswordResult {
        if (passwordText.isEmpty()) {
            return PasswordResult.NOT_MATCH
        }

        // 前回のバッファを消去
        if(buffer.length >= passwordText.length) {
            buffer.setLength(0)
        }

        buffer.append(text)

        return when {
            buffer.length < passwordText.length -> PasswordResult.CONTINUE
            passwordText == buffer.toString()   -> PasswordResult.MATCH
            else -> PasswordResult.NOT_MATCH
        }
    }

    fun reset() {
        buffer.setLength(0)
    }
}