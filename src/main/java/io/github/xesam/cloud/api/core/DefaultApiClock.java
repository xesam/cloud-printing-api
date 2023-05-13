package io.github.xesam.cloud.api.core;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class DefaultApiClock implements ApiClock {
    @Override
    public long getEpochSecond() {
        return LocalDateTime.now().atZone(ZoneOffset.systemDefault()).toEpochSecond();
    }
}
