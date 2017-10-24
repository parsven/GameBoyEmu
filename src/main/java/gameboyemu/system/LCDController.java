package gameboyemu.system;


import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: par
 * Date: 2012-02-23
 * Time: 21:41
 * To change this template use File | Settings | File Templates.
 */
public class LCDController implements IoPorts {

    private int currentScanLine = 0;
    private int lcdc;
    private MemoryController mc;
    private int joyselect;


    public void setMemoryController(MemoryController mc) {
        this.mc = mc;
    }
    public void advanceScanLine() {
        currentScanLine++;
    }

    public void resetScanLine() {
        currentScanLine = 0;
    }

    @Override
    public void writeByte(int adress, int b) {
        switch(adress) {
            case 0xFF00: //Joypad
                joyselect = b & 0x30;
            case 0xFF40: // LCDC
                lcdc = b;
                break;
            case 0xFF46:
                System.err.println("OAM DMA!!");
                break;
            default: //Ignore sofar..
                System.err.println("writeByte(" + Utils.wordIntToHexString(adress) + " , " + Utils.byteIntToHexString(b) + ")");

        }
    }

    @Override
    public int readByte(int adress) {

        if(adress == 0xff44) { //LY
    //        System.err.println("readByte(" + Utils.wordIntToHexString(adress) + " LY) -> " + currentScanLine);
            return currentScanLine;
        }
        if(adress == 0xff00) { //Joypad
            return 0xcf | joyselect;
        }
        System.err.println("readByte(" + Utils.wordIntToHexString(adress) + ") -> 0");
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getWindowTileMapDisplayBase() {
        return (lcdc & 0x40) == 0 ? 0x9800 : 0x9c00;
    }
    boolean isWindowDisplayEnable() {
        return (lcdc & 0x20) != 0;
    }

    public int getBGAndWindowTileDataBase() {
        return (lcdc & 0x10) == 0 ? 0x8800 : 0x8000;
    }
    
    public int getBGTileMapDisplayBase() {
        return (lcdc & 0x08) == 0 ? 0x9800 : 0x9c00;
    }
    
    public void renderLine(int dest[], int offset) {
        ArrayList<Integer> tiles = new ArrayList<Integer>(20);

        int base = (currentScanLine / 8) * 32;    // TODO: !!!
        int tileYOffset = currentScanLine - (currentScanLine / 8) * 8; // TODO: !!!

        for(int i = 0; i < 20 ; i++) {
            tiles.add(mc.readByte(getBGTileMapDisplayBase() + base + i));
        }
        for (Integer tile : tiles) {

            int i = getBGAndWindowTileDataBase() + tile * 16 + tileYOffset * 2;
            int hi = mc.readByte(i);
            int lo = mc.readByte(i+1);


            for (int j = 7 ; j >= 0 ; j--) {
                int c = (hi & (1 << j)) >> j;
                int d = (lo & (1 << j)) >> j;
                int paletteEntry = c * 2 + d;
                int color = getColorFromPaletteEntry(paletteEntry);
                dest[offset++] = color;
                dest[offset++] = color;
                dest[offset++] = color;
            }
        }
    }


    public void renderTile(int tileNo, int tileBase, int dest[], int offset, int stride) {

        int baseOfThisTile = tileBase + tileNo * 16;

        //int color = new Random().nextInt(256);

        for( int i = 0 ; i < 8 ; i++) {
            int hi = mc.readByte(baseOfThisTile + i*2);
            int lo = mc.readByte(baseOfThisTile + i*2 + 1);

            for(int j= 0 ; j < 8 ; j++) {
                int c = (hi & 0x80) != 0 ? 1 : 0;
                int d = (lo & 0x80) != 0 ? 1 : 0;
                hi <<= 1;
                lo <<= 1;
                int color = getColorFromPaletteEntry(c*2 +d);
                dest[offset + j*3 + stride * i * 3 + 0] = color;
                dest[offset + j*3 + stride * i * 3 + 1] = color;
                dest[offset + j*3 + stride * i * 3 + 2] = color;
            }

        }
    }

    private int getColorFromPaletteEntry(int paletteEntry) {
        switch(paletteEntry) {
            case 0:
                return 0x12;
            case 1:
                return 0x40;
            case 2:
                return 0x80;
            default:
                return 0xc0;
        }
    }

    public boolean getLcdDisplayEnable() {
        return (lcdc & 0x80) != 0;
    }

    public int getLcdc() {
        return lcdc;
    }

    public boolean getWindowDisplayEnable() {
        return (lcdc & 0x20) != 0;
    }

    public void renderTiles(int[] tileBuffer) {

        int tileNo = 0;
        for(int y = 0 ; y < 16 ; y++) {
            for(int x = 0 ; x < 16 ; x++) {

                int offset = 256*3*8*y + 8 * x * 3;
                renderTile(tileNo++, 0x8000, tileBuffer, offset, 256);
            }
        }

    }
}
