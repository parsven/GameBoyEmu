package gameboyemu.system;


import gameboyemu.cpu.VirtualMemory;

public class Utils {
    public static String byteIntToHexString(int v) {
        return String.format("%02x", v);
    }

    public static String wordIntToHexString(int v) {
        return String.format("%04x", v);
    }

    public static int hexStringToInt(String v) {
        v = v.trim();
        return Integer.parseInt(v,16);
    }

    public static class VMLogger implements VirtualMemory {

        private final VirtualMemory delegate;

        public VMLogger(VirtualMemory delegate) {
            this.delegate = delegate;
        }

        public void writeByte(int adress, int b) {
            delegate.writeByte(adress, b);
        }

        public int readByte(int adress) {
            return delegate.readByte(adress);
        }

    }
}