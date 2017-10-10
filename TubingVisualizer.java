import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class TubingVisualizer extends SixPortVisualizer{
	
	public TubingVisualizer(simulation sim, File filename, double ratioX, double ratioY) {
		super(sim, filename);
		init();
	}
	
	public void init(){
		Color blue;
		int alpha;
		double analyte;
		double ACN;
		int red;
		Color color;
		int concavity = 3;
		Boolean leftCap;
		Boolean rightCap;
		int initialValue = (int) Math.round(bufferOverlay.getWidth()*.012);
    	for (int x = 0; x < bufferOverlay.getWidth(); x++){
    		ACN = sim.eluent;
    		red = (int) Math.round(155*ACN/100) + 100;
			color = new Color(red,25,25,100);
			for (int y = (int) Math.round(bufferOverlay.getHeight()*.2); y < bufferOverlay.getHeight()*.8;y++){
    			leftCap = -concavity*Math.sqrt(x)+bufferOverlay.getHeight()/2 <= y && y<=concavity*Math.sqrt(x)+bufferOverlay.getHeight()/2;
    			rightCap = -concavity*Math.sqrt(-x + bufferOverlay.getWidth()-1*initialValue)+bufferOverlay.getHeight()/2 <= y && y<=concavity*Math.sqrt(-x + bufferOverlay.getWidth())+bufferOverlay.getHeight()/2;
    			if (leftCap && rightCap){
    				bufferOverlay.setRGB((int) Math.round(x*(bufferOverlay.getWidth()-initialValue)/bufferOverlay.getWidth() + initialValue), y, color.getRGB());
    			} 
    		}
           }
        labelOverlay.setIcon(new ImageIcon(bufferOverlay));
	}
	
	public void update(simulation sim, int m, boolean isLast){
		Color blue;
		int alpha;
		double analyte;
		double ACN;
		int red;
		int initialValue = (int) Math.round(bufferOverlay.getWidth()*.035);
		Color color;
		Color blank;
		int concavity = 2;
		Boolean leftCap;
		Boolean rightCap;
		initialValue = (int) Math.round(bufferOverlay.getWidth()*.012);
		int index = 0;
		if (isLast){
			index = sim.zsteps-1;
		}
    	for (int x = 0; x < bufferOverlay.getWidth(); x++){
    		ACN = sim.get(m)[1].getYValue(0,index);
    		analyte= sim.get(m)[0].getYValue(0,index);
    		alpha = (int) Math.round(255*Math.sqrt(analyte/sim.absoluteMax));
    		red = (int) Math.round(155*ACN/100) + 100;
			color = new Color((int) Math.round(red-alpha*.1),25,alpha,(int) Math.round(100+alpha*155/255));
    		for (int y = (int) Math.round(bufferOverlay.getHeight()*.2); y < bufferOverlay.getHeight()*.8;y++){
    			leftCap = -concavity*Math.sqrt(x)+bufferOverlay.getHeight()/2 <= y && y<=concavity*Math.sqrt(x)+bufferOverlay.getHeight()/2;
    			rightCap = -concavity*Math.sqrt(-x + bufferOverlay.getWidth()-1*initialValue)+bufferOverlay.getHeight()/2 <= y && y<=concavity*Math.sqrt(-x + bufferOverlay.getWidth()-2*initialValue)+bufferOverlay.getHeight()/2;
    			if (leftCap && rightCap){
    				bufferOverlay.setRGB((int) Math.round(x*(bufferOverlay.getWidth()-initialValue)/bufferOverlay.getWidth() + initialValue), y, color.getRGB());
    			} 
    		}
           }
        labelOverlay.setIcon(new ImageIcon(bufferOverlay));
	}
}