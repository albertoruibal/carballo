package com.alonsoruibal.chess.applet;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class PieceJLabel extends JLabel {
	private static final long serialVersionUID = -4865276927847037885L;

	char piece;
	
	public static String style = "/merida.png";
	
	public char getPiece() {
		return piece;
	}

	public PieceJLabel(char piece) {
		this.piece = piece;
		ImageIcon icon1 = new ImageIcon(this.getClass().getResource(style));
		BufferedImage cut = new BufferedImage(75, 75, BufferedImage.TYPE_4BYTE_ABGR);
		int xindex = "PNBRQK".indexOf(Character.toUpperCase(piece)) * -75;
		int yindex = piece == Character.toUpperCase(piece) ? 0 : -75;
		cut.getGraphics().drawImage(icon1.getImage(), xindex, yindex, this);
		setIcon(new ImageIcon(cut));
	}

	/**
	 * for antialiasing
	 */
	public void paint(java.awt.Graphics g) {
		Graphics2D g2 = ((Graphics2D) g);
		g2.setRenderingHint(
				java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
				java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	
		super.paint(g2);
	}
	
}
