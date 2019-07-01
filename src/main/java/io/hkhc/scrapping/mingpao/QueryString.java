package io.hkhc.scrapping.mingpao;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by hermanc on 19/10/2016.
 */
public class QueryString {

    private Map<String, String> entries;
    private String[] ignoredKeys;
    private String[] includedKeys;
    private String str;

    public static String[] gaKeys = {
            "utm_source",
            "utm_medium",
            "utm_term",
            "utm_content",
            "utm_campaign"
    };

    public QueryString() {
        this.str = "";
    }
    public QueryString(String str) {
        this.str = str;
    }

    @NotNull
    public QueryString ignoreKeys(String ...s) {
        ignoredKeys = s;
        return this;
    }

    public QueryString includedKeys(String... s) {
        includedKeys = s;
        return this;
    }

    @Nullable
    public String decode(@Nullable String s) {
        try {
            if (s==null) {
                return null;
            }
            else {
                return URLDecoder.decode(s, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            // it shall never happen as we use UTF-8
            return null;
        }
    }

    @Nullable
    public String encode(@Nullable String s) {
        try {
            if (s==null) {
                return null;
            }
            else {
                return URLEncoder.encode(s, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            // it shall never happen as we use UTF-8
            return null;
        }
    }

    @NotNull
    public QueryString parse() {
        if (str==null) {
            return this;
        }
        entries = new LinkedHashMap<>(); // need predictable order
        StringTokenizer tokenizer = new StringTokenizer(str, "&");
        while (tokenizer.hasMoreElements()) {
            String nextEntry = tokenizer.nextToken();
            int equalDelim = nextEntry.indexOf("=");
            String key, value;
            if (equalDelim == -1) {
                key = nextEntry;
                value = null;
            } else {
                key = nextEntry.substring(0, equalDelim);
                value = nextEntry.substring(equalDelim + 1);
            }
            key = decode(key);
            value = decode(value);
            entries.put(key, value);
        }
        return this;
    }

    @NotNull
    public QueryString filter() {
        if (entries==null) {
            return this;
        }
        Iterator<String> it = entries.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (shallIgnoreKey(key) || !shallIncludeKey(key)) {
                it.remove();
            }
        }
        return this;
    }

    @NotNull
    private Map<String,String> getEntriesForChange() {
        if (entries==null) {
            entries = new HashMap<String,String>();
        }
        return entries;
    }

    @NotNull
    public Map<String, String> getEntries() {
        Map<String,String> e = getEntriesForChange();
        return Collections.unmodifiableMap(e);
    }

    public void add(String name, String value) {
        Map<String,String> e = getEntriesForChange();
        e.put(name, value);
    }

    public void addOptional(String name, String value) {
        if (value!=null) {
            Map<String,String> e = getEntriesForChange();
            e.put(name, value);
        }
    }

    @NotNull
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (entries!=null) {
            boolean first = true;
            for (Map.Entry<String, String> e : entries.entrySet()) {
                if (!first) {
                    builder.append('&');
                }
                builder.append(encode(e.getKey()));
                if (e.getValue() != null) {
                    builder.append('=');
                    builder.append(encode(e.getValue()));
                }
                first = false;
            }
        }
        return builder.toString();
    }

    private boolean shallIgnoreKey(@NotNull String key) {
        if (ignoredKeys==null) {
            return false;
        }
        for (String s : ignoredKeys)
            if (s.equals(key)) {
                return true;
            }
        return false;
    }

    private boolean shallIncludeKey(@NotNull String key) {
        if (includedKeys==null || includedKeys.length == 0) return true; // if no specific set, all key included
        for (String s : includedKeys)
            if (s.equals(key)) return true;
        return false;
    }

}
