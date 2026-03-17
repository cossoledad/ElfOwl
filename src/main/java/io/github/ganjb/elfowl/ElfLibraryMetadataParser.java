package io.github.ganjb.elfowl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ElfLibraryMetadataParser implements LibraryMetadataParser {

    private static final byte[] ELF_MAGIC = {0x7f, 0x45, 0x4c, 0x46};
    private static final long DT_NULL = 0L;
    private static final long DT_NEEDED = 1L;
    private static final long DT_SONAME = 14L;

    @Override
    public LibraryMetadata parse(Path path) {
        String basename = path.getFileName().toString();
        if (!Files.isReadable(path)) {
            return new LibraryMetadata(path, basename, null, Collections.<String>emptyList());
        }

        String soname = null;
        List<String> needed = new ArrayList<String>();

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer ident = ByteBuffer.allocate(16);
            readFully(channel, ident, 0L);
            ident.flip();

            for (int i = 0; i < ELF_MAGIC.length; i++) {
                if (ident.get(i) != ELF_MAGIC[i]) {
                    return new LibraryMetadata(path, basename, null, Collections.<String>emptyList());
                }
            }

            boolean is64Bit = ident.get(4) == 2;
            ByteOrder order = ident.get(5) == 2 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

            ByteBuffer header = ByteBuffer.allocate(is64Bit ? 64 : 52).order(order);
            readFully(channel, header, 0L);
            header.flip();

            long sectionHeaderOffset;
            int sectionHeaderEntrySize;
            int sectionHeaderCount;
            int sectionNameIndex;

            if (is64Bit) {
                sectionHeaderOffset = header.getLong(40);
                sectionHeaderEntrySize = header.getShort(58) & 0xffff;
                sectionHeaderCount = header.getShort(60) & 0xffff;
                sectionNameIndex = header.getShort(62) & 0xffff;
            } else {
                sectionHeaderOffset = header.getInt(32) & 0xffffffffL;
                sectionHeaderEntrySize = header.getShort(46) & 0xffff;
                sectionHeaderCount = header.getShort(48) & 0xffff;
                sectionNameIndex = header.getShort(50) & 0xffff;
            }

            if (sectionHeaderOffset <= 0 || sectionHeaderEntrySize <= 0 || sectionHeaderCount <= 0) {
                return new LibraryMetadata(path, basename, null, Collections.<String>emptyList());
            }

            ByteBuffer sectionNameHeader = readBuffer(
                channel,
                sectionHeaderOffset + (long) sectionNameIndex * sectionHeaderEntrySize,
                sectionHeaderEntrySize,
                order
            );
            long sectionNameTableOffset = is64Bit
                ? sectionNameHeader.getLong(24)
                : sectionNameHeader.getInt(16) & 0xffffffffL;
            long sectionNameTableSize = is64Bit
                ? sectionNameHeader.getLong(32)
                : sectionNameHeader.getInt(20) & 0xffffffffL;
            ByteBuffer sectionNameTable = readBuffer(channel, sectionNameTableOffset, safeSize(sectionNameTableSize), order);

            long dynamicOffset = -1L;
            long dynamicSize = 0L;
            long dynamicStringOffset = -1L;
            long dynamicStringSize = 0L;

            for (int i = 0; i < sectionHeaderCount; i++) {
                ByteBuffer section = readBuffer(
                    channel,
                    sectionHeaderOffset + (long) i * sectionHeaderEntrySize,
                    sectionHeaderEntrySize,
                    order
                );
                String name = getString(sectionNameTable, section.getInt(0));
                if (".dynamic".equals(name)) {
                    dynamicOffset = is64Bit ? section.getLong(24) : section.getInt(16) & 0xffffffffL;
                    dynamicSize = is64Bit ? section.getLong(32) : section.getInt(20) & 0xffffffffL;
                } else if (".dynstr".equals(name)) {
                    dynamicStringOffset = is64Bit ? section.getLong(24) : section.getInt(16) & 0xffffffffL;
                    dynamicStringSize = is64Bit ? section.getLong(32) : section.getInt(20) & 0xffffffffL;
                }
            }

            if (dynamicOffset < 0 || dynamicStringOffset < 0) {
                return new LibraryMetadata(path, basename, null, Collections.<String>emptyList());
            }

            ByteBuffer dynstr = readBuffer(channel, dynamicStringOffset, safeSize(dynamicStringSize), order);
            int dynamicEntrySize = is64Bit ? 16 : 8;
            long entryCount = dynamicSize / dynamicEntrySize;

            for (int i = 0; i < entryCount; i++) {
                ByteBuffer entry = readBuffer(
                    channel,
                    dynamicOffset + (long) i * dynamicEntrySize,
                    dynamicEntrySize,
                    order
                );
                long tag = is64Bit ? entry.getLong(0) : entry.getInt(0) & 0xffffffffL;
                long value = is64Bit ? entry.getLong(8) : entry.getInt(4) & 0xffffffffL;

                if (tag == DT_NULL) {
                    break;
                }
                if (tag == DT_NEEDED) {
                    needed.add(getString(dynstr, (int) value));
                } else if (tag == DT_SONAME) {
                    soname = getString(dynstr, (int) value);
                }
            }
        } catch (IOException e) {
            return new LibraryMetadata(path, basename, null, Collections.<String>emptyList());
        }

        return new LibraryMetadata(path, basename, soname, needed);
    }

    private static void readFully(FileChannel channel, ByteBuffer buffer, long position) throws IOException {
        while (buffer.hasRemaining()) {
            int read = channel.read(buffer, position + buffer.position());
            if (read < 0) {
                throw new IOException("Unexpected end of file");
            }
        }
    }

    private static ByteBuffer readBuffer(FileChannel channel, long offset, int size, ByteOrder order) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(size).order(order);
        readFully(channel, buffer, offset);
        buffer.flip();
        return buffer;
    }

    private static int safeSize(long size) throws IOException {
        if (size < 0 || size > Integer.MAX_VALUE) {
            throw new IOException("ELF section too large: " + size);
        }
        return (int) size;
    }

    private static String getString(ByteBuffer table, int offset) {
        if (offset < 0 || offset >= table.limit()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = offset; i < table.limit(); i++) {
            byte current = table.get(i);
            if (current == 0) {
                break;
            }
            builder.append((char) current);
        }
        return builder.toString();
    }
}
