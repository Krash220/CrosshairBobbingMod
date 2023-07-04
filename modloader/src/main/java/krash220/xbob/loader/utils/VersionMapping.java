package krash220.xbob.loader.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

public class VersionMapping {

    private static final Map<Integer, String> mapping = ImmutableMap.<Integer, String>builder()
            .put(11602, "1.16.2")
            .put(11700, "1.17")
//            .put(11800, "1.18")
            .put(11900, "1.19")
            .put(11903, "1.19.3")
            .put(11200, "1.20")
            .build();

    private static final Pattern RELEASE_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?");
    private static final Pattern SNAPSHOT_PATTERN = Pattern.compile("(?:Snapshot )?(\\d+)w0?(0|[1-9]\\d*)([a-z])");

    public static int getNum(String version) {
        Matcher matcher = SNAPSHOT_PATTERN.matcher(version);

        if (matcher.matches()) {
            int year = Integer.parseInt(matcher.group(1));
            int week = Integer.parseInt(matcher.group(2));

            if (year >= 23 && week >= 12)
                version = "1.20";
            if (year >= 23 && week >= 3)
                version = "1.19.4";
            if (year == 22 && week >= 42)
                version = "1.19.3";
            if (year == 22 && week == 24)
                version = "1.19.1";
            if (year == 22 && week >= 11 && week <= 19)
                version = "1.19";
            if (year == 22 && week >= 3 && week <= 7)
                version = "1.18.2";
            if (year == 21 && week >= 37 && week <= 44)
                version = "1.18";
            if ((year == 20 && week >= 45) || (year == 21 && week <= 20))
                version = "1.17";
            if (year == 20 && week >= 27 && week <= 30)
                version = "1.16.2";
            if (year == 20 && week >= 6 && week <= 22)
                version = "1.16";
            if (year == 19 && week >= 34)
                version = "1.15";
            if ((year == 18 && week >= 43) || (year == 19 && week <= 14))
                version = "1.14";
            if (year == 18 && week >= 30 && week <= 33)
                version = "1.13.1";
            if ((year == 17 && week >= 43) || (year == 18 && week <= 22))
                version = "1.13";
            if (year == 17 && week == 31)
                version = "1.12.1";
            if (year == 17 && week >= 6 && week <= 18)
                version = "1.12";
            if (year == 16 && week == 50)
                version = "1.11.1";
            if (year == 16 && week >= 32 && week <= 44)
                version = "1.11";
            if (year == 16 && week >= 20 && week <= 21)
                version = "1.10";
            if (year == 16 && week >= 14 && week <= 15)
                version = "1.9.3";
            if ((year == 15 && week >= 31) || (year == 16 && week <= 7))
                version = "1.9";
            if (year == 14 && week >= 2 && week <= 34)
                version = "1.8";
            if (year == 13 && week >= 47 && week <= 49)
                version = "1.7.4";
            if (year == 13 && week >= 36 && week <= 43)
                version = "1.7.2";
            if (year == 13 && week >= 16 && week <= 26)
                version = "1.6";
            if (year == 13 && week >= 11 && week <= 12)
                version = "1.5.1";
            if (year == 13 && week >= 1 && week <= 10)
                version = "1.5";
            if (year == 12 && week >= 49 && week <= 50)
                version = "1.4.6";
            if (year == 12 && week >= 32 && week <= 42)
                version = "1.4.2";
            if (year == 12 && week >= 15 && week <= 30)
                version = "1.3.1";
            if (year == 12 && week >= 3 && week <= 8)
                version = "1.2.1";
            if ((year == 11 && week >= 47) || (year == 12 && week <= 1))
                version = "1.1";
        }

        matcher = RELEASE_PATTERN.matcher(version);

        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1)) * 10000 + Integer.parseInt(matcher.group(2)) * 100 + (matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0);
        } else {
            return 0;
        }
    }

    public static String get(String version) {
        final int v = getNum(version);

        Optional<Entry<Integer, String>> result = mapping.entrySet().stream().filter(e -> {
            return e.getKey() <= v;
        }).max((a, b) -> {
            return a.getKey() - b.getKey();
        });

        if (result.isPresent()) {
            return result.get().getValue();
        } else {
            return version;
        }
    }
}
