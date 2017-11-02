package gameboyemu.swingui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import gameboyemu.cpu.BreakPointException;
import gameboyemu.cpu.CpuState;
import gameboyemu.cpu.LR35902StateBased;
import gameboyemu.game.MBC;
import gameboyemu.game.Rom;
import gameboyemu.game.Tetris;
import gameboyemu.system.GBSystem;
import gameboyemu.system.HRAMimpl;
import gameboyemu.system.LCDController;
import gameboyemu.system.MemoryController;
import gameboyemu.system.Utils;
import gameboyemu.system.VRAM;
import gameboyemu.system.VRAMimpl;
import gameboyemu.system.WRAMimpl;

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
        instance = this;
        $$$setupUI$$$();
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
                if (BP0enable.isSelected()) {
                    breakpoints.add(Utils.hexStringToInt(BP0value.getText()));
                }
                if (BP1enable.isSelected()) {
                    breakpoints.add(Utils.hexStringToInt(BP1value.getText()));
                }

                int backbuffer[] = new int[160 * 144 * 3];
                try {
                    system.executeFrame(0, backbuffer, breakpoints);
                } catch (BreakPointException e1) {
                    System.err.println("Breakpoint!");
                }
                ((CustomPaintingPane) screenPanel).setPixels(backbuffer);

                int tileBuffer[] = new int[256 * 256 * 3];
                lcdController.renderTiles(tileBuffer);
                ((CustomPaintingPane) tilePanel).setPixels(tileBuffer);
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
        debuggerForm.lcdController.setCpuInteruptLines(debuggerForm.cpu);

        System.err.println("Starting");
        debuggerForm.cpu.startup();

        debuggerForm.updateGuiFromCpuState(debuggerForm.cpu, debuggerForm.lcdController);
        debuggerForm.updateGuiFromCpuState(debuggerForm.cpu, debuggerForm.lcdController);

    }

    LR35902StateBased cpu;

    private void updateField(JLabel label, JLabel value, String newValue) {
        String oldText = value.getText();
        value.setText(newValue);
        setBold(!oldText.equals(newValue), label, value);

    }

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

        String cpuDesc = Utils.byteIntToHexString(cpu.getPC()) + " (" + Utils.byteIntToHexString(this.cpu.vm().readByte(cpu.getPC())) + ")";

        updateField(PClabel, PCvalue,cpuDesc);

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
        str += "Lcd Display Enable: " + lcdController.getLcdDisplayEnable() + "<br>";
        str += "Window Tile Map Display Select: 0x" + Utils.wordIntToHexString(lcdController.getWindowTileMapDisplayBase()) + "<br>";
        str += "Window Display Enable: " + lcdController.getWindowDisplayEnable() + "<br>";
        str += "BG & Window Tile Data Select: 0x" + Utils.wordIntToHexString(lcdController.getBGAndWindowTileDataBase()) + "<br>";
        str += "BG Tile Map Display Select: 0x" + Utils.wordIntToHexString(lcdController.getBGTileMapDisplayBase()) + "<br>";
        str += "</html>";

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

        tilePanel = new CustomPaintingPane(new Dimension(256, 256));

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

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        rootJPanel = new JPanel();
        rootJPanel.setLayout(new FormLayout("fill:m:grow,left:4dlu:noGrow,fill:max(m;4px):noGrow,left:4dlu:noGrow,fill:m:noGrow,left:4dlu:noGrow,fill:m:grow", "center:d:noGrow,top:11dlu:noGrow,center:72px:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        CellConstraints cc = new CellConstraints();
        rootJPanel.add(panel1, cc.xy(7, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        zCheckBox = new JCheckBox();
        zCheckBox.setEnabled(true);
        zCheckBox.setText("z");
        panel1.add(zCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nCheckBox = new JCheckBox();
        nCheckBox.setEnabled(true);
        nCheckBox.setText("n");
        panel1.add(nCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hCheckBox = new JCheckBox();
        hCheckBox.setEnabled(true);
        hCheckBox.setText("h");
        panel1.add(hCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cCheckBox = new JCheckBox();
        cCheckBox.setEnabled(true);
        Font cCheckBoxFont = this.$$$getFont$$$(null, Font.BOLD, -1, cCheckBox.getFont());
        if (cCheckBoxFont != null) cCheckBox.setFont(cCheckBoxFont);
        cCheckBox.setText("c");
        panel1.add(cCheckBox, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootJPanel.add(panel2, cc.xy(5, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        AFvalue = new JLabel();
        AFvalue.setText("01B0");
        panel2.add(AFvalue, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        AFlabel = new JLabel();
        AFlabel.setText("AF");
        panel2.add(AFlabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        BClabel = new JLabel();
        BClabel.setText("BC");
        panel2.add(BClabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        BCvalue = new JLabel();
        BCvalue.setText("0000");
        panel2.add(BCvalue, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        DElabel = new JLabel();
        DElabel.setText("DE");
        panel2.add(DElabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        DEvalue = new JLabel();
        DEvalue.setText("0000");
        panel2.add(DEvalue, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        HLlabel = new JLabel();
        HLlabel.setText("HL");
        panel2.add(HLlabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SPlabel = new JLabel();
        SPlabel.setText("SP");
        panel2.add(SPlabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PClabel = new JLabel();
        PClabel.setText("PC");
        panel2.add(PClabel, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        HLvalue = new JLabel();
        HLvalue.setText("0000");
        panel2.add(HLvalue, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SPvalue = new JLabel();
        SPvalue.setText("0000");
        panel2.add(SPvalue, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PCvalue = new JLabel();
        PCvalue.setText("0000");
        panel2.add(PCvalue, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootJPanel.add(panel3, cc.xy(5, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        BP0value = new JTextPane();
        BP0value.setText("0100");
        panel3.add(BP0value, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 10), null, 0, false));
        BP1value = new JTextPane();
        BP1value.setText("0100");
        panel3.add(BP1value, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 10), null, 0, false));
        BP0enable = new JCheckBox();
        BP0enable.setHideActionText(true);
        BP0enable.setHorizontalAlignment(4);
        BP0enable.setText("BP0");
        panel3.add(BP0enable, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        BP1enable = new JCheckBox();
        BP1enable.setHideActionText(true);
        BP1enable.setHorizontalAlignment(4);
        BP1enable.setText("BP1");
        panel3.add(BP1enable, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Breakpoints");
        panel3.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootJPanel.add(panel4, cc.xy(3, 3));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(467, 29), null, 0, false));
        step1Button = new JButton();
        step1Button.setText("Step");
        panel5.add(step1Button, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        runButton = new JButton();
        runButton.setText("Run");
        panel5.add(runButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        step10Button = new JButton();
        step10Button.setText("Step 10");
        panel5.add(step10Button, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        runFrame = new JButton();
        runFrame.setText("Run Frame");
        rootJPanel.add(runFrame, cc.xy(3, 5));
        screenPanel.setMaximumSize(new Dimension(320, 288));
        screenPanel.setMinimumSize(new Dimension(320, 288));
        rootJPanel.add(screenPanel, cc.xy(3, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
        screenPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        registerDesc = new JLabel();
        registerDesc.setBackground(UIManager.getColor("Control.highlight"));
        registerDesc.setHorizontalAlignment(2);
        registerDesc.setHorizontalTextPosition(2);
        registerDesc.setMaximumSize(new Dimension(80, 112));
        registerDesc.setMinimumSize(new Dimension(80, 14));
        registerDesc.setPreferredSize(new Dimension(80, 112));
        registerDesc.setText("Label");
        registerDesc.setVerticalAlignment(1);
        registerDesc.setVerticalTextPosition(1);
        rootJPanel.add(registerDesc, cc.xy(1, 1));
        tilePanel.setBackground(new Color(-6684877));
        tilePanel.setDebugGraphicsOptions(1);
        tilePanel.setDoubleBuffered(true);
        tilePanel.setEnabled(true);
        tilePanel.setMaximumSize(new Dimension(256, 256));
        tilePanel.setMinimumSize(new Dimension(256, 256));
        tilePanel.setPreferredSize(new Dimension(256, 256));
        rootJPanel.add(tilePanel, cc.xy(1, 7, CellConstraints.CENTER, CellConstraints.CENTER));
        tilePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootJPanel;
    }
}
