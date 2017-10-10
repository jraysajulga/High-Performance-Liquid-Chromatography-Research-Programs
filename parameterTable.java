import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

@SuppressWarnings("serial")
public class parameterTable extends JPanel {
	String compound;
	double C;
	int N;
	double Tm;
	double tmax;
	double length;
	double flow;
	double sample;
	double eluent;
	double tg;
	double Gi;
	double Gf;
	double td;
	int paraNum;
	String loopSize;
	String percFill;
	JTable table;
	JTable resultsTable;
	JTable resultsinputTable;
	JTable inputTable;
	JTableHeader header;
	int selectedColumn;
	Color color;
	int[][] colorMap;
	int[] fontParameterMap;
	int[][] fontMap;
	int noOfColumns;
	int[][] colorKey = {{255, 255, 255},{66, 244, 244},{241,244,66},{66, 244, 75},{255, 0, 0}};
	int simulationNum = 0;
	
	
	public parameterTable(String compound, double C, int N, double Tm, double tmax, double length, double flow, double sample, double eluent, double tg, double Gi, double Gf, double td, String loopSize, String percFill){
		this.compound = compound;
		this.C = C;
		this.N = N;
		this.Tm = Tm;
		this.tmax = tmax;
		this.length = length;
		this.flow = flow;
		this.sample = sample;
		this.eluent = eluent;
		this.tg = tg;
		this.Gi = Gi;
		this.Gf = Gf;
		this.td = td;
		this.loopSize = loopSize;
		this.percFill = percFill;
		createTable();
		createInputTable();
		mirrorHistory();
		createHeader();
		resultsTable.setValueAt("",7,0);
		
		paraNum = 15;
		noOfColumns = 9;
		colorMap = new int[paraNum][noOfColumns];
		fontParameterMap = new int[paraNum];
		fontMap = new int[paraNum][noOfColumns];
		fillColumn(0,1);
		
		
		JPanel parameterPanel = new JPanel(new GridBagLayout());
	    parameterPanel.setMinimumSize(new Dimension(200,200));
	    
	    int height1 = 280;
	    int height2 = 150;
	    int width1 = 175; 
	    int width2 = 110;
	    
	    // OVERVIEW LAYOUT
	    //  inputTableSP     tableSP
	    //  resultsHeaderSP   resultsSP
	    //
	    
	    GridBagConstraints c = new GridBagConstraints();
	    c.fill=GridBagConstraints.BOTH;
	    c.insets = new Insets(0,1,0,1); //up left down right
	    c.gridx = 1;
	    c.gridy = 0;
	    //add(table.getTableHeader(),c);
	    c.weightx = 0.4;
	    c.gridx = 0;
	    c.gridy = 1;
	    c.anchor = GridBagConstraints.WEST;
	    inputTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    	inputTable.getColumnModel().getColumn(0).setPreferredWidth(105);
	    	inputTable.getColumnModel().getColumn(1).setPreferredWidth(65);
	    	inputTable.getTableHeader().setReorderingAllowed(false);
	    JScrollPane inputTableSP = new JScrollPane(inputTable);
	    	inputTableSP.setPreferredSize(new Dimension(width1,height1));
	    add(inputTableSP,c );
	   
			
	    c.weightx = .6;
	    c.gridx = 1;
	    c.gridy = 1;
	    c.anchor = GridBagConstraints.EAST;
	    JScrollPane tableSP = new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tableSP.setPreferredSize(new Dimension(width2,height1));
	    add(tableSP, c);
	    
	    resultsinputTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    resultsinputTable.getColumnModel().getColumn(0).setPreferredWidth(105);
	    resultsinputTable.getColumnModel().getColumn(1).setPreferredWidth(65);
	    resultsinputTable.getTableHeader().setReorderingAllowed(false);
	    resultsinputTable.setDefaultRenderer(Object.class,
	    		new DefaultTableCellRenderer() {
			Color originalColor = null;
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Put red font for changed values
				if (fontParameterMap[row] == 1){
					renderer.setForeground(Color.RED);
					System.out.println(row);
				} else {
					renderer.setForeground(Color.BLACK);
				}
				if (column == 0){
					renderer.setBackground(new Color(238,238,238));
					renderer.setForeground(Color.BLACK);
					renderer.setBorder(BorderFactory.createRaisedBevelBorder());
				} else {
    				renderer.setBackground(Color.WHITE);
    				int R = colorKey[colorMap[row][selectedColumn]][0];
    				int G = colorKey[colorMap[row][selectedColumn]][1];
    				int B = colorKey[colorMap[row][selectedColumn]][2];
    				color = new Color(255-(255 - R)/3,255-(255 - G)/3, 255-(255 - B)/3);
    				renderer.setBorder(BorderFactory.createDashedBorder(color,2,2));
    				//renderer.setBackground(color);
				}
				
				// Highlights rows
				if (isSelected){
					if (column == 0){
						renderer.setBackground(new Color(238,238,238));
						renderer.setForeground(new Color(230,150,50));
						renderer.setBorder(BorderFactory.createLoweredBevelBorder());
					} else {
						renderer.setBackground(Color.ORANGE);
					}
				}
				
				return renderer;
			}
		});
	    
