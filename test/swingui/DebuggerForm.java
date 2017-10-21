package swingui;

import cpu.BreakPointException;
import cpu.CpuState;
import cpu.LR35902StateBased;
import game.MBC;
import game.Rom;
import game.Tetris;
import system.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: par
 * Date: 2012-02-19
 * Time: 19:54
 */
public class DebuggerForm {
    private JCheckBox zCheckBox;
    private JCheckBox nCheckBox;
    private JCheckBox hCheckBox;
    private JCheckBox cCheckBox;
    private JLabel AFvalue;
    private JLabel AFlabel;
    private JLabel BClabel;
    private JLabel BCvalue;
    private JLabel DElabel;
    private JLabel DEvalue;
    private JLabel HLlabel;
    private JLabel SPlabel;
    private JLabel PClabel;
    private JLabel HLvalue;
    private JLabel SPvalue;
    private JLabel PCvalue;
    private JTextPane BP0value;
    private JTextPane BP1value;
    private JCheckBox BP0enable;
    private JCheckBox BP1enable;
    private JButton step1Button;
    private JButton runButton;
    private JButton step10Button;
    private JPanel rootJPanel;
    private JButton runFrame;
    private JPanel screenPanel;
    private JLabel registerDesc;
    private JPanel tilePanel;
    private GBSystem system;
    private LCDController lcdController;


    private static DebuggerForm instance;

    public DebuggerForm() {
        instance =this;
        step1Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cpu.step();
                updateGuiFromCpuState(cpu, lcdController);
            }
        });
        runFrame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Set<Integer> breakpoints = new HashSet<>();
                if(BP0enable.isSelected()) {
                    breakpoints.add(Utils.hexStringToInt(BP0value.getText()));
                }
                if(BP1enable.isSelected()) {
                    breakpoints.add(Utils.hexStringToInt(BP1value.getText()));
                }

                int backbuffer[] = new int[160*144*3];
                try {
                    system.executeFrame(0, backbuffer, breakpoints);
                } catch (BreakPointException e1) {
                    System.err.println("Breakpoint!");
                }
                ((CustomPaintingPane)screenPanel).setPixels(backbuffer);

                int tileBuffer[] = new int[256*256*3];
                lcdController.renderTiles(tileBuffer);
                ((CustomPaintingPane)tilePanel).setPixels(tileBuffer);
	            updateGuiFromCpuState(cpu, lcdController);
            }
        });
    }

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, IllegalAccessException, InstantiationException {
        UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        JFrame frame = new JFrame("DebuggerForm");
        DebuggerForm debuggerForm = new DebuggerForm();
        frame.setContentPane(debuggerForm.rootJPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        Rom rom = new Rom(Tetris.getRomContent());
        MBC mbc = rom.createMBC();

        WRAMimpl workRam = new WRAMimpl();
        HRAMimpl hram = new HRAMimpl();
        debuggerForm.lcdController = new LCDController();
        VRAM vram = new VRAMimpl();
        MemoryController memoryController = new MemoryController(mbc, workRam, hram, vram, debuggerForm.lcdController);
        debuggerForm.lcdController.setMemoryController(memoryController);

        debuggerForm.cpu = new LR35902StateBased(memoryController);
        debuggerForm.system = new GBSystem(memoryController, debuggerForm.cpu, debuggerForm.lcdController);

        System.err.println("Starting");
        debuggerForm.cpu.startup();

        debuggerForm.updateGuiFromCpuState(debuggerForm.cpu, debuggerForm.lcdController);
        debuggerForm.updateGuiFromCpuState(debuggerForm.cpu, debuggerForm.lcdController);

    }
    LR35902StateBased cpu;
    private void updateField(JLabel label, JLabel value, int newValue) {
        String oldText = value.getText();
        String newText = Utils.wordIntToHexString(newValue);
        value.setText(newText);
        setBold(!oldText.equals(newText), label, value);
    }

    private void updateGuiFromCpuState(CpuState cpu, LCDController lcdController) {
        updateField(AFlabel, AFvalue, cpu.getAF());
        updateField(BClabel, BCvalue, cpu.getBC());
        updateField(DElabel, DEvalue, cpu.getDE());
        updateField(HLlabel, HLvalue, cpu.getHL());
        updateField(SPlabel, SPvalue, cpu.getSP());
        updateField(PClabel, PCvalue, cpu.getPC());

        updateRegisterDescField(lcdController);
        updateDisasm(cpu.getPC());
    }

    private void updateDisasm(int pc) {
        String[] cmd = {
                "/bin/bash",
                "-c",
                "grep --colour=always -E ':" + Utils.wordIntToHexString(pc).toUpperCase() + "' -C10 gb.dis >out.txt"
        };
        try {
            Process p = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int oldFF40 = 0;
    private void updateRegisterDescField(LCDController lcdController) {

        String str = "<html>FF40 - LCDC =0x" + Utils.byteIntToHexString(lcdController.getLcdc()) + "<br>";
        str +="Lcd Display Enable: " + lcdController.getLcdDisplayEnable() + "<br>";
        str +="Window Tile Map Display Select: 0x" + Utils.wordIntToHexString(lcdController.getWindowTileMapDisplayBase()) + "<br>";
        str +="Window Display Enable: " + lcdController.getWindowDisplayEnable() + "<br>";
        str +="BG & Window Tile Data Select: 0x" + Utils.wordIntToHexString(lcdController.getBGAndWindowTileDataBase()) + "<br>";
        str +="BG Tile Map Display Select: 0x" + Utils.wordIntToHexString(lcdController.getBGTileMapDisplayBase()) + "<br>";
        str +="</html>";

        registerDesc.setText(str);


    }

    private void setBold(boolean bold, JLabel a, JLabel b) {
        setBold(bold, a);
        setBold(bold, b);
    }

    //Todo: Performance?
    private void setBold(boolean bold, JLabel label) {
        Font font = label.getFont();
        label.setFont(font.deriveFont(bold ? Font.BOLD : Font.PLAIN));
    }

    private void createUIComponents() {
        screenPanel = new CustomPaintingPane(new Dimension(160, 144));

        tilePanel = new CustomPaintingPane(new Dimension(256,256));

/*        tilePanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                if(screenPanelImage == null) {
                    initBackBufferImage();
                }
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(screenPanelTexturePaint);
                g2d.fill(screenPanelRectangle);
            }
        };
        Dimension tilePanelSize = new Dimension(256, 256);
        tilePanel.setMinimumSize(tilePanelSize);
        tilePanel.setMaximumSize(tilePanelSize);
        tilePanel.setPreferredSize(tilePanelSize);

*/
    }

}
