package net.fornwall.jelf;

public class ElfNoteSection extends ElfSection {

    /**
     * A possible value of the {@link #n_type} where the description should contain {@link GnuAbiDescriptor}.
     */
    public static final int NT_GNU_ABI_TAG = 1;
    /**
     * A possible value of the {@link #n_type} for a note containing synthetic hwcap information.
     * <p>
     * The descriptor begins with two words:
     * word 0: number of entries
     * word 1: bitmask of enabled entries
     * Then follow variable-length entries, one byte followed by a '\0'-terminated hwcap name string.  The byte gives the bit
     * number to test if enabled, <code>(1U &lt;&lt; bit) &amp; bitmask</code>.
     */
    public static final int NT_GNU_HWCAP = 2;
    /**
     * A possible value of the {@link #n_type} for a note containing build ID bits as generated by "ld --build-id".
     * <p>
     * The descriptor consists of any nonzero number of bytes.
     */
    public static final int NT_GNU_BUILD_ID = 3;

    /**
     * A possible value of the {@link #n_type} for a note containing a version string generated by GNU gold.
     */
    public static final int NT_GNU_GOLD_VERSION = 4;

    /**
     * The descriptor content of a link {@link #NT_GNU_ABI_TAG} type note.
     * <p>
     * Accessible in {@link #descriptorAsGnuAbi()}.
     */
    public final static class GnuAbiDescriptor {

        /**
         * A possible value of {@link #operatingSystem}.
         */
        public static final int ELF_NOTE_OS_LINUX = 0;
        /**
         * A possible value of {@link #operatingSystem}.
         */
        public static final int ELF_NOTE_OS_GNU = 1;
        /**
         * A possible value of {@link #operatingSystem}.
         */
        public static final int ELF_NOTE_OS_SOLARIS2 = 2;
        /**
         * A possible value of {@link #operatingSystem}.
         */
        public static final int ELF_NOTE_OS_FREEBSD = 3;

        /**
         * One of the ELF_NOTE_OS_* constants in this class.
         */
        public final int operatingSystem;
        /**
         * Major version of the required ABI.
         */
        public final int majorVersion;
        /**
         * Minor version of the required ABI.
         */
        public final int minorVersion;
        /**
         * Subminor version of the required ABI.
         */
        public final int subminorVersion;

        public GnuAbiDescriptor(int operatingSystem, int majorVersion, int minorVersion, int subminorVersion) {
            this.operatingSystem = operatingSystem;
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.subminorVersion = subminorVersion;
        }
    }

    public final /* uint32_t */ int n_namesz;
    public final /* uint32_t */ int n_descsz;
    public final /* uint32_t */ int n_type;
    private final String n_name;
    private final byte[] descriptorBytes;
    private final GnuAbiDescriptor gnuAbiDescriptor;

    ElfNoteSection(ElfParser parser, ElfSectionHeader header) throws ElfException {
        super(parser, header);

        parser.seek(header.sh_offset);
        n_namesz = parser.readInt();
        n_descsz = parser.readInt();
        n_type = parser.readInt();
        byte[] nameBytes = new byte[n_namesz];
        descriptorBytes = new byte[n_descsz];
        int bytesRead = parser.read(nameBytes);
        if (bytesRead != n_namesz) {
            throw new ElfException("Error reading note name (read=" + bytesRead + ", expected=" + n_namesz + ")");
        }
        parser.skip(bytesRead % 4);

        switch (n_type) {
            case NT_GNU_ABI_TAG:
                gnuAbiDescriptor = new GnuAbiDescriptor(parser.readInt(), parser.readInt(), parser.readInt(), parser.readInt());
                break;
            default:
                gnuAbiDescriptor = null;
        }

        bytesRead = parser.read(descriptorBytes);
        if (bytesRead != n_descsz) {
            throw new ElfException("Error reading note name (read=" + bytesRead + ", expected=" + n_descsz + ")");
        }

        n_name = new String(nameBytes, 0, n_namesz - 1); // unnecessary trailing 0
    }

    public String getName() {
        return n_name;
    }

    public byte[] descriptorBytes() {
        return descriptorBytes;
    }

    public String descriptorAsString() {
        return new String(descriptorBytes);
    }

    public GnuAbiDescriptor descriptorAsGnuAbi() {
        return gnuAbiDescriptor;
    }

}
