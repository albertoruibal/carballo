package com.alonsoruibal.chess.applet;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

 
public class BoardJPanel extends JPanel implements MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = 1L;
	
	JLayeredPane layeredPane;
    JPanel chessBoard;
    PieceJLabel chessPiece;
    int xAdjustment;
    int yAdjustment;
    final int height = 75*8;
    final int width = 75*8;    
    SquareJPanel originComponent;
    ChessApplet chess;
    String lastFen;
    boolean flip;
    
    private boolean acceptInput;
    
    public void setAcceptInput(boolean acceptInput) {
		this.acceptInput = acceptInput;
	}

	public BoardJPanel(ChessApplet chess)
    {
    	Dimension d = new Dimension(width, height);
    	
        layeredPane = new JLayeredPane();
        add(layeredPane);
        layeredPane.setPreferredSize( d );
        layeredPane.addMouseListener( this );
        layeredPane.addMouseMotionListener( this );

        chessBoard = new JPanel();
        layeredPane.add(chessBoard, JLayeredPane.DEFAULT_LAYER);
        chessBoard.setLayout( new GridLayout(8, 8) );
        chessBoard.setPreferredSize(d);
 
        for (int i = 0; i < 64; i++) chessBoard.add(new SquareJPanel(i));
        
        chessBoard.setBounds(0, 0, width, height);
        layeredPane.setBounds(0, 0, width, height);
        setBounds(0, 0, width, height);
        
        acceptInput = true;
        this.chess = chess;
    }
 
    /**
     *  Add the selected chess piece to the drag and drop layer
    */
    public void mousePressed(MouseEvent e)
    {
    	if (!acceptInput) return;
        chessPiece = null;
        Component c =  chessBoard.findComponentAt(e.getX(), e.getY());
 
        if (c instanceof SquareJPanel) return;
        originComponent = (SquareJPanel) c.getParent();

        // TODO set legal moves
        
        Point parentLocation = c.getParent().getLocation();
        xAdjustment = parentLocation.x - e.getX();
        yAdjustment = parentLocation.y - e.getY();
        chessPiece = (PieceJLabel) c;
        chessPiece.setLocation(e.getX() + xAdjustment, e.getY() + yAdjustment);
        chessPiece.setSize(chessPiece.getWidth(), chessPiece.getHeight());
        layeredPane.add(chessPiece, JLayeredPane.DRAG_LAYER);
    }
 
    /**
     *  Move the piece
    */
    public void mouseDragged(MouseEvent me)
    {
    	if (!acceptInput) return;
        if (chessPiece == null) return;
        chessPiece.setLocation(me.getX() + xAdjustment, me.getY() + yAdjustment);
    }
 
    /*
    **  Drop the piece back
    */
    public void mouseReleased(MouseEvent e)
    {
    	if (!acceptInput) return;
    	// Only if inside board
        if (chessPiece == null) return;
 
        chessPiece.setVisible(false);
        Component c = chessBoard.findComponentAt(e.getX(), e.getY());
        if (c == null) c = originComponent;

        SquareJPanel parent;
        if (c instanceof PieceJLabel) {
            parent = (SquareJPanel) c.getParent();
            parent.remove(0);
            parent.add( chessPiece );
        } else {
            parent = (SquareJPanel) c;
            parent.add( chessPiece );
        }
        chessPiece.setVisible(true);
        
        // notifies move
        chess.userMove(flip ? originComponent.getIndex() : 63-originComponent.getIndex(), flip ? parent.getIndex() : 63-parent.getIndex());
    }
 
    public void mouseClicked(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    
	public void setFen(String fen, boolean flip, boolean redraw) {
		if (fen == null) return;
		this.flip = flip;
		lastFen = fen;
		int i = 0;
		int j = 0;
		while (i < fen.length()) {
			char p = fen.charAt(i++);
			if (p != '/') {
				int number = 0;
				try {
					number = Integer.parseInt(String.valueOf(p));
				} catch (Exception ignored) {}
				
				for (int k = 0; k < (number == 0 ? 1 : number); k++) {
					SquareJPanel panel = (SquareJPanel) chessBoard.getComponent(flip ? 63 - j++ : j++);
					try {
						PieceJLabel label = (PieceJLabel) panel.getComponent(0);
						if (label.getPiece() != p || redraw) {
							label.setVisible(false);
							panel.remove(0);
							throw new Exception();
						}
					} catch (Exception e) {
						if (number == 0) panel.add(new PieceJLabel(p));
					}
					if (j>=64) {
						return; // security 
					}
				}
			}
		}
	}
 	
	public void unhighlight() {
		for (int i = 0; i< 64; i++) ((SquareJPanel) chessBoard.getComponent(i)).setHighlighted(false); 
	}

	public void highlight(int from, int to) {
		SquareJPanel squareFrom = (SquareJPanel) chessBoard.getComponent(flip ? from : 63 - from);
		SquareJPanel squareTo = (SquareJPanel) chessBoard.getComponent(flip ? to : 63 - to);
		squareFrom.setHighlighted(true);
		squareTo.setHighlighted(true);
	}

	public String getLastFen() {
		return lastFen;
	}
}
