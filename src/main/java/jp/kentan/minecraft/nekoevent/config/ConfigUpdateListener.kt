package jp.kentan.minecraft.nekoevent.config

interface ConfigUpdateListener<T> {
    fun onConfigUpdate(dataMap: Map<String, T>)
}