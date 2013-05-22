package com.alonsoruibal.chess.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class SquareJPanel extends JPanel {
	private static final long serialVersionUID = -4865276927847037885L;

	private static BufferedImage imgWhite;
	private static BufferedImage imgBlack;
	
	static {
		imgWhite = new BufferedImage(75, 75, BufferedImage.TYPE_4BYTE_ABGR);
		imgBlack = new BufferedImage(75, 75, BufferedImage.TYPE_4BYTE_ABGR);
	}
	
	public static void loadImages(URL url) {
		ImageIcon icon1 = new ImageIcon(url);
		imgWhite.getGraphics().drawImage(icon1.getImage(), -75, 0, null);
		imgBlack.getGraphics().drawImage(icon1.getImage(), 0, 0, null);	
	}

	private int index;
	private boolean highlighted;

	private boolean color;

	public SquareJPanel(int index) {
		super(new BorderLayout());
		this.index = index;
		this.highlighted = false;
		
	    Dimension size = new Dimension(75, 75);
	    setPreferredSize(size);
	    setMinimumSize(size);
	    setMaximumSize(size);
	    setSize(size);

		int row = (index / 8) % 2;
		if (row == 0) {
			color = index % 2 == 0;
		} else {
			color = index % 2 != 0;
		}		
	}

	public void paintComponent(Graphics g) {
	    g.drawImage(color ? imgWhite : imgBlack, 0, 0, 75, 75, null);
	    if (highlighted) {
	    	g.setColor(Color.yellow);
	    	//g.drawRect(0, 0, 74, 74);
	    	g.drawRect(1, 1, 72, 72);
	    	g.drawRect(2, 2, 70, 70);
	    	g.drawRect(3, 3, 68, 68);
	    	g.drawRect(4, 4, 66, 66);
	    }
	}
	
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	public int getIndex() {
		return index;
	}
}
