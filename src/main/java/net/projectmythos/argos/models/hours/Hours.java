package net.projectmythos.argos.models.hours;


import dev.morphia.annotations.Converters;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.converters.LocalDateConverter;
import dev.morphia.converters.UUIDConverter;
import lombok.*;
import net.projectmythos.argos.utils.TimeUtils.TickTime;
import net.projectmythos.argos.framework.interfaces.PlayerOwnedObject;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Entity(value = "hours", noClassnameStored = true)
@NoArgsConstructor
@RequiredArgsConstructor
@Converters({UUIDConverter.class, LocalDateConverter.class})
public class Hours implements PlayerOwnedObject {
    @Id
    @NonNull
    protected UUID uuid;
    protected Map<LocalDate, Integer> times = new ConcurrentHashMap<>();

    public void increment() {
        increment(1);
    }

    public void increment(int amount) {
        times.put(LocalDate.now(), times.getOrDefault(LocalDate.now(), 0) + amount);
    }

    public boolean has(TickTime time) {
        return has(time.get());
    }

    public boolean has(long ticks) {
        return getTotal() >= ticks / TickTime.SECOND.get();
    }

    /**
     * Gets the player's total playtime on the server
     *
     * @return time as seconds
     */
    @ToString.Include
    public int getTotal() {
        return times.values().stream().reduce(0, Integer::sum);
    }

    /**
     * Gets the player's total playtime on the server as of the specified date
     * @param date maximum date to count (exclusive), time is ignored
     * @return time as seconds
     */
    public int getTotalAt(@NotNull LocalDateTime date) {
        return getTotalAt(date.toLocalDate());
    }

    /**
     * Gets the player's total playtime on the server as of the specified date
     * @param date maximum date to count (exclusive)
     * @return time as seconds
     */
    public int getTotalAt(@NotNull LocalDate date) {
        AtomicInteger total = new AtomicInteger(0);
        times.forEach((date1, hours) -> {
            if (date1.isBefore(date))
                total.getAndAdd(hours);
        });

        return total.get();
    }

    /**
     * Gets the player's playtime on the server during the current year
     * @return time as seconds
     */
    @ToString.Include
    public int getYearly() {
        return getYearly(Year.now());
    }

    /**
     * Gets the player's playtime on the server during the specified year
     * @param year year to count
     * @return time as seconds
     */
    public int getYearly(@NotNull Year year) {
        return times.entrySet().stream()
                .filter(entry -> entry.getKey().getYear() == year.getValue())
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    /**
     * Gets the player's playtime on the server during the current month
     * @return time as seconds
     */
    @ToString.Include
    public int getMonthly() {
        return getMonthly(YearMonth.now());
    }

    /**
     * Gets the player's playtime on the server during the specified month
     * @param yearMonth the yearMonth to count
     * @return time as seconds
     */
    public int getMonthly(@NotNull YearMonth yearMonth) {
        return times.entrySet().stream()
                .filter(entry -> yearMonth.equals(YearMonth.of(entry.getKey().getYear(), entry.getKey().getMonth())))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

//	public int getWeekly(Year year, Month month) {
//		return times.entrySet().stream()
//				.filter(entry -> entry.getKey().getYear() == year.getValue() && entry.getKey().getMonth() == month)
//				.mapToInt(Entry::getValue)
//				.sum();
//	}

    /**
     * Gets the player's playtime on the server during the current day
     * @return time as seconds
     */
    @ToString.Include
    public int getDaily() {
        return getDaily(LocalDate.now());
    }

    /**
     * Gets the player's playtime on the server during the specified day
     * @param date date to count
     * @return time as seconds
     */
    public int getDaily(@NotNull LocalDate date) {
        return times.getOrDefault(date, 0);
    }

}
