package jp.kentan.minecraft.neko_event.config.listener;

import java.util.Map;

public interface ConfigListener<T> {
    void onUpdate(Map<String, T> dataMap);
}
