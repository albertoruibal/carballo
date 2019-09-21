package com.alonsoruibal.chess.swing;

import javax.swing.*;

public class PgnDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JTextArea textArea;
	
	public PgnDialog(JFrame parent) {
		super(parent, "PGN", true);

		textArea = new JTextArea();
		
		Box b = Box.createVerticalBox();
		b.add(Box.createGlue());
		b.add(textArea);
		b.add(Box.createGlue());
		getContentPane().add(b, "Center");

		JPanel p2 = new JPanel();
		JButton ok = new JButton("Ok");
		p2.add(ok);
		getContentPane().add(p2, "South");

		ok.addActionListener(evt -> setVisible(false));

		setSize(600, 600);
	}

	public void setText(String text) {
		textArea.setText(text);
	}
}