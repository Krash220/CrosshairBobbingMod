package ik.ffm1.gradle.utils;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PropertiesOrdered extends Properties {

    private static final long serialVersionUID = 6641193094627810308L;

    private LinkedHashSet<Object> keys = new LinkedHashSet<>();

    @Override
    public synchronized Object put(Object key, Object value) {
        this.keys.add(key);

        return super.put(key, value);
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        Iterator<Object> it = this.keys.iterator();

        return new Enumeration<Object>() {

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public Object nextElement() {
                return it.next();
            }
        };
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        LinkedHashMap<Object, Object> map = new LinkedHashMap<>();

        for (Object key : this.keys) {
            map.put(key, this.get(key));
        }

        return Collections.synchronizedSet(map.entrySet());
    }
}