	    JScrollPane resultsHeaderSP = new JScrollPane(resultsinputTable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    resultsHeaderSP.setPreferredSize(new Dimension(width1,height2));
	    c.gridx = 0;
	    c.gridy = 1;
	    c.weighty = .2;
	    add(resultsHeaderSP, c); 
	    
	    //BoundedRangeModel model = resultsHeaderSP.getHorizontalScrollBar().getModel();
	    //inputTableSP.getHorizontalScrollBar().setModel( model );
	    
	    JScrollPane resultsSP = new JScrollPane(resultsTable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    resultsSP.setPreferredSize(new Dimension(width2,height2));
	    c.gridx = 1;
	    add(resultsSP, c);
	    setColumnWidth(45);
	    
	    // Create an options box
	    Box optionsBox = new Box(BoxLayout.X_AXIS);
	    	// Dispersion button
	    	JCheckBox hasDispersion = new JCheckBox("Dispersion");
	    	hasDispersion.setSelected(true);
	    	// Save button
	    	JButton saveButton = new JButton("Save");
	    	
	    optionsBox.add(saveButton);
	    optionsBox.add(hasDispersion);
	    
	    // Save button listener
	    //saveButton.addActionListener(l);
	    
	    //add(optionsBox);
	    
	    tableSP.getHorizontalScrollBar().setModel(
	              resultsSP.getHorizontalScrollBar().getModel());
	    resultsSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    
	    setMinimumSize(new Dimension(500,1400));
	    setBorder(BorderFactory.createCompoundBorder(
	    		BorderFactory.createTitledBorder("              Input                                     History"),
	    		BorderFactory.createBevelBorder(2)));
	    
	}
	
	public Object[] extractParameters(){
		int num = 15; // Number of parameters
		Object[] parameters = new Object[num];
		
		// Iterates through each parameter and aggregates
		// them into a returned array
		parameters[0] = (String) getValueAt(0); // Compound name
		for (int i = 1; i < num; i++){
			parameters[i] = processValueAt(i);
		}
		return parameters;
	}
	
	public void mirrorHistory(){
		int num = 15;
		for (int i = 0; i < num; i++){
			table.setValueAt(inputTable.getValueAt(i, 1), i, simulationNum);		
		}
	}
	
	public void createTable(){
		Object[] columnNames = {"A","B","C","D","E","F","G","H","I"};
		noOfColumns = columnNames.length;
		Object[][] data = blankTable(15,noOfColumns);
	    
	    table = new JTable(data, columnNames){
	    	@Override
	    	public boolean isCellEditable(int row, int col){
	    		return false;
	    	}
	    };
	    table.setColumnSelectionAllowed(false);
	    table.setRowSelectionAllowed(false);
	    table.addKeyListener(new ClipboardKeyAdapter(table));
	    table.setDefaultRenderer(Object.class,
	    		new DefaultTableCellRenderer() {
	    			Color originalColor = null;
	    			@Override
	    			public Component getTableCellRendererComponent(JTable table,
	    					Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    				DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	    				if (originalColor == null) {
	    					originalColor = getForeground();
	    				}
	    				if (value == null) {
	    					renderer.setText("0");
	    				} else {
	    					renderer.setText(value.toString());
	    				}
	    				// Highlights the appropriate column
	    				if (column == selectedColumn){
	    					renderer.setBackground(originalColor);
    						renderer.setBorder(BorderFactory.createEtchedBorder());
    					}
	    				
	    				// 0 = white
	    				// 1 = blue for next calculation
	    				// 2 = yellow for impending calc
	    				// 3 = green for successful calculation
	    				// 4 = red for early termination
	    				
	    				// Set cell color
	    				Color color;
	    				int R = colorKey[colorMap[row][column]][0];
	    				int G = colorKey[colorMap[row][column]][1];
	    				int B = colorKey[colorMap[row][column]][2];
	    				color = new Color(R,G,B);
	    				renderer.setBackground(color);
	    				
	    				// Put red font for changed values
	    				if (fontMap[row][column] == 1){
	    					//renderer.setForeground(new Color(62, 122, 34));
	    					renderer.setFont(new Font("SansSerif", Font.ITALIC,12));
	    					renderer.setForeground(Color.DARK_GRAY);
	    				} else {
	    					//renderer.setForeground(Color.BLACK);
	    					renderer.setFont(new Font("SansSerif", Font.PLAIN,12));
	    					renderer.setForeground(Color.BLACK);
	    				}
	    				
	    				return renderer;
	    			}
	    		});
	    // Blue
	    color = new Color(66,215,244);
	    Object[] columnNameBlanks = new Object[noOfColumns];
	    for (int i = 0; i < noOfColumns; i++){
	    	columnNameBlanks[i] = " ";
	    }
	   //Object[] columnNameBlanks = {" "," "," "," "," "};
	    Object[][] blankData = blankTable(8,columnNameBlanks.length);
	    blankData[7][0] = new JRadioButton("B");
	    
	    resultsTable = new JTable(blankData,columnNameBlanks){
	    	@Override
	    	public boolean isCellEditable(int row, int col){
	    		return false;
	    	}
	    };
	    
	    resultsTable.setColumnSelectionAllowed(false);
	    resultsTable.setRowSelectionAllowed(false);
	    resultsTable.addKeyListener(new ClipboardKeyAdapter(table));

	    resultsTable.setDefaultRenderer(Object.class,
	    		new DefaultTableCellRenderer() {
	    			Color originalColor = null;
	    			@Override
	    			public Component getTableCellRendererComponent(JTable table,
	    					Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    				DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	    				if (originalColor == null) {
	    					originalColor = getForeground();
	    				}
	    				if (value == null) {
	    					renderer.setText("0");
	    				} else {
	    					renderer.setText(value.toString());
	    				}
	    				// Highlights the appropriate column
	    				if (column == selectedColumn){
	    					renderer.setBackground(originalColor);
    						renderer.setBorder(BorderFactory.createEtchedBorder());
    					}
	    				
	    				// 0 = white
	    				// 1 = blue for next calculation
	    				// 2 = yellow for impending calc
	    				// 3 = green for successful calculation
	    				// 4 = red for early termination
	    				
	    				Color color;
	    				int R = colorKey[colorMap[0][column]][0];
	    				int G = colorKey[colorMap[0][column]][1];
	    				int B = colorKey[colorMap[0][column]][2];
	    				color = new Color(R,G,B);
	    				renderer.setBackground(color);
	    				return renderer;
	    			}
	    		});
	    }
	
	public void setColumnWidth(int width){
		int columns = table.getColumnCount();
		TableColumn column = null;
		TableColumn column2 = null;
		for (int i = 0; i < columns; i++){
			column = table.getColumnModel().getColumn(i);
			column2 = resultsTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(width);
			column2.setPreferredWidth(width);
		}
	}
	
	public Object[][] blankTable(int rows, int columns){
		Object[][] data = new Object[rows][columns];
		for (int x = 0; x < rows; x++){
			for (int y = 0; y < columns; y++){
				data[x][y] = "";
			}
		}
		return data;
	}
	
	public int firstBlankColumn(){
		int i = 0;
		while (!columnIsEmpty(i)){
			i++;
		}
		return i;
	}
	
	public boolean columnIsEmpty(int column){
		boolean isEmpty = true;
		for (int j = 0; j < table.getRowCount(); j++){
			if (table.getValueAt(j, column) != ""){
				isEmpty = false;
			}
		}
		return isEmpty;
	}
	
	
	public void setResults(int column, double[] results){
		for (int i = 0; i < results.length; i++){
			resultsinputTable.setValueAt(results[i], i, 1);
			resultsTable.setValueAt(results[i], i, column);
		}
	}
	
	public void isProcessing(int column){
		fillColumn(column,2);
	}
	
	public void failed(int column){
		fillColumn(column, 4);
	}
	
	public void fillColumn(int column, int value){
		for (int i = 0; i < paraNum; i++){
			colorMap[i][column] = value;
		}
		if (value == 1){
			simulationNum = column;
		}
	}
	
	public void changeFontColor(int row, int column){
		fontParameterMap[row] = 1;
	}
	
	public void changeFontColor(int row){
		//fontParameterMap[row][1] = 1;
		// DO THIS
	}
	
	public void printColorMap(){
		int x = colorMap.length;
		int y = colorMap[0].length;
		String print = "";
		for (int i = 0; i < x; i++){
			for (int j = 0; j < y; j++){
				print = print + colorMap[i][j] + " ";
			}
			print = print + "\n";
		}
		System.out.println("Color Map: ");
		System.out.println(print);
	}
	
	
	public void setColor(Color color){
		this.color = color;
	}
	
	public String getLetter(){
		System.out.println((String) table.getColumnModel().getColumn(selectedColumn).getHeaderValue());
		return (String) table.getColumnModel().getColumn(selectedColumn).getHeaderValue();
	}
	
	public int tick(){
		selectedColumn++;
		changeHeader();
		refreshFonts();
		return selectedColumn;
	}
	
	public void tick(int column){
		selectedColumn = column;
		changeHeader();
		refreshFonts();
	}
	
	
	public void setValueAt(Object aValue, int row){
		fontParameterMap[row] = 1;
		inputTable.setValueAt(aValue, row, 1);
		changeFontColor(row);
	}
	
	public void setValueAt(Object aValue, int row, int column){
		table.setValueAt(aValue, row, column);
		changeFontColor(row, column);
	}
	
	public Object getValueAt(int row){
		Object value = inputTable.getValueAt(row, 1);
		inputTable.setValueAt(value, row, 1);
		return value;
	}
	
	public Object getValueAt(int row, int column){
		Object value = table.getValueAt(row,  column);
		table.setValueAt(value, row, column);
		return value;
	}
	
	public void transferValuesToTable(int column){
		for (int i = 0; i < table.getRowCount(); i++){
			table.setValueAt(inputTable.getValueAt(i, 1),i,column);
		}
	}
	
	public void refresh(){
		header.repaint();
		table.repaint();
		inputTable.repaint();
		resultsinputTable.repaint();
		resultsTable.repaint();
	}
	
	public double processValueAt(int index){
		double var;
			if (getValueAt(index).getClass() == Integer.class){
				var = (Integer) getValueAt(index);
			} else if (getValueAt(index).getClass() == Double.class){
				var = (Double) getValueAt(index);
			} else {
				String rawVar = (String) getValueAt(index);
				var = Double.parseDouble(rawVar);
			}
		return var;
	}
	
	public void transferValuesFromTable(int column){
		for (int i = 0; i < table.getRowCount(); i++){
			inputTable.setValueAt(table.getValueAt(i, column),i,1);
		}
		for (int i = 0; i < resultsTable.getRowCount()-1; i++){
			resultsinputTable.setValueAt(resultsTable.getValueAt(i, column),i,1);
		}
	}
	
	public void createHeader(){
		header = table.getTableHeader();
		header.setReorderingAllowed(false);
		/*header.addMouseListener(new MouseAdapter() {
	    	@Override
	    	public void mouseClicked(MouseEvent mouseEvent) {
	    		selectedColumn = table.columnAtPoint(mouseEvent.getPoint());
	    		changeHeader();
	    		resultsTable.updateUI();
	    		if (!columnIsEmpty(selectedColumn)){
	    			transferValuesFromTable(selectedColumn);
	    		}
	    	}
	    });*/
		
		header.setDefaultRenderer(new DefaultTableCellRenderer() {
			Color originalColor = null;
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				
				renderer.setForeground(Color.WHITE);
				renderer.setBackground(Color.GRAY);
				if (column == selectedColumn){
					renderer.setBorder(BorderFactory.createLoweredBevelBorder());
					renderer.setForeground(Color.ORANGE);
				} else {
					renderer.setBorder(BorderFactory.createRaisedBevelBorder());
				}
				return renderer;
			}
		});
	}
	
