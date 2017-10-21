package executionlog;

public class Instructions {


    public static class Instruction {
        int length;
        boolean sequential;

        public Instruction(int length, boolean sequential) {
            this.length = length;
            this.sequential = sequential;
        }
    }

    public static Instruction[] opcodes = new Instruction[256];

    static {

        opcodes[0x00] = new Instruction(1, true);
        opcodes[0x01] = new Instruction(3, true);
        opcodes[0x02] = new Instruction(1, true);
        opcodes[0x03] = new Instruction(1, true);
        opcodes[0x04] = new Instruction(1, true);
        opcodes[0x05] = new Instruction(1, true);
        opcodes[0x06] = new Instruction(2, true);
        opcodes[0x07] = new Instruction(1, true);
        opcodes[0x08] = new Instruction(3, true);
        opcodes[0x09] = new Instruction(1, true);
        opcodes[0x0A] = new Instruction(1, true);
        opcodes[0x0B] = new Instruction(1, true);
        opcodes[0x0C] = new Instruction(1, true);
        opcodes[0x0D] = new Instruction(1, true);
        opcodes[0x0E] = new Instruction(2, true);
        opcodes[0x0f] = new Instruction(1, true);
        opcodes[0x10] = new Instruction(2, true);
        opcodes[0x11] = new Instruction(3, true);
        opcodes[0x12] = new Instruction(1, true);
        opcodes[0x13] = new Instruction(1, true);
        opcodes[0x14] = new Instruction(1, true);
        opcodes[0x15] = new Instruction(1, true);
        opcodes[0x16] = new Instruction(2, true);
        opcodes[0x17] = new Instruction(1, true);
        opcodes[0x18] = new Instruction(2, false);
        opcodes[0x19] = new Instruction(1, true);
        opcodes[0x1A] = new Instruction(1, true);
        opcodes[0x1B] = new Instruction(1, true);
        opcodes[0x1C] = new Instruction(1, true);
        opcodes[0x1D] = new Instruction(1, true);
        opcodes[0x1E] = new Instruction(2, true);
        opcodes[0x1f] = new Instruction(1, true);
        opcodes[0x20] = new Instruction(2, false);
        opcodes[0x21] = new Instruction(3, true);
        opcodes[0x22] = new Instruction(1, true);
        opcodes[0x23] = new Instruction(1, true);
        opcodes[0x24] = new Instruction(1, true);
        opcodes[0x25] = new Instruction(1, true);
        opcodes[0x26] = new Instruction(2, true);
        opcodes[0x28] = new Instruction(2, false);
        opcodes[0x29] = new Instruction(1, true);
        opcodes[0x2A] = new Instruction(1, true);
        opcodes[0x2B] = new Instruction(1, true);
        opcodes[0x2C] = new Instruction(1, true);
        opcodes[0x2D] = new Instruction(1, true);
        opcodes[0x2E] = new Instruction(2, true);
        opcodes[0x2F] = new Instruction(1, true);
        opcodes[0x30] = new Instruction(2, false);
        opcodes[0x31] = new Instruction(3, true);
        opcodes[0x32] = new Instruction(1, true);
        opcodes[0x33] = new Instruction(1, true);
        opcodes[0x34] = new Instruction(1, true);
        opcodes[0x35] = new Instruction(1, true);
        opcodes[0x36] = new Instruction(2, true);
        opcodes[0x37] = new Instruction(1, true);
        opcodes[0x38] = new Instruction(2, false);
        opcodes[0x39] = new Instruction(1, true);
        opcodes[0x3A] = new Instruction(1, true);
        opcodes[0x3B] = new Instruction(1, true);
        opcodes[0x3C] = new Instruction(1, true);
        opcodes[0x3D] = new Instruction(1, true);
        opcodes[0x3E] = new Instruction(2, true);
        opcodes[0x3F] = new Instruction(1, true);
        opcodes[0x40] = new Instruction(1, true);
        opcodes[0x41] = new Instruction(1, true);
        opcodes[0x42] = new Instruction(1, true);
        opcodes[0x43] = new Instruction(1, true);
        opcodes[0x44] = new Instruction(1, true);
        opcodes[0x45] = new Instruction(1, true);
        opcodes[0x46] = new Instruction(1, true);
        opcodes[0x47] = new Instruction(1, true);
        opcodes[0x48] = new Instruction(1, true);
        opcodes[0x49] = new Instruction(1, true);
        opcodes[0x4A] = new Instruction(1, true);
        opcodes[0x4B] = new Instruction(1, true);
        opcodes[0x4C] = new Instruction(1, true);
        opcodes[0x4D] = new Instruction(1, true);
        opcodes[0x4E] = new Instruction(1, true);
        opcodes[0x4F] = new Instruction(1, true);
        opcodes[0x50] = new Instruction(1, true);
        opcodes[0x51] = new Instruction(1, true);
        opcodes[0x52] = new Instruction(1, true);
        opcodes[0x53] = new Instruction(1, true);
        opcodes[0x54] = new Instruction(1, true);
        opcodes[0x55] = new Instruction(1, true);
        opcodes[0x56] = new Instruction(1, true);
        opcodes[0x57] = new Instruction(1, true);
        opcodes[0x58] = new Instruction(1, true);
        opcodes[0x59] = new Instruction(1, true);
        opcodes[0x5A] = new Instruction(1, true);
        opcodes[0x5B] = new Instruction(1, true);
        opcodes[0x5C] = new Instruction(1, true);
        opcodes[0x5D] = new Instruction(1, true);
        opcodes[0x5E] = new Instruction(1, true);
        opcodes[0x5F] = new Instruction(1, true);
        opcodes[0x60] = new Instruction(1, true);
        opcodes[0x61] = new Instruction(1, true);
        opcodes[0x62] = new Instruction(1, true);
        opcodes[0x63] = new Instruction(1, true);
        opcodes[0x64] = new Instruction(1, true);
        opcodes[0x65] = new Instruction(1, true);
        opcodes[0x66] = new Instruction(1, true);
        opcodes[0x67] = new Instruction(1, true);
        opcodes[0x68] = new Instruction(1, true);
        opcodes[0x69] = new Instruction(1, true);
        opcodes[0x6A] = new Instruction(1, true);
        opcodes[0x6B] = new Instruction(1, true);
        opcodes[0x6C] = new Instruction(1, true);
        opcodes[0x6D] = new Instruction(1, true);
        opcodes[0x6E] = new Instruction(1, true);
        opcodes[0x6F] = new Instruction(1, true);
        opcodes[0x70] = new Instruction(1, true);
        opcodes[0x71] = new Instruction(1, true);
        opcodes[0x72] = new Instruction(1, true);
        opcodes[0x73] = new Instruction(1, true);
        opcodes[0x74] = new Instruction(1, true);
        opcodes[0x75] = new Instruction(1, true);
        opcodes[0x76] = new Instruction(1, true);
        opcodes[0x77] = new Instruction(1, true);
        opcodes[0x78] = new Instruction(1, true);
        opcodes[0x79] = new Instruction(1, true);
        opcodes[0x7A] = new Instruction(1, true);
        opcodes[0x7B] = new Instruction(1, true);
        opcodes[0x7C] = new Instruction(1, true);
        opcodes[0x7D] = new Instruction(1, true);
        opcodes[0x7E] = new Instruction(1, true);
        opcodes[0x7F] = new Instruction(1, true);
        opcodes[0x80] = new Instruction(1, true);
        opcodes[0x81] = new Instruction(1, true);
        opcodes[0x82] = new Instruction(1, true);
        opcodes[0x83] = new Instruction(1, true);
        opcodes[0x84] = new Instruction(1, true);
        opcodes[0x85] = new Instruction(1, true);
        opcodes[0x86] = new Instruction(1, true);
        opcodes[0x87] = new Instruction(1, true);
        opcodes[0x88] = new Instruction(1, true);
        opcodes[0x89] = new Instruction(1, true);
        opcodes[0x8A] = new Instruction(1, true);
        opcodes[0x8B] = new Instruction(1, true);
        opcodes[0x8C] = new Instruction(1, true);
        opcodes[0x8D] = new Instruction(1, true);
        opcodes[0x8E] = new Instruction(1, true);
        opcodes[0x8F] = new Instruction(1, true);
        opcodes[0x90] = new Instruction(1, true);
        opcodes[0x91] = new Instruction(1, true);
        opcodes[0x92] = new Instruction(1, true);
        opcodes[0x93] = new Instruction(1, true);
        opcodes[0x94] = new Instruction(1, true);
        opcodes[0x95] = new Instruction(1, true);
        opcodes[0x96] = new Instruction(1, true);
        opcodes[0x97] = new Instruction(1, true);
        opcodes[0x98] = new Instruction(1, true);
        opcodes[0x99] = new Instruction(1, true);
        opcodes[0x9A] = new Instruction(1, true);
        opcodes[0x9B] = new Instruction(1, true);
        opcodes[0x9C] = new Instruction(1, true);
        opcodes[0x9D] = new Instruction(1, true);
        opcodes[0x9E] = new Instruction(1, true);
        opcodes[0x9F] = new Instruction(1, true);
        opcodes[0xa0] = new Instruction(1, true);
        opcodes[0xa1] = new Instruction(1, true);
        opcodes[0xa2] = new Instruction(1, true);
        opcodes[0xa3] = new Instruction(1, true);
        opcodes[0xa4] = new Instruction(1, true);
        opcodes[0xa5] = new Instruction(1, true);
        opcodes[0xa6] = new Instruction(1, true);
        opcodes[0xa7] = new Instruction(1, true);
        opcodes[0xa8] = new Instruction(1, true);
        opcodes[0xa9] = new Instruction(1, true);
        opcodes[0xaa] = new Instruction(1, true);
        opcodes[0xab] = new Instruction(1, true);
        opcodes[0xac] = new Instruction(1, true);
        opcodes[0xad] = new Instruction(1, true);
        opcodes[0xae] = new Instruction(1, true);
        opcodes[0xaf] = new Instruction(1, true);
        opcodes[0xb0] = new Instruction(1, true);
        opcodes[0xb1] = new Instruction(1, true);
        opcodes[0xb2] = new Instruction(1, true);
        opcodes[0xb3] = new Instruction(1, true);
        opcodes[0xb4] = new Instruction(1, true);
        opcodes[0xb5] = new Instruction(1, true);
        opcodes[0xb6] = new Instruction(1, true);
        opcodes[0xb7] = new Instruction(1, true);
        opcodes[0xb8] = new Instruction(1, true);
        opcodes[0xb9] = new Instruction(1, true);
        opcodes[0xba] = new Instruction(1, true);
        opcodes[0xbb] = new Instruction(1, true);
        opcodes[0xbc] = new Instruction(1, true);
        opcodes[0xbd] = new Instruction(1, true);
        opcodes[0xbe] = new Instruction(1, true);
        opcodes[0xbf] = new Instruction(1, true);
        opcodes[0xc0] = new Instruction(1, false);
        opcodes[0xc1] = new Instruction(1, true);
        opcodes[0xc2] = new Instruction(3, false);
        opcodes[0xc3] = new Instruction(2, false);
        opcodes[0xc4] = new Instruction(3, false);
        opcodes[0xc5] = new Instruction(1, true);
        opcodes[0xc6] = new Instruction(2, true);
        opcodes[0xc7] = new Instruction(1, false);
        opcodes[0xc8] = new Instruction(1, false);
        opcodes[0xc9] = new Instruction(1, false);
        opcodes[0xca] = new Instruction(3, false);
        opcodes[0xcb] = new Instruction(2, true);
        opcodes[0xcc] = new Instruction(3, false);
        opcodes[0xcd] = new Instruction(3, false);
        opcodes[0xce] = new Instruction(1, true);
        opcodes[0xcf] = new Instruction(1, false);
        opcodes[0xd0] = new Instruction(1, false);
        opcodes[0xd1] = new Instruction(1, true);
        opcodes[0xd2] = new Instruction(3, false);
        opcodes[0xd4] = new Instruction(3, false);
        opcodes[0xd5] = new Instruction(1, true);
        opcodes[0xd6] = new Instruction(2, true);
        opcodes[0xd7] = new Instruction(1, false);
        opcodes[0xd8] = new Instruction(1, false);
        opcodes[0xd9] = new Instruction(1, false);
        opcodes[0xda] = new Instruction(3, false);
        opcodes[0xdc] = new Instruction(3, false);
        opcodes[0xde] = new Instruction(2, true);
        opcodes[0xdf] = new Instruction(1, false);
        opcodes[0xE0] = new Instruction(2, true);
        opcodes[0xE1] = new Instruction(1, true);
        opcodes[0xE2] = new Instruction(1, true);
        opcodes[0xe5] = new Instruction(1, true);
        opcodes[0xe6] = new Instruction(2, true);
        opcodes[0xe7] = new Instruction(1, false);
        opcodes[0xe8] = new Instruction(2, true);
        opcodes[0xe9] = new Instruction(1, false);
        opcodes[0xea] = new Instruction(3, true);
        opcodes[0xee] = new Instruction(2, true);
        opcodes[0xef] = new Instruction(1, false);
        opcodes[0xF0] = new Instruction(2, true);
        opcodes[0xF1] = new Instruction(1, true);
        opcodes[0xF2] = new Instruction(1, true);
        opcodes[0xf3] = new Instruction(1, true);
        opcodes[0xf5] = new Instruction(1, true);
        opcodes[0xf6] = new Instruction(2, true);
        opcodes[0xf7] = new Instruction(1, false);
        opcodes[0xf8] = new Instruction(2, true);
        opcodes[0xf9] = new Instruction(1, true);
        opcodes[0xfa] = new Instruction(3, true);
        opcodes[0xfb] = new Instruction(1, true);
        opcodes[0xfe] = new Instruction(2, true);
        opcodes[0xff] = new Instruction(1, false);
    }

}
