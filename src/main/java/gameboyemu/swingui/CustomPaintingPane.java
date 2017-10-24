package gameboyemu.swingui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Created with IntelliJ IDEA.
 * User: par
 * Date: 7/3/12
 * Time: 1:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomPaintingPane extends JPanel {

    Rectangle2D.Double r = null;
    private BufferedImage image = null;

    private TexturePaint texturePaint = null;
    private Dimension size;

    public CustomPaintingPane(Dimension size) {
        this.size = size;

        setMinimumSize(size);
        setMaximumSize(size);
        setPreferredSize(size);

    }

    public void setPixels(int [] backBuffer) {
        WritableRaster writableRaster = image.getRaster();
        writableRaster.setPixels(0, 0, (int) size.getWidth(), (int) size.getHeight(), backBuffer);
        System.err.println("writeableRaster.getWidth=" + writableRaster.getWidth());

    }

    @Override
    public void paint(Graphics g) {
        if(image == null) {
            initBackBufferImage();
        }
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(texturePaint);
        g2d.fill(r);
    }

    private void initBackBufferImage() {
        image = (BufferedImage) this.createImage((int) size.getWidth(), (int) size.getHeight());
        System.err.println("image" + image);
        System.err.println("this.width:" + getWidth());
        System.err.println("this.height:" + getHeight());

        r = new Rectangle2D.Double(0,0, size.getWidth(), size.getHeight());
        texturePaint = new TexturePaint(image, r);
    }



}
