package io.extremum.datetime;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

public class PreciseDateTimes {
    public static ZonedDateTime preciseZonedDateTime(ZonedDateTime inpreciseDateTime, Long epochMicros) {
        if (inpreciseDateTime == null) {
            return null;
        }

        if (epochMicros == null) {
            return inpreciseDateTime;
        }

        return ZonedDateTimes.fromEpochMicros(epochMicros, inpreciseDateTime.getZone());
    }

    public static void storePreciseZonedDateTime(ZonedDateTime preciseDateTime, Consumer<ZonedDateTime> storeInprecisely,
                                                 Consumer<Long> storeMicroseconds) {
        if (preciseDateTime == null) {
            storeInprecisely.accept(null);
            storeMicroseconds.accept(null);
        } else {
            storeInprecisely.accept(preciseDateTime);
            storeMicroseconds.accept(ZonedDateTimes.toEpochMicros(preciseDateTime));
        }
    }

    private PreciseDateTimes() {}
}
