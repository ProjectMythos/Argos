package net.projectmythos.argos.utils;

import lombok.Getter;
import net.projectmythos.argos.framework.exceptions.postconfigured.InvalidInputException;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
    public static Vector randomVector() {
        double x = random.nextDouble() * 2 - 1;
        double y = random.nextDouble() * 2 - 1;
        double z = random.nextDouble() * 2 - 1;

        return new Vector(x, y, z).normalize();
    }

    public static Vector randomCircleVector() {
        double rnd = random.nextDouble() * 2 * Math.PI;
        double x = Math.cos(rnd);
        double z = Math.sin(rnd);

        return new Vector(x, 0, z);
    }

    public static Material randomMaterial() {
        return randomMaterial(Material.values());
    }

    public static Material randomMaterial(Tag<Material> tag) {
        return randomMaterial(tag.getValues().toArray(Material[]::new));
    }

    public static Material randomMaterial(Material[] materials) {
        return materials[random.nextInt(materials.length)];
    }

    protected RandomUtils() {
        throw new IllegalStateException("Cannot instantiate utility class");
    }

    @Getter
    protected static final Random random = new Random();

    public static boolean chanceOf(int chance) {
        return chanceOf((double) chance);
    }

    public static boolean chanceOf(double chance) {
        if(chance <= 0.0)
            return false;

        return randomDouble(0, 100) <= chance;
    }

    public static int randomInt(int max) {
        return randomInt(0, max);
    }

    public static int randomInt(int min, int max) throws IllegalArgumentException {
        if (min == max) return min;
        if (min > max) throw new IllegalArgumentException("Min (" + min + ") cannot be greater than max (" + max + ")!");
        return min + random.nextInt(max - min + 1);
    }

    public static long randomLong(long max) {
        return randomLong(0, max);
    }

    public static long randomLong(long min, long max) throws IllegalArgumentException {
        if (min == max) return min;
        if (min > max) throw new IllegalArgumentException("Min (" + min + ") cannot be greater than max (" + max + ")!");
        return min + ThreadLocalRandom.current().nextLong(max - min + 1);
    }

    public static double randomDouble() {
        return random.nextDouble();
    }

    public static double randomDouble(double max) {
        return randomDouble(0, max);
    }

    public static double randomDouble(double min, double max) throws IllegalArgumentException {
        if (min == max) return min;
        if (min > max) throw new IllegalArgumentException("Min (" + min + ") cannot be greater than max (" + max + ")!");
        return min + (max - min) * random.nextDouble();
    }

    public static String randomAlphanumeric() {
        return randomElement(Utils.ALPHANUMERICS.split(""));
    }

    @Contract("null -> null")
    public static <T> T randomElement(T @Nullable ... list) {
        if (list == null || list.length == 0)
            return null;
        return list[random.nextInt(list.length)];
    }

    @Contract("null -> null")
    public static <T> T randomElement(@Nullable Collection<@Nullable T> list) {
        if (Nullables.isNullOrEmpty(list)) return null;
        int getIndex = random.nextInt(list.size());
        int currentIndex = 0;
        for (T item : list) {
            if (currentIndex++ == getIndex)
                return item;
        }
        throw new IllegalStateException("Collection was altered during iteration");
    }

    public static <T> T randomElement(@NotNull Class<? extends T> enumClass) {
        return randomElement(enumClass.getEnumConstants());
    }

    @Contract("null -> null")
    private static <T> T randomElement(@Nullable List<@Nullable T> list) {
        if (Nullables.isNullOrEmpty(list)) return null;
        return list.get(random.nextInt(list.size()));
    }

    public static double randomAngle() {
        return random.nextDouble() * 2 * Math.PI;
    }

    public static <E> E getWeightedRandom(@NotNull Map<E, Double> weights) {
        return Utils.getMin(weights.keySet(), element -> -Math.log(RandomUtils.getRandom().nextDouble()) / weights.get(element)).getObject();
    }

}
