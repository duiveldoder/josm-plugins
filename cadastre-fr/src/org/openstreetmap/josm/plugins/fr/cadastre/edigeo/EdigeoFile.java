// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.openstreetmap.josm.tools.Logging;

/**
 * Superclass of all Edigeo files.
 */
abstract class EdigeoFile {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Block descriptor.
     */
    static class Block {
        /** RTY */ final String type;
        /** RID */ String identifier;

        Block(String type) {
            this.type = Objects.requireNonNull(type, "type");
        }

        public final String getType() {
            return type;
        }

        public final String getIdentifier() {
            return identifier;
        }

        void processRecord(EdigeoRecord r) {
            if ("RID".equals(r.name)) {
                identifier = safeGetAndLog(r, tr("Identifier"));
            } else {
                throw new IllegalArgumentException(r.toString());
            }
        }
    }

    private boolean bomFound;
    private boolean eomFound;
    EdigeoCharset charset;
    private Block currentBlock;

    EdigeoFile(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    // Read record
                    EdigeoRecord r = new EdigeoRecord(line);
                    // Process begin of file
                    if (!bomFound) {
                        bomFound = "BOM".equals(r.name);
                        if (!bomFound) {
                            throw new IOException("Unexpected first record: " + r);
                        } else {
                            assert r.length == 12 && r.values.size() == 1 : r;
                            continue;
                        }
                    }
                    // Process charset
                    if (charset == null) {
                        if (!"CSE".equals(r.name)) {
                            throw new IOException("Unexpected record instead of charset: " + r);
                        } else {
                            assert r.values.size() == 1 : r;
                            charset = EdigeoCharset.of(r.values.get(0));
                            continue;
                        }
                    }
                    // Process other records & end of file
                    if (eomFound) {
                        throw new IOException("Unexpected record after end of file: " + r);
                    }
                    eomFound = "EOM".equals(r.name);
                    if (!eomFound) {
                        processRecord(r);
                    } else {
                        assert r.length == 0 && r.values.isEmpty() : r;
                    }
                }
            }
        }
    }

    protected abstract Block createBlock(String type);

    private void processRecord(EdigeoRecord r) {
        if ("RTY".equals(r.name)) {
            currentBlock = createBlock(r.values.get(0));
            return;
        }

        if (currentBlock == null) {
            throw new IllegalStateException(r.toString());
        }

        currentBlock.processRecord(r);
    }

    protected static String safeGet(EdigeoRecord r) {
        return r.length > 0 ? r.values.get(0) : null;
    }

    protected static int safeGetInt(EdigeoRecord r) {
        return r.length > 0 ? Integer.parseInt(r.values.get(0)) : 0;
    }

    protected static LocalDate safeGetDate(EdigeoRecord r) {
        return r.length > 0 ? LocalDate.parse(r.values.get(0), dateFormatter) : null;
    }

    protected static String safeGetAndLog(EdigeoRecord r, String msg) {
        if (r.length > 0) {
            String v = r.values.get(0);
            Logging.info(msg + ": " + v);
            return v;
        }
        return null;
    }

    protected static LocalDate safeGetDateAndLog(EdigeoRecord r, String msg) {
        if (r.length > 0) {
            LocalDate v = LocalDate.parse(r.values.get(0), dateFormatter);
            Logging.info(msg + ": " + v);
            return v;
        }
        return null;
    }
}