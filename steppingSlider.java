import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class steppingSlider extends JSlider{
		int[] VALUES;
		public steppingSlider(int values[], int division, int max, String suffix){
			super(0,max,values[0]);
			VALUES = new int[values.length];
			
			for (int i = 0; i < values.length; i++){
				VALUES[i] = values[i];
			}
			setPaintTicks(true);
			setPaintLabels(true);
			setSnapToTicks(true);
			setMajorTickSpacing(division);
			setOrientation(SwingConstants.VERTICAL);
			
			// Snaps to specific values as a result of this listener:
			/*addChangeListener(new ChangeListener(){
				@Override
				public void stateChanged(ChangeEvent e) {
					//System.out.println(getValue());
					//System.out.println(closestValue(getValue()));
					setValue(closestValue(getValue()));
				}
			});*/
			
			// Create the label table
			Hashtable labelTable = new Hashtable();
			JLabel label;
			int j = 0;
			for (int i = 0; i <= max; i = i + division){
				label = new JLabel(Integer.toString(i));
				label.setFont(new Font("TimesRoman",Font.PLAIN,10));
				if (i != VALUES[j]){
					label.setForeground(new Color(200,200,200));
				} else {
					j++;
				}
				//System.out.println(label.getName());
				labelTable.put(i, label);
			}
			setLabelTable(labelTable);
		}
		
		public int closestValue(double value){
			int index = 0;
			double diff = Math.abs(VALUES[0] - value);
			for (int i = 1; i < VALUES.length; i++){
				if (diff > Math.abs(VALUES[i] - value)){
					index = i;
					diff = Math.abs(VALUES[i] - value);
				}
				
			}
			return VALUES[index];
		}
		
		public void snapToValue(){
			setValue(closestValue(getValue()));
		}
		
}