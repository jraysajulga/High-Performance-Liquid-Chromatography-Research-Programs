import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ColumnVisualizer extends SixPortVisualizer{
	
	public ColumnVisualizer(simulation sim, File filename) {
		super(sim, filename);
		init();
	}
	
	public void init(){
		Color blue;
		int alpha;
		double analyte;
		double ACN;
		int red;
		int initialValue = (int) Math.round(bufferOverlay.getWidth()*.035);
		for (int x = 0; x < bufferOverlay.getWidth(); x++){
			ACN = sim.eluent;
			red = (int) Math.round(155*ACN/100) + 100;
			Color color = new Color(red,25,25,100);
			for (int y = (int) Math.round(bufferOverlay.getHeight()*.2); y < bufferOverlay.getHeight()*.8;y++){
				if (-10*Math.sqrt(x)+bufferOverlay.getHeight()/2 <= y && y<=10*Math.sqrt(x)+bufferOverlay.getHeight()/2){
					if (-10*Math.sqrt(bufferOverlay.getWidth()-2*initialValue-x)+bufferOverlay.getHeight()/2 <= y && y<=10*Math.sqrt(bufferOverlay.getWidth()-2*initialValue-x)+bufferOverlay.getHeight()/2){
						bufferOverlay.setRGB((int) Math.round(x*(bufferOverlay.getWidth()-2*initialValue)/bufferOverlay.getWidth() + initialValue), y, color.getRGB());
					}
				}
			}
		}
        //labelOverlay.setIcon(new ImageIcon(bufferOverlay));
	}
	
	public void update(simulation sim, int m){
		Color blue;
		int alpha;
		double analyte;
		double ACN;
		int red;
		int initialValue = (int) Math.round(bufferOverlay.getWidth()*.035);
		for (int x = 0; x < bufferOverlay.getWidth(); x++){
			analyte= sim.get(m)[0].getYValue(0,(int) Math.round(sim.zsteps*x/bufferOverlay.getWidth()));
			ACN = sim.get(m)[1].getYValue(0,(int) Math.round(sim.zsteps*x/bufferOverlay.getWidth()));
			alpha = (int) Math.round(255*Math.sqrt(analyte/sim.absoluteMax));
			red = (int) Math.round(155*ACN/100) + 100;
			Color color = new Color((int) Math.round(red-alpha*.1),25,alpha,(int) Math.round(100+alpha*155/255));
			for (int y = (int) Math.round(bufferOverlay.getHeight()*.2); y < bufferOverlay.getHeight()*.8;y++){
				if (-10*Math.sqrt(x)+bufferOverlay.getHeight()/2 <= y && y<=10*Math.sqrt(x)+bufferOverlay.getHeight()/2){
					if (-10*Math.sqrt(bufferOverlay.getWidth()-2*initialValue-x)+bufferOverlay.getHeight()/2 <= y && y<=10*Math.sqrt(bufferOverlay.getWidth()-2*initialValue-x)+bufferOverlay.getHeight()/2){
						bufferOverlay.setRGB((int) Math.round(x*(bufferOverlay.getWidth()-2*initialValue)/bufferOverlay.getWidth() + initialValue), y, color.getRGB());
					}
				}
			}
		}
        labelOverlay.setIcon(new ImageIcon(bufferOverlay));
	}
	
}