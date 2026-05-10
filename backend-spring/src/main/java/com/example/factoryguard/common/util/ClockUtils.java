package com.example.factoryguard.common.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class ClockUtils {

    private ClockUtils() {
    }

    public static OffsetDateTime nowUtc() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
