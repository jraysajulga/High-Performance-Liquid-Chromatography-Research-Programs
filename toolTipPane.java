import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

public class toolTipPane extends JPanel{
	JTextArea toolTip;
	
	public toolTipPane(){
		setBackground(new Color(255,255,255));
		toolTip = new JTextArea();
		toolTip.setWrapStyleWord(true);
		//toolTip.setLineWrap(true);
		//toolTip.setEditable(false);
		toolTip.setMinimumSize(new Dimension(1000,10));
		toolTip.setMaximumSize(new Dimension(1000,10));
		//toolTip.setBackground(new Color(24,24,24));
		Font font = new Font("SansSerif", Font.ITALIC,12);
		toolTip.setFont(font);
		add(toolTip);
	}
	
	public void hovered(String message){
		toolTip.setText(message);
	}
	
	public void blank(){
		toolTip.setText("");
	}
	
	public void setText(String text){
		toolTip.setText(text);
	}
}