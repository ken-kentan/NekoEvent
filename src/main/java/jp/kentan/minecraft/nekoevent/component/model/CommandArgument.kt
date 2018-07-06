package jp.kentan.minecraft.nekoevent.component.model

class CommandArgument(
        private vararg val arguments: String
) {

    companion object {
        const val PLAYER = "[player]"
    }

    fun matchFirst(arg: String) = arguments.isNotEmpty() && arguments[0].startsWith(arg, true)

    fun get(args: Array<String>) = if (args.size in 1..arguments.size) arguments[args.size-1] else null

}