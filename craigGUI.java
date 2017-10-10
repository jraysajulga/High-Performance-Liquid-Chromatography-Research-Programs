import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
//import java.util.Date;
//import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
//import java.util.Vector;

import javax.imageio.ImageIO;

//import javax.imageio.ImageIO;

//import simulator.craig4.simulationThread;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
//import javax.swing.event.TableModelEvent;
//import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;


public class craigGUI {
	private static JTable table;
	private static JComboBox<String> compoundS;
	private static JComboBox<String> loopSizeS;
	private static JComboBox<String> percFillS;
	private static boolean timerOn = false;
	private static JSlider simulationSlider;
	private static JProgressBar progressBar;
	private static XYPlot profilePlot;
	private static XYPlot analyteDistancePlot;
	private static XYPlot analyteOutletPlot;
	private static XYPlot eluentDistancePlot;
	private static XYPlot eluentInletPlot;
	private static XYPlot eluentOutletPlot;
	private static JButton bPlay;
	private static JTextPane textPane;
	private static JButton start;
	private static IntervalMarker marker;
	private static IntervalMarker analyteDistanceMarker;
	private static IntervalMarker eluentDistanceMarker;
	private static IntervalMarker analyteYMarkerOut;
	private static IntervalMarker analyteYMarkerIn;
	private static IntervalMarker eluentYMarkerOut;
	private static IntervalMarker eluentYMarkerIn;
	private static IntervalMarker eluentYMarker;
	private static IntervalMarker profileYMarker;
	private static Timer timer;
	private static Timer runTimer;
	private static double seconds;
	private volatile static boolean running;
	private static ImageIcon pauseIcon;
	private static ImageIcon playIcon;
	private static int frameInterval;
	//private static ArrayList<simulation> history;
	private static JList<String> historyS;
	private static DefaultListModel<String> listModel;
	private static JScrollPane historyScrollPane;
	private static ArrayList<String[]> resultParameterHistory;
	//private static ArrayList<XYDataset[]> datasetHistory;
	private static ArrayList<List<XYDataset>> eluentDistanceGradientDatasets;
	private static ArrayList<List<XYDataset>> analyteDistanceGradientDatasets;
	private static ArrayList<XYDataset> analyteOutletGradientDatasets;
	private static ArrayList<XYDataset> eluentInletGradientDatasets;
	private static JButton previous_sim;
	private static JButton next_sim;
	private static JButton clear_sim;
	private static JButton load;
	private static double[] profile;
	private static profiles injProfiles;
	private static int historyIndex;
	private static double dt;
	private static double dz;
	private static double tmax;
	private static int N;
	private static int zsteps;
	private static Boolean initialRun;
	private static simulation sim;
	private static double timeMult;
	private static XYDataset analyteDistanceDataset;
	private static XYDataset eluentDistanceDataset;
	private static XYTextAnnotation analyteDistanceInLabel;
	private static XYTextAnnotation analyteDistanceOutLabel;
	private static XYTextAnnotation eluentDistanceInLabel;
	private static XYTextAnnotation eluentDistanceOutLabel;
	private static XYTextAnnotation analyteOutletLabel;
	private static XYTextAnnotation eluentOutletLabel;
	private static XYTextAnnotation eluentInletLabel;
	private static XYTextAnnotation profileLabel;
	private static NumberAxis eluentDistanceyaxis; 
	private static NumberAxis analyteYaxis;
	private static NumberAxis eluentYaxis;
	private static NumberAxis chromatogramYaxis;
	private static NumberAxis timeAxis;
	private static NumberAxis lengthAxis;
	private static BufferedImage column;
	private static JLabel picLabel;
	private static BufferedImage loop;
	private static JLabel sixportLabel;
	private static JLabel delayLabel;
	private static BufferedImage pumpDelayTubing;
	private static RangeSlider gradientXSlider;
	private static RangeSlider gradientYSlider;
	private static steppingSlider loopVolumeSlider;
	private static steppingSlider percFillSlider;
	private static JSlider eluentSlider;
	private static JSlider analyteSlider;
	private static SixPortVisualizer spv;
	private static TubingVisualizer preColumnTubing;
	private static TubingVisualizer postColumnTubing;
	private static ColumnVisualizer cv;
	private static int m = 1;
	private static int n = 0;
	private static int selected=0;
	private static parameterTable parameterPane;
	private static int simulationNum = 0;
	private static toolTipPane toolTipPane;
	private static SimulationRack simulationRack;
	private static int simulationSliderValue;
	private static double time;
	private static Boolean isChanging = false;
	private static Boolean failed = false;
	
// Creates and shows GUI
@SuppressWarnings({ "deprecation", "serial" })
public static void createAndShowGUI(){
	
	// Creates the main frame
	JFrame simulatorFrame = new JFrame();
	GradientPanel gradientFrame= new GradientPanel();
	simulatorFrame.setPreferredSize(new Dimension(1200,630));
	simulatorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	simulatorFrame.setResizable(false);
	
	// Chemical compound dropdown menu
	compoundS = new JComboBox<>(new String[] {"DiEthF","NA3","NA4","NA5","BzAlc","PB1","PB2","PB3","PB4","AP2","AP4","AP5","AP6","AP7","AP8"});

	// Simulation management buttons
	JSplitPane clear_buttons_jSplP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	JButton clear_one_button_jSplP = new JButton("Clear");
	JButton clear_all_button_jSplP = new JButton("Clear all");
	clear_buttons_jSplP.setLeftComponent(clear_one_button_jSplP);
	clear_buttons_jSplP.setRightComponent(clear_all_button_jSplP);
	
    // Creates Box that contains inputs
    start = new JButton("Calculate");
    progressBar = new JProgressBar(0,100);
    progressBar.setValue(0);
    progressBar.setStringPainted(true);
    
    // Default value parameters
    double C = 2.5;
    N = 100;
    double Tm = 0.022;
    tmax = .5;
    double length = 3; 
    double flow = 2.5;
    double sample = 30;
    double eluent = 30;
    double tg = .25;
    double Gi = 30;
    double Gf = 65;
    double td = .027;
    String loopSize = "15";
    String percFill = "25";
    dt = Tm/N;
    
    // Initializes profile based on default values listed above
    injProfiles = new profiles(3,"data/profile.csv");
	profile = injProfiles.profileExtraction("15", "25",N,Tm,tmax);
	
	// Extracts neueKuss parameters
    String compound = "DiEthF";
    profiles neueKuss = new profiles(1,"data/neueKuss.csv");
    double[] NKParameters = neueKuss.profileMap.get(compound);

    // Constructs a simulation for ACN profile generation...
    parameterPane = new parameterTable(compound,C,N,Tm,tmax,length,flow,sample,eluent,tg,Gi,Gf,td,loopSize,percFill);
    Object[] parameters = parameterPane.extractParameters();
    sim = new simulation(compound,NKParameters,parameters,profile);
    double[] ACNprofile = sim.getACNprofile();
    
    simulationRack = new SimulationRack();
    
    // Table Labels
    Object[] rowLabels={"","Current"};   
    Object[][] filler = {
    		{"Compound",""},
    		{"Concentration",""},
    		{"Plate Number",""},
    		{"Dead Time (min)",""},
   			{"Maximum time (min)",""},
   			{"Length (cm)",""},
  			{"Flow (mL/min)",""},
  			{"Sample %B",""},
  			{"Mobile Phase %B",""},
  			{"Gradient Time (min)",""},
  			{"Phi, init",""},
  			{"Phi, final",""},
  			{"Delay time (min)",""},
  			{"Loop Size (µL)",""},
  			{"Percent Fill (%)",""}};
    //JTable inputTable = new JTable(filler,rowLabels); 
    
    // Creates blank and profile series
    XYSeries blank = new XYSeries("Blank");
    	blank.add(0,.1);    
    	blank.add(0,0);
    	blank.add(tmax,0); 
    XYSeries constant = new XYSeries("Mobile");
    	constant.add(0,100); 
    	constant.add(0,0);
    	constant.add(tmax,0);
    XYSeriesCollection blankData = new XYSeriesCollection(blank);
    XYSeriesCollection constantData = new XYSeriesCollection(constant);
    XYDataset profileData = injProfiles.profileToDataset(profile, tmax);
    XYDataset gradientData = injProfiles.profileToDataset(ACNprofile, tmax);
    
    // Charts
    Color clear = new Color(0,0,0,0);
    JFreeChart profileChart = ChartFactory.createXYLineChart(
    		"", "", "", profileData, PlotOrientation.VERTICAL, false, false, false);
    	profileChart.setBackgroundPaint(clear);
    
    JFreeChart analyteDistanceChart = ChartFactory.createXYLineChart(
            "", "", "", blankData, PlotOrientation.VERTICAL, false, false, false);
    	analyteDistanceChart.setBackgroundPaint(clear);
    
    JFreeChart analyteOutletChart = ChartFactory.createXYLineChart(
    		"", "Time (min)", "Concentration", blankData, PlotOrientation.VERTICAL, false, false, false);
    	analyteOutletChart.setTitle(new TextTitle("Chromatogram", new Font("SansSerif", Font.PLAIN,12)));
    analyteOutletChart.setBackgroundPaint(new Color(1, 1, 1, 1));
    
    JFreeChart eluentDistanceChart = ChartFactory.createXYLineChart(
    		"", "Length (cm)", "", blankData,
    		PlotOrientation.VERTICAL, false, false, false);
    eluentDistanceChart.setBackgroundPaint(new Color(1, 1, 1, 1));
    
    JFreeChart eluentInletChart = ChartFactory.createXYLineChart(
    		"", "Time (min)", "Percent", gradientData,
    		PlotOrientation.VERTICAL, false, false, false);
    eluentInletChart.setBackgroundPaint(new Color(1, 1, 1, 1));
    
    JFreeChart eluentOutletChart = ChartFactory.createXYLineChart(
    		"", "Time (min)", "Percent", constantData,
    		PlotOrientation.VERTICAL, false, false, false);
    eluentOutletChart.setTitle(
    		new TextTitle("Column Outlet Solvent Composition", new Font("SansSerif", Font.PLAIN,12)));
    eluentOutletChart.setBackgroundPaint(new Color(1, 1, 1, 1));
    
    // Creates plots
    Color gridLine = new Color(199,199,199);
    
    // Profile Plot
    profilePlot = (XYPlot) profileChart.getPlot();
    profilePlot.setBackgroundPaint(Color.white);
    profilePlot.getRenderer().setSeriesPaint(0, new Color(25, 25, 255));
    profilePlot.setDomainGridlinePaint(gridLine);
    profilePlot.setRangeGridlinePaint(gridLine);
    
    // Analyte Distance Profile
    analyteDistancePlot = (XYPlot) analyteDistanceChart.getPlot();
    analyteDistancePlot.setBackgroundPaint(Color.white);
    analyteDistancePlot.setDomainGridlinePaint(gridLine);
    analyteDistancePlot.setRangeGridlinePaint(gridLine);
    analyteDistancePlot.getRenderer().setSeriesPaint(0, new Color(25, 25, 255));

    analyteOutletPlot = (XYPlot) analyteOutletChart.getPlot();
    analyteOutletPlot.setBackgroundPaint(Color.white);
    analyteOutletPlot.getRenderer().setSeriesPaint(0, new Color(25, 25, 255));
    analyteOutletPlot.setDomainGridlinePaint(gridLine);
    analyteOutletPlot.setRangeGridlinePaint(gridLine);
    
    eluentInletPlot = (XYPlot) eluentInletChart.getPlot();
    eluentInletPlot.setBackgroundPaint(Color.white);
    eluentInletPlot.getRenderer().setSeriesPaint(0, new Color(255, 25, 25));
    eluentInletPlot.setDomainGridlinePaint(gridLine);
    eluentInletPlot.setRangeGridlinePaint(gridLine);
    
    eluentDistancePlot = (XYPlot) eluentDistanceChart.getPlot();
    eluentDistancePlot.setBackgroundPaint(Color.white);
    eluentDistancePlot.getRenderer().setSeriesPaint(0, new Color(255, 25, 25));
    eluentDistancePlot.setDomainGridlinePaint(gridLine);
    eluentDistancePlot.setRangeGridlinePaint(gridLine);
    
    eluentOutletPlot = (XYPlot) eluentOutletChart.getPlot();
    eluentOutletPlot.setBackgroundPaint(Color.white);
    eluentOutletPlot.getRenderer().setSeriesPaint(0, new Color(255,25,25));
    eluentOutletPlot.setDomainGridlinePaint(gridLine);
    eluentOutletPlot.setRangeGridlinePaint(gridLine);
    
    // Sets axes
    timeAxis = new NumberAxis("Time (min)");
    lengthAxis = new NumberAxis("Length (cm)");
    analyteYaxis = new NumberAxis("");
    eluentYaxis = new NumberAxis("%B");
    	eluentYaxis.setRange(0,100);
    analyteYaxis.setAutoRange(true);
    chromatogramYaxis = new NumberAxis("Signal");
    analyteDistancePlot.setRangeAxis(analyteYaxis);
    analyteDistancePlot.setDomainAxis(lengthAxis);
    NumberAxis profileAxis = new NumberAxis("Mass");
    profileAxis.setAutoRange(true);
    profilePlot.setRangeAxis(profileAxis);
    profilePlot.setDomainAxis(timeAxis);
    analyteOutletPlot.setRangeAxis(chromatogramYaxis);
    analyteOutletPlot.setDomainAxis(timeAxis);
    eluentOutletPlot.setRangeAxis(eluentYaxis);
    eluentOutletPlot.setDomainAxis(timeAxis);
    
    NumberAxis eluentInletyaxis = new NumberAxis("%B");
    eluentInletyaxis.setRange(0,100);
    eluentDistanceyaxis = new NumberAxis("");
    eluentDistanceyaxis.setRange(0,100);
	timeAxis.setRange(0,tmax*1.001);
    eluentDistancePlot.setRangeAxis(eluentDistanceyaxis);
    eluentDistancePlot.setDomainAxis(lengthAxis);
    eluentInletPlot.setRangeAxis(eluentInletyaxis);
    eluentInletPlot.setDomainAxis(timeAxis);
    
    
    // Creates chart panels
    ChartPanel profilePanel = new ChartPanel(profileChart);
    profilePanel.setPreferredSize(new Dimension(700, 400));
    
    ChartPanel analyteDistancePanel = new ChartPanel(analyteDistanceChart);
    analyteDistancePanel.setPreferredSize(new Dimension(700, 400));
    
    ChartPanel analyteOutletPanel = new ChartPanel(analyteOutletChart);
    analyteOutletPanel.setPreferredSize(new Dimension(700, 400));
    
    ChartPanel eluentDistancePanel = new ChartPanel(eluentDistanceChart);
    eluentDistancePanel.setPreferredSize(new Dimension(700, 400));
    
    ChartPanel eluentInletPanel = new ChartPanel(eluentInletChart);
    eluentInletPanel.setPreferredSize(new Dimension(700, 400));
    
    ChartPanel eluentOutletPanel = new ChartPanel(eluentOutletChart);
    eluentOutletPanel.setPreferredSize(new Dimension(700, 400));
   
    
    //Create a text pane.
    textPane = new JTextPane();
    JScrollPane paneScrollPane = new JScrollPane(textPane);
    paneScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    paneScrollPane.setPreferredSize(new Dimension(250, 155));
    paneScrollPane.setMinimumSize(new Dimension(100, 10));
    paneScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Parameters"),
            BorderFactory.createRaisedBevelBorder()));
    
    // Creates markers
    analyteDistanceMarker = new IntervalMarker(99,100);

    analyteYMarkerOut = new IntervalMarker(0,-1);
    analyteYMarkerOut.setOutlinePaint(new Color(72,72,255, 25));
    
    analyteYMarkerIn = new IntervalMarker(0,-1);
    analyteYMarkerIn.setOutlinePaint(new Color(255, 25, 255, 25));;
    
    eluentYMarkerOut = new IntervalMarker(0,-1);
    eluentYMarkerOut.setOutlinePaint(new Color(72,72,255, 25));
    
    eluentYMarkerIn = new IntervalMarker(0,-1);
    eluentYMarkerIn.setOutlinePaint(new Color(255, 25, 255, 25));;
    
    eluentDistanceMarker = new IntervalMarker(-1,0);
    eluentDistanceMarker.setPaint(new Color(72, 72, 72));
    
    eluentYMarker = new IntervalMarker(0,-1);
    eluentYMarker.setPaint(new Color(222, 22, 255, 128));
    
    profileYMarker = new IntervalMarker(0,-1);
    profileYMarker.setPaint(new Color(22, 222, 255, 128));
    
    int fontSize = 11;
    Font labelFont = new Font("Sans Serif", Font.PLAIN, fontSize);
    eluentDistanceInLabel = new XYTextAnnotation("", 0,0);
    eluentDistanceInLabel.setFont(labelFont);
    eluentDistanceInLabel.setTextAnchor(TextAnchor.BASELINE_CENTER);
    eluentDistanceInLabel.setPaint(Color.black);
    
    eluentDistanceOutLabel = new XYTextAnnotation("", 0,0);
    eluentDistanceOutLabel.setFont(labelFont);
    eluentDistanceOutLabel.setTextAnchor(TextAnchor.BASELINE_CENTER);
    eluentDistanceOutLabel.setPaint(Color.black);
    
    analyteDistanceInLabel = new XYTextAnnotation("", 0,0);
    analyteDistanceInLabel.setFont(labelFont);
    analyteDistanceInLabel.setTextAnchor(TextAnchor.BASELINE_CENTER);
    analyteDistanceInLabel.setPaint(Color.black);
    
    analyteDistanceOutLabel = new XYTextAnnotation("", 0,0);
    analyteDistanceOutLabel.setFont(labelFont);
    analyteDistanceOutLabel.setTextAnchor(TextAnchor.BASELINE_CENTER);
    analyteDistanceOutLabel.setPaint(Color.black);

    analyteOutletLabel = new XYTextAnnotation("", 0,0);
    analyteOutletLabel.setFont(labelFont);
    analyteOutletLabel.setTextAnchor(TextAnchor.BASELINE_CENTER);
    analyteOutletLabel.setPaint(Color.black);
    
    eluentOutletLabel = new XYTextAnnotation("", 0,0);
    eluentOutletLabel.setFont(labelFont);
    eluentOutletLabel.setTextAnchor(TextAnchor.BASELINE_CENTER);
    eluentOutletLabel.setPaint(Color.black);
    
    profileLabel = new XYTextAnnotation("", 0,0);
    profileLabel.setFont(labelFont);
    profileLabel.setTextAnchor(TextAnchor.BASELINE_CENTER);
    profileLabel.setPaint(Color.black);
    
    
    analyteDistancePlot.addDomainMarker(analyteDistanceMarker, org.jfree.ui.Layer.BACKGROUND);
    analyteDistancePlot.addRangeMarker(analyteYMarkerOut, org.jfree.ui.Layer.BACKGROUND);
    analyteDistancePlot.addRangeMarker(analyteYMarkerIn, org.jfree.ui.Layer.BACKGROUND);
    analyteDistancePlot.addAnnotation(analyteDistanceInLabel);
    analyteDistancePlot.addAnnotation(analyteDistanceOutLabel);
    
    eluentInletLabel = new XYTextAnnotation("", 0,0);
    eluentInletLabel.setFont(labelFont);
    eluentInletLabel.setTextAnchor(TextAnchor.BASELINE_CENTER);
    eluentInletLabel.setPaint(Color.black);
    
    eluentDistancePlot.addDomainMarker(eluentDistanceMarker, org.jfree.ui.Layer.BACKGROUND);
    //eluentDistancePlot.addRangeMarker(eluentYMarker, org.jfree.ui.Layer.BACKGROUND);
    eluentDistancePlot.addRangeMarker(analyteYMarkerOut, org.jfree.ui.Layer.BACKGROUND);
    eluentDistancePlot.addRangeMarker(analyteYMarkerIn, org.jfree.ui.Layer.BACKGROUND);
    eluentDistancePlot.addAnnotation(eluentDistanceInLabel);
    eluentDistancePlot.addAnnotation(eluentDistanceOutLabel);
    //eluentDistancePlot.addAnnotation(eluentDistanceLabel);
    //eluentDistancePlot.addAnnotation(updateLabel);
    
    marker = new IntervalMarker(0,200);
    analyteOutletPlot.addRangeMarker(analyteYMarkerOut, org.jfree.ui.Layer.BACKGROUND);
    analyteOutletPlot.addDomainMarker(marker, org.jfree.ui.Layer.BACKGROUND);
    analyteOutletPlot.addAnnotation(analyteOutletLabel);
    
    //eluentOutletPlot.addRangeMarker(eluentYMarkerOut, org.jfree.ui.Layer.BACKGROUND);
    
    eluentInletPlot.addRangeMarker(eluentYMarkerIn, org.jfree.ui.Layer.BACKGROUND);
    eluentInletPlot.addDomainMarker(marker, org.jfree.ui.Layer.BACKGROUND);
    eluentInletPlot.addAnnotation(eluentInletLabel);
    
    eluentOutletPlot.addRangeMarker(eluentYMarkerOut, org.jfree.ui.Layer.BACKGROUND);
    eluentOutletPlot.addDomainMarker(marker, org.jfree.ui.Layer.BACKGROUND);
    eluentOutletPlot.addAnnotation(eluentOutletLabel);
    
    profilePlot.addRangeMarker(profileYMarker, org.jfree.ui.Layer.BACKGROUND);
    profilePlot.addDomainMarker(marker, org.jfree.ui.Layer.BACKGROUND);
    profilePlot.addAnnotation(profileLabel);
    
    // Creates range simulationSliders for gradient input
    eluentSlider = new JSlider(0,100,(int) Math.round(sim.eluent));
    eluentSlider.setOrientation(SwingConstants.VERTICAL);
    eluentSlider.setPaintTicks(true);
    eluentSlider.setPaintLabels(true);
    eluentSlider.setMajorTickSpacing(10);
    eluentSlider.setFont(new Font("TimesRoman",Font.PLAIN,10));
    analyteSlider = new JSlider(0,100,(int) Math.round(sim.sample));
    analyteSlider.setOrientation(SwingConstants.VERTICAL);
    analyteSlider.setPaintTicks(true);
    analyteSlider.setPaintLabels(true);
    analyteSlider.setMajorTickSpacing(10);
    analyteSlider.setFont(new Font("TimesRoman",Font.PLAIN,10));
    
    gradientXSlider = new RangeSlider(0,sim.tsteps);
    gradientXSlider.setValue((int) Math.round(sim.NgradientInit));
    gradientXSlider.setUpperValue((int) Math.round(sim.NgradientFinal));
    Box mobileXSliderFrame = new Box(BoxLayout.X_AXIS);
    mobileXSliderFrame.add(gradientXSlider);
    
    gradientYSlider = new RangeSlider(0,100);
    gradientYSlider.setOrientation(1);
    gradientYSlider.setValue((int) Math.round(sim.Gi));
    gradientYSlider.setUpperValue((int) Math.round(sim.Gf));
    
    eluentSlider.setOpaque(false);
    analyteSlider.setOpaque(false);
    gradientXSlider.setOpaque(false);
    gradientYSlider.setOpaque(false);
    
    // Injection Profile box with range simulationSliders
    int[] values = {25,50,75,100,200};
    int[] values2 = {15,20,40,60,80};
    JLabel loopVolumeLabel = new JLabel("<html>Loop<br>volume (µL)</html>");
    JLabel percFillLabel = new JLabel("<html>Percent<br>fill (%)</html>");
    	loopVolumeLabel.setFont(new Font("TimesRoman",Font.PLAIN,10));
    	percFillLabel.setFont(new Font("TimesRoman",Font.PLAIN,10));
    loopVolumeSlider = new steppingSlider(values2,5,80,"µL");
    percFillSlider = new steppingSlider(values,25,200,"%");
    	double weightx0 = 0;
    	double weightx1 = 0;
    	double weightx2 = 100;
    	double weighty0 = 0;
    	double weighty1 = 1;
    JPanel injectionPanel = new JPanel();
    injectionPanel.setOpaque(false);
    injectionPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    	c.gridx = 0; c.gridy = 0;
    	c.weightx = weightx0;
    	c.weighty = weighty0;
    	c.insets = new Insets(0,-10,0,50);
    injectionPanel.add(loopVolumeLabel, c);
    	c.gridx = 1; 
    	c.weightx = weightx1;
    	c.insets = new Insets(0,-40,0,0);
    injectionPanel.add(percFillLabel, c);
		compoundS.setFont(new Font("TimesRoman",Font.PLAIN,10));
		JPanel compoundBox = new JPanel();
			compoundBox.setOpaque(false);
			compoundBox.setLayout(new GridBagLayout());
			JLabel compoundLabel = new JLabel("Compounds:");
			compoundLabel.setFont(new Font("TimesRoman",Font.PLAIN,10));
			c.gridx = 0;
			c.gridy = 0;
			c.insets = new Insets(0,0,0,0);
			compoundBox.add(compoundLabel,c);
			c.gridy = 1;
			compoundBox.add(compoundS,c);
		c.gridy = 0;
		c.gridx = 2; 
		c.weightx = .1;
		c.insets = new Insets(0,60,10,60);
	injectionPanel.add(compoundBox, c);
    	c.gridx = 0; c.gridy = 1;
    	c.weightx = weightx0;
    	c.weighty = weighty1;
    	c.insets = new Insets(0,-10,0,50);
    injectionPanel.add(loopVolumeSlider, c);
    	c.gridx = 1;
    	c.weightx = weightx1;
    	c.insets = new Insets(0,-55,0,0);
    injectionPanel.add(percFillSlider, c);
    	c.gridx = 2;
    	c.weightx = weightx2;
    	c.insets = new Insets(-13,-15,0,21);
    injectionPanel.add(profilePanel, c);
    
    weightx0 = 0;
    weightx1 = 0;
    weightx2 = 100;
    double weightx3 = .1;
    //      	0					1				2				3
    // 0   									gradientXSlider  
    // 1  analyteSlider		eluentSlider   eluentInletPanel  gradientYSlider
    //
    weighty0 = 0;
    weighty1 = 1;     
    
    // Sample and analyte labels
    JLabel analyteLabel = new JLabel("<html>Analyte<br>%B</html>");
    JLabel eluentLabel = new JLabel("<html>Eluent<br>%B</html>");
    analyteLabel.setFont(new Font("TimesRoman",Font.PLAIN,10));
    eluentLabel.setFont(new Font("TimesRoman",Font.PLAIN,10));
    
    // Eluent time box with range simulationSliders
    JPanel eluentInletBox = new JPanel();
    eluentInletBox.setLayout(new GridBagLayout());
		c.gridx = 0; c.gridy = 0;
		c.weightx = weightx0;
		c.weighty = weighty0;
		c.insets = new Insets(0,-9,0,50);
	eluentInletBox.add(analyteLabel, c);
    	c.gridx = 1;
		c.weightx = weightx1;
		c.insets = new Insets(0,-52,0,0);
	eluentInletBox.add(eluentLabel, c);
		c.gridx = 2; c.gridy = 0;
		c.weightx = weightx2;
		c.insets = new Insets(0,16,-25,10);
	eluentInletBox.add(gradientXSlider, c);
    	c.gridx = 0; c.gridy = 1;
    	c.weightx = weightx0;
    	c.weighty = weighty1;
    	c.insets = new Insets(0,-9,35,50);
    eluentInletBox.add(analyteSlider, c);
    	c.gridx = 1;
    	c.weightx = weightx1;
    	c.insets = new Insets(0,-67,35,0);
    eluentInletBox.add(eluentSlider, c);
    	c.gridx = 2;
    	c.weightx = weightx2;
    	c.insets = new Insets(0,-20,0,10);
    eluentInletBox.add(eluentInletPanel, c);
    	c.gridx = 3; c.gridy = 1;
    	c.weightx = weightx3;
    	c.insets = new Insets(5,-14,37,10);
    eluentInletBox.add(gradientYSlider, c);
    c.insets = new Insets(0,0,0,0);

    // Column Visualizer
    JPanel columnVisualizer = new JPanel();
    columnVisualizer.setLayout(new OverlayLayout(columnVisualizer));
    	preColumnTubing = new TubingVisualizer(sim,new File("images/delay.png"),.2,.8);
    	cv = new ColumnVisualizer(sim, new File("images/column.png"));
    	postColumnTubing = new TubingVisualizer(sim, new File("images/postColumnTubing.png"),.2,.8);
    columnVisualizer.add(preColumnTubing);
    columnVisualizer.add(cv);
    columnVisualizer.add(postColumnTubing);
    preColumnTubing.setBorder(0,0,0,0);
    cv.setBorder(0,290,0,0);
    postColumnTubing.setBorder(0,450,0,0);
    columnVisualizer.setMinimumSize(new Dimension(550,100));
    
    // Creates the play button
    playIcon = new ImageIcon("images/play.png", "play button");
    pauseIcon = new ImageIcon("images/pause.png","pause button");
    bPlay = new JButton(playIcon);
    bPlay.setActionCommand("play");
    
    // Creates simulationSlider
    simulationSlider = new JSlider(0,100,0);
    simulationSlider.enable(false);
    simulationSlider.setPaintTicks(true);
    simulationSlider.setPaintLabels(false);
    
    // Creates the play button-simulationSlider box
    Box playSlider = new Box(BoxLayout.X_AXIS);
    playSlider.add(bPlay); 
    bPlay.setEnabled(false);
    playSlider.add(simulationSlider);  
    playSlider.setBorder(BorderFactory.createEtchedBorder());

    // Menu
    Font f = new Font("SansSerif", Font.PLAIN,11);
    JMenuBar menuBar = new JMenuBar();
    JMenu menu1 = new JMenu("File");
    	menu1.setFont(f);
    	menu1.setMnemonic(KeyEvent.VK_F);
    	menu1.getAccessibleContext().setAccessibleDescription(
    			"More to come!");
    		JMenuItem menuItem1 = new JMenuItem("This does nothing");
    	menu1.add(menuItem1);
    JMenu menu2 = new JMenu("Edit");
    	menu2.setFont(f);
    JMenu menu3 = new JMenu("View");
    	menu3.setFont(f);
    JMenu menu4 = new JMenu("Tools");
    	menu4.setFont(f);
    JMenu menu5 = new JMenu("About");
        menu5.setFont(f);
    menuBar.add(menu1);
    menuBar.add(menu2);
    menuBar.add(menu3);
    menuBar.add(menu4);
    menuBar.add(menu5);
    
    
    // ORGANIZATION
    
    // Left Pane
    JPanel l = new JPanel();
    l.setLayout(new GridBagLayout());
	l.setPreferredSize(new Dimension(300,600));
	l.setOpaque(false);
	
    
    // Rows for L box
    	c.weighty = .5;
    	c.gridy = 0;
    l.add(parameterPane,c);
    
    Box control_panel = new Box(BoxLayout.X_AXIS);
    	control_panel.add(clear_buttons_jSplP);
    	c.weighty = .4;
    	c.gridy = 1;
    	l.add(control_panel, c);
    Box bottom_left_box = new Box(BoxLayout.X_AXIS);
    
    bottom_left_box.add(start);bottom_left_box.add(progressBar);
    	c.weighty = .01;
    	c.gridy = 2;
    	l.add(bottom_left_box,c);
    parameterPane.setBorder(BorderFactory.createLineBorder(Color.GREEN));
    bottom_left_box.setBorder(BorderFactory.createLineBorder(Color.GREEN));
    l.setMinimumSize(new Dimension(370,500));
    
    //		0					1				
    //  profilePanel  analyteDistancePanel  
    //  eluentInletBox eluentDistancePanel
    weightx0 = .1;
    weightx1 = 500;
    weighty0 = .5;
    weighty1 = .5;
    
    // Middle visualizer panel
    JPanel vis = new JPanel();
    	//vis.setLayout(new GridLayout(2,3));
    	vis.setLayout(new GridBagLayout());
    	vis.setMinimumSize(new Dimension(500,700));
    		c.gridx = 0; c.gridy = 0;
    		c.weightx = weightx0;
    		c.weighty = weighty0;
    		c.insets = new Insets(0,0,0,0);
    	vis.add(injectionPanel, c);
    		c.gridx = 1;
    		c.weightx = weightx1;
    		c.insets = new Insets(20,-22,0,10);
    	vis.add(analyteDistancePanel, c);
    		c.gridx = 0; c.gridy = 1;
    		c.weightx = weightx0;
    		c.weighty = weighty1;
    		c.insets = new Insets(0,0,0,0);
    	vis.add(eluentInletBox, c);
    		c.gridx = 1;
    		c.weightx = weightx1;
    		c.insets = new Insets(20,-25,0,10);
    	vis.add(eluentDistancePanel, c);
    	c.insets = new Insets(0,0,0,0);
    	
    		profilePanel.setOpaque(false);
    		analyteDistancePanel.setOpaque(false);
    		analyteOutletPanel.setOpaque(false);
    		gradientXSlider.setOpaque(false);
    		gradientYSlider.setOpaque(false);
    		eluentInletPanel.setOpaque(false);
    		eluentInletBox.setOpaque(false);
    		eluentDistancePanel.setOpaque(false);
    		vis.setOpaque(false);
    JLayeredPane mid = new JLayeredPane();
		mid.setLayout(new GridBagLayout());
		mid.setOpaque(false);
		columnVisualizer.setOpaque(false);
		
		c.gridy = 0; c.gridx = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(0,0,-10,0);
	mid.add(columnVisualizer,c,-1);
		c.gridy = 1;
		c.weighty = 100;
		c.insets = new Insets(0,0,0,0);
    mid.add(vis,c,2);
    	c.gridy = 2;
    	c.weighty = 1;
    	//c.insets = new Insets()
    mid.add(playSlider,c,2);
    
    // Create a text area.
    JPanel r = new JPanel();
    	r.setLayout(new GridLayout(2,1));
    BufferedImage detector = null;
    //try {
        //detector = ImageIO.read(new File("detectorChromatogram.png"));
    //} catch (IOException e) {
    //}
    JPanel detectorPanel = new JPanel();
    detectorPanel.setLayout(new OverlayLayout(detectorPanel));
    //JLabel detectorPic = new JLabel(new ImageIcon(detector));
    analyteOutletPanel.setOpaque(false);
    //analyteOutletPanel.setBorder(BorderFactory.createEtchedBorder());
    //detectorPanel.add(analyteOutletPanel);
    //detectorPanel.add(detectorPic);
    //r.setMinimumSize(new Dimension(400, 600));
    //Border border2 = analyteOutletPanel.getBorder();
    //Border margin2 = new EmptyBorder(12,12,15,20);
    /*analyteOutletPanel.setBorder(new CompoundBorder(border2,margin2));
    detectorPic.setBorder(new CompoundBorder(border2,new EmptyBorder(20,0,5,0)));
    	r.add(detectorPanel);
    	r.add(rightPane);
    		detectorPanel.setOpaque(false);
    		rightPane.setOpaque(false);
    		r.setOpaque(false);*/
    JScrollPane toolTipPaneContainer = new JScrollPane();
    toolTipPane = new toolTipPane();
    	toolTipPane.setMaximumSize(new Dimension(400,10));
    	toolTipPane.setFont(new Font("SansSerif", Font.PLAIN,10));
    	toolTipPane.setBorder(BorderFactory.createTitledBorder("Tool tips"));
    r.setOpaque(true);
    r.setMinimumSize(new Dimension(400, 600));
    r.setLayout(new GridBagLayout());
    	c.weightx = 1;
    	c.weighty = .5;
    	c.gridx = 0;
    	c.gridy = 0;
    	c.insets = new Insets(0,0,0,0);
    r.add(analyteOutletPanel,c);
    	c.weighty = .5;
    	c.gridy = 1;
    r.add(eluentOutletPanel,c);
    	c.weighty= .05;
    	c.gridy = 2;
    toolTipPaneContainer.add(toolTipPane);
    r.add(toolTipPane,c);
    r.setBorder(BorderFactory.createCompoundBorder(
    		BorderFactory.createTitledBorder("Detector"),
    		BorderFactory.createBevelBorder(2)));
    //r.add(eluentInletBox, c);
    	c.gridy = 1;
    	//r.add(eluentInletBox, c);
    		
    // Packs and visualizes the components
    gradientFrame.setSize(new Dimension(1600,500));
    gradientFrame.setLayout(new GridBagLayout());
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    gradientFrame.add(l,c);
    c.gridx = 1;
    gradientFrame.add(mid,c);
    c.gridx = 2;
    gradientFrame.add(r,c);
    gradientFrame.setBackground(Color.WHITE);
    
    //simulatorFrame.setJMenuBar(menuBar);
    simulatorFrame.add(gradientFrame);
    simulatorFrame.pack();
    simulatorFrame.setLocationRelativeTo(null);
    simulatorFrame.setVisible(true);
    
    // Change Listeners
    gradientXSlider.addChangeListener(new ChangeListener() {
    	public void stateChanged(ChangeEvent e){
    	  if (!isChanging){
    		// Updates the eluentInletPlot when changed
    		sim.NgradientInit = gradientXSlider.getValue();
    		sim.NgradientFinal = gradientXSlider.getUpperValue();
    		double tgsteps = sim.NgradientFinal - sim.NgradientInit;
    		sim.slope = (sim.Gf - sim.Gi) / tgsteps;
    		sim.Ntd = sim.NgradientInit - sim.NinputT_loop;
    		sim.tgsteps = gradientXSlider.getUpperValue() - sim.NgradientInit;
    		eluentInletPlot.setDataset(injProfiles.profileToDataset(sim.getACNprofile(), sim.tmax));
    		parameterPane.setValueAt(func.truncate(tgsteps * dt,3), 9);
    		parameterPane.setValueAt(func.truncate(sim.Ntd * dt,3), 12);
    		start.setEnabled(true);
    		if (running){
    			bPlay.setEnabled(false);
    			timer.cancel();
    			timerOn = false;
    		}
    	  }
    	}
    });    
    
    /*timewinder.addChangeListener(new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			//timeMult = (timewinder.getMaximum()-timewinder.getValue())/100.0;
			//System.out.println(timewinder.getValue());
			//System.out.println(timeMult);
			//System.out.println(tmax/(simulationSlider.getMaximum())*60*1000*timeMult);
			int increment = (int) Math.round(tmax/(simulationSlider.getMaximum())*60*1000*timeMult);
			if (increment == 0){
				increment = 1;
			}
			if (timerOn){
				timer.scheduleAtFixedRate(new TimerTask(){
					public void run() {
						simulationSlider.setValue(simulationSlider.getValue() + 1);
					}
				},0,increment);
			}
		}
    });*/
    
    // Table listener
    parameterPane.inputTable.addMouseListener(new MouseListener(){
		@Override
		public void mouseClicked(MouseEvent e) {		}
		public void mousePressed(MouseEvent e) {		}
		public void mouseReleased(MouseEvent e) {
			int row = parameterPane.inputTable.getSelectedRow();
			String[] toolTips = {"The compound abbreviation used as the analyte.",
					"The concentration of the sample (mass/volume)",
					"The column is divided into this many theoretical plates.",
					"The amount of time it takes for an unretained \ncompound (i.e., uracil) to flow through the column (min).",
					"How long the experiment is to be run (min).",
					"The length of the high-performance liquid \nchromatography (HPLC) column (cm).",
					"The flow of the pump (mL/min)",
					"The percentage of organic modifier, acetonitrile \n(ACN), in the sample (0 - 100%).",
					"The percentage of organic modifier, acetonitrile \n(ACN), in the mobile phase eluent (0 - 100%).",
					"The time length of the gradient step (min).",
					"The initial ACN percentage during the gradient \nstep (min)",
					"The final ACN percentage during the gradient \nstep (min)",
					"The amount of delayed time it takes for mobile \nphase from the loop to reach the column (min).",
					"The loop volume containing the sample (µL). \nChoose from 15 µL, 20 µL, 40 µL, 60 µL, and 80 µL.",
					"The percent fill of the injection loop containing the sample. \nChoose from 25%, 50%, 75%, 100%, and 200%.",
					"The flow of the pump (mL/min)."};		
			toolTipPane.setText(toolTips[row]);
		}
		public void mouseEntered(MouseEvent e) {	}
		public void mouseExited(MouseEvent e) {}
    });
    parameterPane.resultsinputTable.addMouseListener(new MouseListener(){
		@Override
		public void mouseClicked(MouseEvent e) {		}
		public void mousePressed(MouseEvent e) {		}
		public void mouseReleased(MouseEvent e) {
			int row = parameterPane.resultsinputTable.getSelectedRow();
			String[] toolTips = {"The time (in minutes) at which \nthe analyte exits the column.",
					"The width (in minutes) of the eluted peak in \nthe chromatogram at half its height.",
					"The width (in minutes) of the eluted peak in \nthe chromatogram at 4.4% of the peak's height.",
					"The area of the eluted peak in the chromatogram",
					"A summation of all the signals in the chromatogram.",
					"The height of the eluted peak (in signal units) \nin the chromatogram.",
					"The calculated retention factor at the end \nof the simulation and column",
					"The runtime of the simulation in seconds."};		
			toolTipPane.setText(toolTips[row]);
			toolTipPane.setMaximumSize(new Dimension(400,10));
		}
		public void mouseEntered(MouseEvent e) {	}
		public void mouseExited(MouseEvent e) {}
    });
    
    parameterPane.inputTable.addContainerListener(new ContainerListener(){

		@Override
		public void componentAdded(ContainerEvent e) {
		}
		
		// Updates components based on parameters recently inputed.
		@Override
		public void componentRemoved(ContainerEvent e) {
			// Extracts variables
			int tgsteps = (int) Math.round(parameterPane.processValueAt(9)/sim.dt);
			int Ntd = (int) Math.floor((double) parameterPane.processValueAt(12)/sim.dt);
			int loopSize = (int) parameterPane.processValueAt(13);
			int percFill= (int) parameterPane.processValueAt(14);
			N = (int) parameterPane.processValueAt(2);
			double Tm = parameterPane.processValueAt(3);
			
			// Extracts parameters from pane and recalculates
    		sim.varInit(parameterPane.extractParameters());
    		sim.varCalculation();
    		
    		// Transfers values from input to current history slot
    		parameterPane.mirrorHistory();
    		
			// Initial and final %ACN for the gradient step
			gradientYSlider.setValue((int) Math.round(sim.Gi));
			gradientYSlider.setUpperValue((int) parameterPane.processValueAt(11));
			
			sim.loopSize = loopSize;
			// Calculate NgradientFinal
			sim.NgradientFinal = sim.NgradientInit + tgsteps;

			// Sets analyte and eluent %ACN simulationSliders
			analyteSlider.setValue((int) parameterPane.processValueAt(7));
			eluentSlider.setValue((int) parameterPane.processValueAt(8));
			
			// Set injection plug
			profile = injProfiles.profileExtraction(Integer.toString(loopSize), Integer.toString(percFill),N,Tm,sim.tmax);
			profilePlot.setDataset(injProfiles.profileToDataset(profile, sim.tmax));
			
			// Replots gradient profile
			sim.profile = profile;
			sim.varCalculation();
			eluentInletPlot.setDataset(injProfiles.profileToDataset(sim.getACNprofile(), sim.tmax));
			
			// Rescales the time axis of the eluent inlet plot
			timeAxis.setRange(0,sim.tmax*1.001);
			eluentInletPlot.setDomainAxis(timeAxis);
			
			// Snaps the simulationSlider to the closest value
			loopVolumeSlider.setValue(loopVolumeSlider.closestValue(loopSize));
			percFillSlider.setValue(percFillSlider.closestValue(percFill));
			
			// Changes gradient simulationSlider without triggering its action listener
			isChanging = true;
			gradientXSlider.setMaximum(sim.tsteps);
			gradientXSlider.setValue(sim.NgradientInit);
			gradientXSlider.setUpperValue(sim.NgradientFinal);
			isChanging = false;
		}
    });
    
    loopVolumeSlider.addChangeListener(new ChangeListener() {
    	public void stateChanged(ChangeEvent e){
    		loopVolumeSlider.snapToValue(); // Snaps to specific values
    		
    		// Extracts values
    		String loopSize = Integer.toString(loopVolumeSlider.getValue());
			String percFill = Integer.toString(percFillSlider.getValue());
			N = (int) parameterPane.processValueAt(2);
    		double Tm = parameterPane.processValueAt(3);
    		tmax = parameterPane.processValueAt(4);
    		
			// Updates delay time in respect to the adjusted loop size
			sim.loopSize = Double.parseDouble(loopSize);
			sim.NgradientFinal = sim.NgradientInit + sim.tgsteps;
			
			// Extracts the correct profile
    		profile = injProfiles.profileExtraction(loopSize, percFill,N,Tm,tmax);
    		sim.profile = profile;
    		
    		// Update the plots
    		profilePlot.setDataset(injProfiles.profileToDataset(profile, tmax));
    		eluentInletPlot.setDataset(injProfiles.profileToDataset(sim.getACNprofile(), tmax));
    		
    		// Updates the simulationSliders
    		isChanging = true;
    		gradientXSlider.setUpperValue(sim.NgradientFinal);
    		gradientXSlider.setValue(sim.NgradientInit);
    		isChanging = false;
    		
			// Updates values in the table
			parameterPane.setValueAt(loopSize,13);
			
    		// Updates button
    		start.setEnabled(true);
    		
    		parameterPane.mirrorHistory();
		}
    });
    
    percFillSlider.addChangeListener(new ChangeListener() {
    	public void stateChanged(ChangeEvent e){
    		percFillSlider.snapToValue();
    		
    		String loopSize = Integer.toString(loopVolumeSlider.getValue());
			String percFill = Integer.toString(percFillSlider.getValue());
    		parameterPane.setValueAt(percFill,14);
    		N = (int) parameterPane.processValueAt(2);
    		double Tm = parameterPane.processValueAt(3);
    		tmax = parameterPane.processValueAt(4);
    		
    		profile = injProfiles.profileExtraction(loopSize, percFill,N,Tm,tmax);
    		sim.profile = profile;
    		//spv.init(sim);
    		profilePlot.setDataset(injProfiles.profileToDataset(profile, tmax));
    		eluentInletPlot.setDataset(injProfiles.profileToDataset(sim.getACNprofile(), tmax));
    		start.setEnabled(true);
    		
    		parameterPane.mirrorHistory();
    	}
    });
    
    gradientYSlider.addChangeListener(new ChangeListener() {
    	public void stateChanged(ChangeEvent e){
    		// Updates the eluentInletPlot when changed
    		sim.Gi = gradientYSlider.getValue();
    		sim.Gf = gradientYSlider.getUpperValue();
    		sim.slope = (sim.Gf - sim.Gi) / (sim.NgradientFinal - sim.NgradientInit);
    		eluentInletPlot.setDataset(injProfiles.profileToDataset(sim.getACNprofile(), sim.tmax));
    		
    		parameterPane.setValueAt(sim.Gi, 10);
    		parameterPane.setValueAt(sim.Gf, 11);
    		start.setEnabled(true);
    		if (running){
    			bPlay.setEnabled(false);
    			timer.cancel();
    			timerOn = false;
    		}
    		parameterPane.mirrorHistory();
    	}
    });
    
    eluentSlider.addChangeListener(new ChangeListener() {
    	public void stateChanged(ChangeEvent e){
    		// Updates the eluentInletPlot when changed
    		sim.eluent= eluentSlider.getValue();
    		eluentInletPlot.setDataset(injProfiles.profileToDataset(sim.getACNprofile(), sim.tmax));
    		parameterPane.setValueAt(sim.eluent, 8);
    		start.setEnabled(true);
    		if (running){
    			bPlay.setEnabled(false);
    			timer.cancel();
    			timerOn = false;
    		}
    		parameterPane.mirrorHistory();
    	}
    }); 
    
    analyteSlider.addChangeListener(new ChangeListener() {
    	public void stateChanged(ChangeEvent e){
    		// Updates the eluentInletPlot when changed
    		sim.sample= analyteSlider.getValue();
    		eluentInletPlot.setDataset(injProfiles.profileToDataset(sim.getACNprofile(), sim.tmax));
    		parameterPane.setValueAt(sim.sample, 7);
    		start.setEnabled(true);
    		if (running){
    			bPlay.setEnabled(false);
    			timer.cancel();
    			timerOn = false;
    		}
    		parameterPane.mirrorHistory();
    	}
    }); 
    
 // Action listeners that update the table when modified
    compoundS.addActionListener(new ActionListener(){
    	public void actionPerformed(ActionEvent evt) {
    		parameterPane.setValueAt((String) compoundS.getSelectedItem(),0);
    		start.setEnabled(true);
    		parameterPane.mirrorHistory();
    	}	        	
    });
    
    
    // If a new parameter is set, "Calculate" button will activate.
    parameterPane.inputTable.addContainerListener(new ContainerListener() {
    	public void componentAdded(ContainerEvent e) {	start.setEnabled(true);}
    	public void componentRemoved(ContainerEvent e) {	start.setEnabled(true);}
    });
    
    // Start button action listener
    start.addActionListener(new ActionListener(){
    	public void actionPerformed(ActionEvent evt) {
    		if (start.getText().equals("Calculate")){
    			if (parameterPane.table.isEditing()){
    				parameterPane.table.getCellEditor().stopCellEditing();
    			}
    			if (parameterPane.inputTable.isEditing()){
    				parameterPane.inputTable.getCellEditor().stopCellEditing();
    			}
    			running = true;
    			simulationSlider.setValue(0);
    			simulationSlider.setEnabled(false);
    			bPlay.setEnabled(false);
    			new Thread(new simulationThread()).start();
        		start.setText("Stop");
        		parameterPane.selectedColumn = simulationNum;
        		parameterPane.changeHeader();
        		
    		} else if (start.getText().equals("Stop")){
    			running = false;
    			start.setText("Calculate");
    			progressBar.setValue(0);
    			parameterPane.failed(simulationNum); // Red
    			failed = true;
    			parameterPane.fillColumn(simulationNum + 1, 1); // Blue
    			//simulationNum++;
    			//parameterPane.tick();
    			parameterPane.refresh();
    		}
    		
    	}	        	
    });
    
    // Stops the timer once the simulationSlider reaches the end
    simulationSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            //plot.setDataset(sim.get(simulationSlider.getValue()));
        	if (simulationSlider.getValue()==simulationSlider.getMaximum()){
        		timer.cancel();
        		timerOn = false;
        		bPlay.setActionCommand("play");
    			bPlay.setIcon(playIcon);
        	};
        	
        	// Column Visualizer
        	if (simulationSlider.getValue() >= 1){   	        		
        		preColumnTubing.update(sim, simulationSlider.getValue(),false);
        		postColumnTubing.update(sim, simulationSlider.getValue(),true);
        		cv.update(sim, simulationSlider.getValue());
        	}
        	//spv.update(sim,simulationSlider.getValue());
        }
    });
    
	// Slider propagation
    class runTask extends TimerTask{
		@Override
		public void run() {
			simulationSlider.setValue(simulationSlider.getValue() + 1);
		}
	}
	timer = new Timer();
	
	// History
	parameterPane.header.addMouseListener(new MouseAdapter() {
		@SuppressWarnings("unchecked")
		public void mouseClicked(MouseEvent mouseEvent){
			String letter = parameterPane.getLetter();
			/*if (simulationRack.simulationExists(letter)){
				Object[] data = simulationRack.getSimulation(letter);
				sim.analyteDistanceList = (List<XYDataset>) data[0];
				sim.eluentDistanceList = (List<XYDataset>) data[1];
				sim.analyteTimeList = (List<XYDataset>) data[2];
				sim.eluentTimeList = (List<XYDataset>) data[3];
				//analyteDistancePlot.setDataset(sim.get(simulationSlider.getValue())[0]);
				analyteDistancePlot.setDataset(sim.get(simulationSlider.getValue())[0]);
    			eluentDistancePlot.setDataset(sim.get(simulationSlider.getValue())[1]);
    			analyteOutletPlot.setDataset((XYDataset) data[4]);
    			eluentInletPlot.setDataset(1,injProfiles.profileToDataset((double[]) data[5], (double) data[6]));
			}*/
		}
	});
	
    bPlay.addActionListener(new ActionListener(){
    	public void actionPerformed(ActionEvent e){
    		
    		// Pause/Play conditionals
    		if (bPlay.getActionCommand().equals("play")){
    			if (simulationSlider.getValue()== simulationSlider.getMaximum()){
    				simulationSlider.setValue(0);
    			}
    			timer = new Timer();
        		//timer.scheduleAtFixedRate(new runTask(),0, (int) Math.round(sim.dt*60*1000)*frameInterval);
    			//System.out.println(timeMu);
    			//System.out.println((int) Math.round(tmax/(simulationSlider.getMaximum())*60*1000*timeMult));
    			timer.scheduleAtFixedRate(new runTask(),0,(int) Math.round(tmax/(simulationSlider.getMaximum())*60*1000*timeMult));
        		timerOn = true;
    			bPlay.setIcon(pauseIcon);
        		bPlay.setActionCommand("pause");
    		} else if (bPlay.getActionCommand().equals("pause")) {;
    			timer.cancel();
    			timerOn = false;
    			bPlay.setIcon(playIcon);
    			bPlay.setActionCommand("play");
    		}
    	}
    });
    initialRun = true;
    
    
    // Tool tip mouse listeners.
    profilePanel.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("The injection profile plug.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    loopVolumeSlider.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("The injection loop selector. Only a few loop\nvolumes are available.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    percFillSlider.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("The percent fill selector for the injection \nloop. Only a few loop percentages are available.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    analyteDistancePanel.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("The analyte profile throughout the column.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    eluentInletPanel.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("The %ACN profile as it is injected into the \ncolumn over time");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    analyteSlider.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("The %ACN within the sample.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    eluentSlider.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("The %ACN within the mobile phase.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    eluentDistancePanel.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("The %ACN profile throughout the column.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    gradientXSlider.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("Determine the initial and final times for the \ngradient step.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    gradientYSlider.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("The initial and final %ACN values for the \ngradient step.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    columnVisualizer.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("The column visualizer.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    analyteOutletPanel.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("The analyte signal detected at the end of \nthe column over time.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    eluentOutletPanel.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("The organic modifier composition \n as the mobile phase exits the column over time.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    compoundS.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("The available compounds.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    parameterPane.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("Used for data input and output.\nClick on specific parameters/results for more info.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    parameterPane.resultsinputTable.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("Used for result output.\nClick on specific results for more info.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    parameterPane.inputTable.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("Used for data input.\nClick on specific parameters for more info.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    parameterPane.table.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("Used for simulation histories.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
    parameterPane.resultsTable.addMouseListener(new MouseListener(){
		public void mouseEntered(MouseEvent e) {toolTipPane.setText("Used for simulation histories.");}
		public void mouseExited(MouseEvent e) {toolTipPane.setText("");} public void mouseClicked(MouseEvent e) {}; public void mousePressed(MouseEvent e) {}; public void mouseReleased(MouseEvent e) {}
    });
}


public static class simulationThread implements Runnable{
	@SuppressWarnings("deprecation")
	public void run(){
			try{Thread.sleep(50);}
			catch (InterruptedException err){}  
			
			runTimer = new Timer();
			seconds = 0;
			runTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					seconds = seconds + .001;
				}
			}, 1, 1);
			
			parameterPane.transferValuesToTable(selected);
			parameterPane.refresh();
	    	
			// Processes the input cells
			String compound = (String) parameterPane.getValueAt(0, selected);
			double C = parameterPane.processValueAt(1);
			N = (int) parameterPane.processValueAt(2);
			double Tm = parameterPane.processValueAt(3);
			tmax = parameterPane.processValueAt(4);
			double length = parameterPane.processValueAt(5);
    		double flow = parameterPane.processValueAt(6);
    		double sample = parameterPane.processValueAt(7);
    		double eluent = parameterPane.processValueAt(8);
    		double tg = parameterPane.processValueAt(9);
    		double Gi = parameterPane.processValueAt(10);
    		double Gf = parameterPane.processValueAt(11);
    		double td = parameterPane.processValueAt(12);
    		String loopSize = (String) parameterPane.getValueAt(13, selected);
    		String percFill = (String) parameterPane.getValueAt(14, selected);
			
    		// Extracts appropriate injection profile
    		injProfiles = new profiles(3,"data/profile.csv");
    		profile = injProfiles.profileExtraction(loopSize,percFill,N,Tm,tmax);
    		profilePlot.setDataset(injProfiles.profileToDataset(profile, tmax));

    		// Extracts appropriate Neue-Kuss parameters
    		profiles neueKuss = new profiles(1,"data/neueKuss.csv");
    		double[] NKParameters = neueKuss.profileMap.get(compound);   		
    	    
    		// Prepares NK parameter result output
    		String parameterText=String.format("Current Run: \n"
    				+ "k00 = %s \n"
    				+ "A = %s \n"
    				+ "B = %s \n"
    				+ "Compound = %s \n"
    				+ "Concentration = %s \n"
    				+ "Plate Number = %s \n"
    				+ "Dead Time = %s \n"
    				+ "Maximum Time = %s \n"
    				+ "Column Length (cm) = %s \n"
    				+ "Flow (mL/min) = %s \n"
    				+ "Sample Percent = %s \n"
    				+ "analyte Percent = %s \n"
    				+ "Gradient time = %s \n"
    				+ "Initial phi = %s \n"
    				+ "Final phi = %s \n"
    				+ "Delay Time = %s \n"
    				+ "Loop Size = %s \n"
    				+ "Percent Fill = %s \n"
    				+ "Rank = %s \n",
    				Double.toString(NKParameters[0]), Double.toString(NKParameters[1]),Double.toString(NKParameters[2]),compound,C,N,Tm,tmax,length,flow,sample,eluent,tg,Gi,Gf,td,loopSize,percFill,"0");
    		textPane.setText(parameterText);
    		//double[] parameters = {C,N,Tm,tmax,length,flow,sample,eluent,tg,Gi,Gf,td,Double.parseDouble(loopSize),Double.parseDouble(percFill)};
    		Object[] parameters = parameterPane.extractParameters();
    		sim = new simulation(compound,NKParameters,parameters,profile);

    		sim.init();
    		timeMult = 1;
    		int tsteps = sim.tsteps;
    		zsteps = sim.zsteps;
    		simulationSlider.enable(false);
    		
    	    // Calculates which frames are essential
			int fps = 60;
			int framesNeededapprox = (int) Math.round(tmax*60*fps);
			frameInterval = (int) Math.round(tsteps/framesNeededapprox);
			if (frameInterval == 0) frameInterval = 1;
			int framesNeeded = (int) Math.round(tsteps/frameInterval);
			int frameCount = 0;
			if (frameInterval == 1){
				framesNeededapprox = tsteps;
			}
    		m = 1;
    		simulationSlider.setMaximum(framesNeeded-2);
    		progressBar.setMaximum(tsteps);
    		
    		// Sets axes
    	    analyteDistancePlot.setRangeAxis(analyteYaxis);
    	    timeAxis.setRange(0,tmax*1.001);
    	    eluentInletPlot.setDomainAxis(timeAxis);
    	    
    		// Analyte markers
			analyteOutletPlot.addDomainMarker(marker);
			analyteDistanceMarker.setStartValue(sim.length);
			analyteDistanceMarker.setEndValue(sim.length+10);
			dt = sim.dt;
			dz = sim.dz;
			
			// Eluent markers
			eluentOutletPlot.addDomainMarker(marker);
			eluentDistanceMarker.setStartValue(sim.length);
			eluentDistanceMarker.setEndValue(sim.length+10);
			
			// Sets up the plots
			eluentInletPlot.setDataset(sim.getGradient());
			simulationSlider.setMajorTickSpacing((int) Math.round(tsteps/tmax));
			simulationSlider.setMinorTickSpacing((int) Math.round(tsteps/tmax/60));
			gradientXSlider.disable();
			gradientYSlider.disable();

			
			
			double analyteYValueOut;
			double analyteYValueIn;
			double eluentYValueOut;
			double eluentYValueIn;
			
    		compoundS.setEnabled(false);
    		
    		// Sets as yellow
    		parameterPane.isProcessing(simulationNum);
    		parameterPane.refresh();
    		parameterPane.historyFonts();
    		
			// Loading Loop
    		while (running && m < tsteps){
    		  sim.update(m);
    		  if((m % frameInterval == 0 && m < tsteps - frameInterval) || m == tsteps-1){
        		sim.addDataset(frameCount);
        		analyteDistanceDataset = sim.get(frameCount)[0];
        		eluentDistanceDataset = sim.get(frameCount)[1];
        		
        		// Calculate values before plotting
            	analyteYValueOut = analyteDistanceDataset.getYValue(0,zsteps-1);
            	analyteYValueIn = analyteDistanceDataset.getYValue(0,0);
    			
    			// eluent distance graph markers
            	eluentYValueIn = eluentDistanceDataset.getYValue(0, selected);
            	eluentYValueOut = eluentDistanceDataset.getYValue(0, selected);
            	
            	analyteOutletLabel.setText(String.format("%s , %s", (((double) Math.round(m*dt*1000))/1000),analyteYValueOut));
            	//analyteOutletLabel.setText(String.format("%s", analyteYValueOut));
            	analyteOutletLabel.setX(m*dt + tsteps*.2);
            	analyteOutletLabel.setY(analyteYValueOut);
            	
            	//eluentInletLabel.setText(String.format("%s , %s", (((double) Math.round(m*dt*1000))/1000),eluentYValueIn));
            	//eluentInletLabel.setX(time + simulationSlider.getMaximum() * dt * .1);
            	//eluentInletLabel.setY(eluentYValueIn);
            	
            	eluentOutletLabel.setText(String.format("%s , %s", (((double) Math.round(m*dt*1000))/1000),analyteYValueOut));
            	eluentOutletLabel.setX(m*dt + tsteps*.2);
            	eluentOutletLabel.setY(eluentYValueOut);
            	
            	
    			// Set datasets and markers
    			analyteDistancePlot.setDataset(analyteDistanceDataset);
    			eluentDistancePlot.setDataset(eluentDistanceDataset);
    			analyteOutletPlot.setDataset(sim.analyteTimeSet);
    		    eluentOutletPlot.setDataset(sim.eluentTimeSet);
    			
    			
    			simulationSlider.setValue(frameCount);
    			
    			marker.setStartValue(m*dt);
            	analyteYMarkerOut.setStartValue(analyteYValueOut); 
            	analyteYMarkerIn.setStartValue(analyteYValueIn); 
    			eluentYMarkerIn.setStartValue(eluentYValueIn);
    			eluentYMarkerOut.setStartValue(eluentYValueOut);
            	profileYMarker.setStartValue(profile[m]); 	
            	
            	marker.setEndValue(tsteps*dt);
            	analyteYMarkerOut.setEndValue(analyteYValueOut);
            	analyteYMarkerIn.setEndValue(analyteYValueIn);
            	eluentYMarkerIn.setEndValue(eluentYValueIn);
            	eluentYMarkerOut.setStartValue(eluentYValueOut);
            	profileYMarker.setEndValue(profile[m]);
            	
            	double time = m * dt;
            	
            	analyteOutletLabel.setText(String.format("(%s , %s)", (((double) Math.round(frameCount*frameInterval*dt*1000))/1000), (double) Math.round(analyteYValueOut*1000)/1000));
            	analyteOutletLabel.setX(time + simulationSlider.getMaximum() * dt * .1);
            	analyteOutletLabel.setY(analyteYValueOut);
            	
            	eluentOutletLabel.setText(String.format("(%s , %s)", (((double) Math.round(frameCount*frameInterval*dt*1000))/1000), (double) Math.round(analyteYValueOut*1000)/1000));
            	eluentOutletLabel.setX(time + simulationSlider.getMaximum() * dt * .1);
            	eluentOutletLabel.setY(eluentYValueOut);
            	
            	eluentInletLabel.setText(String.format("(%s , %s)", (((double) Math.round(frameCount*frameInterval*dt*1000))/1000), (double) Math.round(eluentYValueIn*1000)/1000));
            	eluentInletLabel.setX(time + simulationSlider.getMaximum() * dt * .1);
            	eluentInletLabel.setY(eluentYValueIn);
            	
            	eluentDistanceInLabel.setText(String.format("%s", func.truncate(eluentYValueIn,3)));
            	eluentDistanceInLabel.setX(zsteps * dz * .1);
            	eluentDistanceInLabel.setY(eluentYValueIn);
            	
            	eluentDistanceOutLabel.setText(String.format("%s", func.truncate(eluentYValueIn,3)));
            	eluentDistanceOutLabel.setX(zsteps*dz*.9);
            	eluentDistanceOutLabel.setY(eluentYValueOut);
            	
            	analyteDistanceInLabel.setText(String.format("%s", func.truncate(analyteYValueIn,3)));
            	analyteDistanceInLabel.setX(zsteps * dz * .1);
            	analyteDistanceInLabel.setY(analyteYValueIn);
            	
            	analyteDistanceOutLabel.setText(String.format("%s", func.truncate(analyteYValueOut,3)));
            	analyteDistanceOutLabel.setX(zsteps * dz * .9);
            	analyteDistanceOutLabel.setY(analyteYValueOut);
            	
            	profileLabel.setText(String.format("(%s , %s)", (((double) Math.round(frameCount*frameInterval*dt*10000))/10000), (double) Math.round(analyteYValueIn*10000)/10000));
            	profileLabel.setX(time + simulationSlider.getMaximum() * dt * .1);
            	profileLabel.setY(profile[m]);
            	
    			frameCount++;
    		  }
    		  progressBar.setValue(m);
  			  m++;
    		}
    		
    		compoundS.setEnabled(true);
		
    		// If the simulation finishes completely, the simulationSlider and play button will activate along with the corresponding graph-simulationSlider listener.
    		if (running) {
    			simulationSlider.enable(true);
    			bPlay.setEnabled(true);
    			if (initialRun){
    			  simulationSlider.addChangeListener(new ChangeListener() {
    	        	public void stateChanged(ChangeEvent e) {
    	        		if (!running){
    	        			simulationSliderValue = simulationSlider.getValue();
    	        			time = simulationSliderValue * frameInterval * dt;
    	        			analyteDistanceDataset = sim.get(simulationSliderValue)[0];
    	            		eluentDistanceDataset = sim.get(simulationSliderValue)[1];
    	        			analyteDistancePlot.setDataset(analyteDistanceDataset);
    	        			eluentDistancePlot.setDataset(eluentDistanceDataset);

    	        			// analyte markers   		
        	            	double analyteYValueOut = analyteDistanceDataset.getYValue(0,zsteps-1);
        	            	double analyteYValueIn = analyteDistanceDataset.getYValue(0, selected);
        	            	analyteYMarkerOut.setStartValue(analyteYValueOut);
        	    			analyteYMarkerOut.setEndValue(analyteYValueOut);
        	    			analyteYMarkerIn.setStartValue(analyteYValueIn);
        	    			analyteYMarkerIn.setEndValue(analyteYValueIn);
        	    			
    	        			// eluent markers
    	        			double eluentYValueIn = eluentDistanceDataset.getYValue(0, selected);
    	        			double eluentYValueOut= eluentDistanceDataset.getYValue(0, zsteps-1);
    	        			eluentYMarkerIn.setStartValue(eluentYValueIn);
    	        			eluentYMarkerIn.setEndValue(eluentYValueIn);
    	                	eluentYMarkerOut.setStartValue(eluentYValueOut);
    	        			eluentYMarkerOut.setEndValue(eluentYValueOut);
    	        			
    	        			analyteOutletLabel.setText(String.format("(%s , %s)", (((double) Math.round(simulationSliderValue*frameInterval*dt*1000))/1000), (double) Math.round(analyteYValueOut*1000)/1000));
    	                	analyteOutletLabel.setX(time + simulationSlider.getMaximum() * dt * .1);
    	                	analyteOutletLabel.setY(analyteYValueOut);
    	                	
    	                	eluentOutletLabel.setText(String.format("(%s , %s)", (((double) Math.round(simulationSliderValue*frameInterval*dt*1000))/1000), (double) Math.round(eluentYValueOut*1000)/1000));
    	                	eluentOutletLabel.setX(time + simulationSlider.getMaximum() * dt * .1);
    	                	eluentOutletLabel.setY(eluentYValueOut);
    	                	
    	                	eluentInletLabel.setText(String.format("(%s , %s)", (((double) Math.round(simulationSliderValue*frameInterval*dt*1000))/1000), (double) Math.round(eluentYValueIn*1000)/1000));
    	                	eluentInletLabel.setX(time + simulationSlider.getMaximum() * dt * .1);
    	                	eluentInletLabel.setY(eluentYValueIn);
    	                	
    	                	eluentDistanceInLabel.setText(String.format("%s", func.truncate(eluentYValueIn, 3)));
    	                	eluentDistanceInLabel.setX(zsteps * dz * .1);
    	                	eluentDistanceInLabel.setY(eluentYValueIn);
    	                	
    	                	eluentDistanceOutLabel.setText(String.format("%s", func.truncate(eluentYValueOut,3)));
    	                	eluentDistanceOutLabel.setX(zsteps * dz * .9);
    	                	eluentDistanceOutLabel.setY(eluentYValueOut);
    	                	
    	                	analyteDistanceInLabel.setText(String.format("%s", (double) Math.round(analyteYValueIn*1000)/1000));
    	                	analyteDistanceInLabel.setX(zsteps * dz * .07);
    	                	analyteDistanceInLabel.setY(analyteYValueIn);
    	                	
    	                	analyteDistanceOutLabel.setText(String.format("%s", (double) Math.round(analyteYValueOut*1000)/1000));
    	                	analyteDistanceOutLabel.setX(zsteps * dz * .93);
    	                	analyteDistanceOutLabel.setY(analyteYValueOut);
    	                	
    	                	profileLabel.setText(String.format("(%s , %s)", (((double) Math.round(simulationSliderValue*frameInterval*dt*1000))/1000), (double) Math.round(profile[simulationSliderValue * frameInterval]*1000)/1000));
    	                	profileLabel.setX(time + simulationSlider.getMaximum() * dt * .1);
    	                	profileLabel.setY(profile[simulationSliderValue * frameInterval]);
    	                	
    	        			// Injection profile graph markers
    	                	profileYMarker.setStartValue(profile[simulationSliderValue*frameInterval]);
    	        			profileYMarker.setEndValue(profile[simulationSliderValue*frameInterval]);
    	        			 
    	        			marker.setStartValue(time);
        	        		marker.setEndValue(time);    	
    	        		}
    	        	}
    			  });
    			  initialRun = false;
    			}
    		}
    		
    		// Obtain results
			sim.areaCorrection();
    		double[] results = sim.getResults(seconds);
    		
    		// Reset start button, stop timer, set results, enable simulationSliders
    		start.setText("Calculate");
    		runTimer.cancel();
    		if (!failed){
    			start.setEnabled(false);
    			parameterPane.setResults(simulationNum, results);
    		} else{
    			failed = false;    			
    		}
    		running = false;
    		gradientXSlider.enable();
			gradientYSlider.enable();

    		
    		// Specify the number of decimal places 
    		double digits = 6;
    		for (int i = 0; i < results.length; i++){
    			results[i] = Math.round(results[i] * Math.pow(10, digits)) / Math.pow(10, digits);
    		}
    		
    		// Store simulations
    		simulationRack.addSimulation(parameterPane.getLetter(), sim);
    		
    		// Advance the column highlighter by one (shift it right)
    		if (parameterPane.selectedColumn == simulationNum){
    			selected = parameterPane.tick();
    		}
    		
    		// Sets the colors of the simulations
    		if (m == tsteps){
    			parameterPane.fillColumn(simulationNum, 3); // Green
    			parameterPane.fillColumn(simulationNum + 1, 1); // Blue
    			parameterPane.refresh();
    		}
    		
    		// Advance simulation
    		simulationNum++;
    		parameterPane.mirrorHistory();
    		
    	    // Sets axes
    		analyteYaxis.setRange(sim.absoluteMax*-.1,sim.absoluteMax*1.1);
    	    analyteDistancePlot.setRangeAxis(analyteYaxis);
    	    eluentDistancePlot.setRangeAxis(eluentDistanceyaxis);
    	    

    	    // Vestige ACN profile
    	    eluentInletPlot.setDataset(1,injProfiles.profileToDataset(sim.getACNprofile(), sim.tmax));
    	    XYLineAndShapeRenderer renderer0 = new XYLineAndShapeRenderer();
    	    XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
    	    renderer0.setSeriesPaint(0,new Color(255,25,25)); //Red
    	    renderer1.setSeriesPaint(0,new Color(125,125,125)); //Grey
    	    renderer0.setSeriesShapesVisible(0, false);
    	    renderer1.setSeriesShapesVisible(0, false);
    	    eluentInletPlot.setRenderer(0, renderer0);
    	    eluentInletPlot.setRenderer(1, renderer1);
    	    n++;

		}
	}	


public static void main(String[] args){
		createAndShowGUI();
		EventQueue.invokeLater(new Runnable() {
            public void run() {
            }
        });
	}
}