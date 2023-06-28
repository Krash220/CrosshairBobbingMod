package ik.ffm1.gradle.extensions;

import java.util.HashMap;
import java.util.Properties;

public class ModExtension extends HashMap<String, String> {

    private static final long serialVersionUID = 4038415571161222803L;

    public ModExtension(Properties map) {
        for (Object k : map.keySet()) {
            String key = (String) k;
            String value = map.getProperty((String) k);
            key = key.toLowerCase();

            if (key.startsWith("mod_")) {
                key = key.substring(4);
            }

            super.put(key, value);
        }
    }
}
