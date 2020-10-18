package org.tbk.bitcoin.exchange.example.api.rate;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;

final class SlidingWindows {
    private SlidingWindows() {
        throw new UnsupportedOperationException();
    }
    
    static List<LocalDate> ofTodayWithSlidingWindow() {
        return withSlidingWindow(LocalDate.now(), 4);
    }

    static List<LocalDate> withSlidingWindow(LocalDate localDate, int days) {
        checkArgument(days >= 0, "days must be greater than zero");
        List<LocalDate> localDates = IntStream.range(0, days)
                .boxed()
                .map(localDate::minusDays)
                .sorted(Comparator.<LocalDate>naturalOrder().reversed())
                .collect(Collectors.toList());

        return localDates;
    }
}