	public void createInputTable(){
		Object[] rowLabels={"Parameters","A"};   
	    Object[][] filler = {
	    		{"Compound",compound},
	    		{"Concentration",C},
	    		{"Plate Number",N},
	    		{"Dead Time (min)",Tm},
	   			{"Max time (min)",tmax},
	   			{"Length (cm)",length},
	  			{"Flow (mL/min)",flow},
	  			{"Sample %B",sample},
	  			{"Mobile Phase %B",eluent},
	  			{"Gradient (min)",tg},
	  			{"Phi, init",Gi},
	  			{"Phi, final",Gf},
	  			{"Delay time (min)",td},
	  			{"Loop Size (µL)",loopSize},
	  			{"Percent Fill (%)",percFill}};
	    inputTable = new JTable(filler,rowLabels){
	    	@Override
	    	public boolean isCellEditable(int row, int col){
	    		return col == 1;
	    	}
	    };
	    inputTable.setDefaultRenderer(Object.class,
	    		new DefaultTableCellRenderer() {
			Color originalColor = null;
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	
				// Put red font for changed values
				if (fontParameterMap[row] == 1){
					renderer.setForeground(Color.RED);
					System.out.println(row);
				} else {
					renderer.setForeground(Color.BLACK);
				}
				if (column == 0){
					renderer.setBackground(new Color(238,238,238));
					renderer.setForeground(Color.BLACK);
					renderer.setBorder(BorderFactory.createRaisedBevelBorder());
				} else {
    				renderer.setBackground(Color.WHITE);
    				int R = colorKey[colorMap[row][selectedColumn]][0];
    				int G = colorKey[colorMap[row][selectedColumn]][1];
    				int B = colorKey[colorMap[row][selectedColumn]][2];
    				color = new Color(255-(255 - R)/3,255-(255 - G)/3, 255-(255 - B)/3);
    				renderer.setBorder(BorderFactory.createDashedBorder(color,4,1));
    				renderer.setBorder(BorderFactory.createEtchedBorder());
    				//renderer.setBackground(color);
				}
				
				// Highlights rows
				if (isSelected){
					if (column == 0){
						renderer.setBackground(new Color(238,238,238));
						renderer.setForeground(new Color(230,150,50));
						renderer.setBorder(BorderFactory.createLoweredBevelBorder());
					} else {
						renderer.setBackground(Color.ORANGE);
					}
				}
				
				return renderer;
			}
		});
	    
