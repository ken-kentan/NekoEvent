package jp.kentan.minecraft.nekoevent.component.model

import jp.kentan.minecraft.nekoevent.util.Log
import jp.kentan.minecraft.nekoevent.util.MersenneTwisterFast
import jp.kentan.minecraft.nekoevent.util.formatColorCode


data class Gacha(
        val id: String,
        val name: String,
        val componentList: List<Component>,
        val winMessage: String?,
        val loseMessage: String?,
        val broadcastMessage: String?,
        val enabledEffect: Boolean
) {

    companion object {
        private val RANDOM = MersenneTwisterFast()
    }

    val formatName = name.formatColorCode()

    init {
        if (componentList.sumByDouble { it.probability } !in 0.99..1.01) {
            throw IllegalArgumentException("ｶﾞﾁｬ($id)の合計確率が100%ではありません.")
        }
    }

    fun getByRandom(): Component {
        val random = RANDOM.nextDouble(false, true)

        var count = 0.0
        componentList.forEach {
            count += it.probability

            if (count >= random) {
                return it
            }
        }

        Log.error("ｶﾞﾁｬ($id) の確率が正常に動作しませんでした. [count:$count, rand:$random]")

        return componentList.last()
    }

    data class Component(
            val name: String,
            val probability: Double,
            val commandList: List<String>,
            val isDisabledBroadcast: Boolean,
            val isEnabledOnlyOnce: Boolean
    ) {
        companion object {
            fun normalize(list: MutableList<Component>): Boolean {
                val sum = list.sumByDouble { it.probability }
                val emptyProbabilityCount = list.filter { it.probability <= 0 }.size

                if (sum > 1) { return false }

                if (emptyProbabilityCount > 0) {
                    val probability = (1.0 - sum) / emptyProbabilityCount

                    list.replaceAll {
                        if (it.probability <= 0) {
                            it.copy(probability = probability)
                        } else {
                            it
                        }
                    }
                } else {
                    val gain = 1 / sum

                    list.replaceAll { it.copy(probability = it.probability * gain) }
                }

                return true
            }
        }

        private var isUsed = false

        fun isWin(): Boolean {
            if (commandList.isEmpty() || (isEnabledOnlyOnce && isUsed)) {
                return false
            }

            isUsed = true

            return true
        }
    }
}