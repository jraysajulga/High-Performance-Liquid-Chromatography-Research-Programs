import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public class SixPortVisualizer extends JPanel{
	BufferedImage bufferOverlay;
	JLabel labelBackground;
	JLabel labelOverlay;
	simulation sim;
	
	public SixPortVisualizer(simulation sim, File picFile){
		this.sim=sim;
		
		setOpaque(false);
		setLayout(new OverlayLayout(this));
		
		BufferedImage background = null;
		try {
			background = ImageIO.read(picFile);
		} catch (IOException e){
		}
		labelBackground = new JLabel(new ImageIcon(background));
		
		bufferOverlay = new BufferedImage(background.getWidth(),background.getHeight(),BufferedImage.TYPE_INT_ARGB);
		labelOverlay = new JLabel(new ImageIcon(bufferOverlay));
		
		add(labelOverlay);
		add(labelBackground);
	}
	
	public void setBorder(int up, int left, int down, int right){
		Border border = labelBackground.getBorder();
		Border margin = new EmptyBorder(up, left, down, right);
		labelOverlay.setBorder(new CompoundBorder(border,margin));
		labelBackground.setBorder(new CompoundBorder(border,margin));
	}
	
	public void init(simulation sim){
		Color color = new Color(25,25,255,100);
	    int x;
	    int y;
	    double ACN = sim.sample;
	    int red = (int) Math.round(155*ACN/100) + 100;
	    Color redColor = new Color(red,25,25,125);
	    double rot = -Math.PI/6;
	    //double t = 3.8;
	    color = new Color(25,25,255,125);
	    double B;
	    for (double A = bufferOverlay.getWidth()*.28; A < bufferOverlay.getWidth()*.31; A=A+.5){	
	    	for (double t = 3.8; t > -1.2; t=t-.01){
	    			B = A * 0.8641975309;
	    			x = (int) Math.round(A * Math.cos(t) * Math.cos(rot) - B*Math.cos(t)*Math.sin(rot)+ bufferOverlay.getWidth()/2-bufferOverlay.getWidth()*.03);
	    			y = (int) Math.round(A * Math.cos(t) * Math.sin(rot) + B*Math.sin(t)*Math.cos(rot)  + bufferOverlay.getHeight()/2+bufferOverlay.getHeight()*.02);     
	    			if (t < 5 * sim.percFill/100 - 1.2){
	    				bufferOverlay.setRGB(x, y, color.getRGB());
	    			} else {
	    				bufferOverlay.setRGB(x, y, redColor.getRGB());
	    			}
	    			//t = t - .01;
	    		}
	    	//}
	    }
	    labelOverlay.setIcon(new ImageIcon(bufferOverlay));
	    
	    /*double A = bufferOverlay.getWidth()*.31;
	    double initialB = A * 0.8641975309;
	    for (double B = initialB; B > initialB*.9; B = B - .5){
	      for (double t = 3.8; t > -1.2; t=t-.01){
			x = (int) Math.round(A * Math.cos(t) * Math.cos(rot) - B*Math.cos(t)*Math.sin(rot)+ bufferOverlay.getWidth()/2-bufferOverlay.getWidth()*.03);
			y = (int) Math.round(A * Math.cos(t) * Math.sin(rot) + B*Math.sin(t)*Math.cos(rot)  + bufferOverlay.getHeight()/2+bufferOverlay.getHeight()*.02);     
			if (t < 5 * sim.percFill/100 - 1.2){
				bufferOverlay.setRGB(x, y, color.getRGB());
			} else {
				bufferOverlay.setRGB(x, y, redColor.getRGB());
			}
			//t = t - .01;
	      }
		}*/
	//}
	}
	
	public void update(simulation sim,int m){
		int x;
        int y;
        double initT = 5 * sim.percFill/100 -1.2;
        if (sim.percFill == 200){
        	//System.out.println("Not sure how this should work");
        	initT = 5 - 1.2;
        }
        double finalT = -1.2;
        double t;
        //double finalT = initT * .5;
        double ACN = sim.eluent;
        int red = (int) Math.round(155*ACN/100) + 100;
        double rot = -Math.PI/6;
        Color eluentColor = new Color(red,25,25,125);
        Color sampleColor = new Color(25,25,255,125);
        double B;
        for (double A = bufferOverlay.getWidth()*.28; A < bufferOverlay.getWidth()*.31; A=A+1){
        		for (t = initT; t > finalT; t=t-.01){
        			B = A * 0.8641975309;
        			x = (int) Math.round(A * Math.cos(t) * Math.cos(rot) - B*Math.cos(t)*Math.sin(rot)+ bufferOverlay.getWidth()/2-bufferOverlay.getWidth()*.03);
        			y = (int) Math.round(A * Math.cos(t) * Math.sin(rot) + B*Math.sin(t)*Math.cos(rot)  + bufferOverlay.getHeight()/2+bufferOverlay.getHeight()*.02);
        			if (t <= m/sim.NinputT_loop * (finalT - initT) + initT){
        				bufferOverlay.setRGB(x, y, sampleColor.getRGB());
        			} else {
        				bufferOverlay.setRGB(x, y, eluentColor.getRGB());
        			}
        		}
        }
        labelOverlay.setIcon(new ImageIcon(bufferOverlay));
	}
}