	    // inputTable's header
	    JTableHeader inputHeader = inputTable.getTableHeader();
	    inputHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
	    	Color originalColor = null;
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				
				renderer.setForeground(Color.WHITE);
				renderer.setBackground(Color.GRAY);
				if (column == 1){
					Color color;
    				int R = colorKey[colorMap[0][selectedColumn]][0];
    				int G = colorKey[colorMap[0][selectedColumn]][1];
    				int B = colorKey[colorMap[0][selectedColumn]][2];
    				color = new Color(R,G,B);
    				//renderer.setBackground(color);
					renderer.setForeground(color.ORANGE);
					renderer.setBorder(BorderFactory.createLoweredBevelBorder());
				} else {
					renderer.setBorder(BorderFactory.createRaisedBevelBorder());
				}
				return renderer;
			}
	    });
	    	    
	    
	    Object[] rowLabels2={"Results"," "};   
	    Object[][] filler2 = {
	    		{"Retention Time"," "},
	    		{"Width at Half Height"," "},
	  			{"Width at 4.4% Height"," "},
	  			{"Area "," "},
	  			{"Sum ",""},
	  			{"Height ",""},
	  			{"K final ",""},
	  			{"Runtime (seconds)"," "}};
	    resultsinputTable = new JTable(filler2,rowLabels2){
	    	@Override
	    	public boolean isCellEditable(int row, int col){
	    		return false;
	    	}
	    };
	}
	
	// Changes the header of the inputheader to the selected column
	public void changeHeader(){
		String value = (String) table.getTableHeader().getColumnModel().getColumn(selectedColumn).getHeaderValue();
		JTableHeader th = inputTable.getTableHeader();
		TableColumnModel tcm = th.getColumnModel();
		TableColumn tc = tcm.getColumn(1);
		tc.setHeaderValue(value);
		th.repaint();
	}
	
	public void refreshFonts(){
		for (int i = 0; i < paraNum; i++){
			fontParameterMap[i] = 0;
		}
		inputTable.repaint();
	}
	
	public void historyFonts(){
		for (int i = 0; i < paraNum; i++){
			fontMap[i][selectedColumn] = fontParameterMap[i];
		}
		System.out.println();
		table.repaint();
	}
}