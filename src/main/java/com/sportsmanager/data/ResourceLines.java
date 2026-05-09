package com.sportsmanager.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Loads newline-delimited text resources from the classpath (lines starting with {@code #} are skipped). */
public final class ResourceLines {

    private ResourceLines() {
    }

    public static List<String> load(String absolutePath) {
        String path = absolutePath.startsWith("/") ? absolutePath : "/" + absolutePath;
        try (InputStream is = ResourceLines.class.getResourceAsStream(path)) {
            if (is == null) {
                return List.of();
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                List<String> out = new ArrayList<>();
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    out.add(line);
                }
                return Collections.unmodifiableList(out);
            }
        } catch (IOException e) {
            return List.of();
        }
    }
}
