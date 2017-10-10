import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class simulation {
	int ID;
	double input;
	double C;
	int N;
	double tm;
	double tmax;
	double length;
	double flow;
	double sample;
	double eluent;
	double tg;
	double Gi;
	double Gf;
	double td;
	double dz;
	double dt;
	double dvT;
	int tsteps;
	int zsteps;
	matrix analyteMat;
	matrix eluentMat;
	matrix analyteFinal;
	matrix eluentFinal;
	matrix Mfinal;
	int Ntd;
	double Ninput;
	int tgsteps;
	double slope;
	double[] kp;
	double k;
	double k2;
	double prevK;
	double inputRem;
	double inputPerDt;
	int NinputT;
	double compound;
	double td_pump;
	double loopSize;
	double percFill;
	int NinputT_loop;
	double loss;
	int NgradientInit;
	int NgradientFinal;
	boolean previousK = true;
	boolean currentK = false;
	List<XYDataset> analyteDistanceList;
	List<XYDataset> eluentDistanceList;
	List<XYDataset> analyteTimeList;
	List<XYDataset> eluentTimeList;

	XYDataset analyteTimeSet;
	XYDataset eluentTimeSet;
	String neueKussFitPath;
	double[] profile;
	double profileArea;
	double[] gradient;
	double[] neueKuss;
	int IDno;
	double cell;
	double absoluteMax = 0;
	XYDataset gradientDataset;
	int test = 49;
	String compoundName;
	double plateVelocity;
	double dv;
	
	public simulation(String compoundName, double[] neueKussParameters,Object[] parameters,double[] selectedProfile){
		// Parameter set up
		this.compoundName = compoundName;
		neueKuss = neueKussParameters; //K00 a b
		profile = selectedProfile;
		varInit(parameters);
		varCalculation();
		//double injectionMax = max(profile);
		
	}
	
	public void varInit(Object[] parameters){
		int i = 0;
		this.compoundName = (String) parameters[i]; i++;
		C = (double) parameters[i]; i++;
		N = (int) Math.round((double) parameters[i]); i++;
		tm= (double) parameters[i]; i++;
		tmax = (double) parameters[i]; i++;
		length = (double) parameters[i]; i++;
		flow = (double) parameters[i]; i++;
		sample = (double) parameters[i]; i++;
		eluent = (double) parameters[i]; i++;
		tg = (double) parameters[i]; i++;
		Gi = (double) parameters[i]; i++;
		Gf = (double) parameters[i]; i++; // 11
		td_pump = (double) parameters[i]; i++;
		loopSize = (double) parameters[i]; i++;
		percFill = (double) parameters[i]; i++;
	}
	
	public void varCalculation(){
		// Variable calculations
		double Vm = flow * tm;
		dz = length / N;
		dt = tm / N;
		zsteps = (int) Math.floor(length / dz);
		tsteps = (int) Math.floor(tmax / dt);
		input = loopSize * percFill/100;
		dv = Vm*1000/N;
		dvT = flow * tm * 1000 / tsteps;
		int NinputV = (int) Math.floor(input/dv);
		plateVelocity = N/(tm*60);
		NinputT_loop = (int) Math.floor((loopSize/dv)/(plateVelocity*dt*60));

		NinputT = (int) Math.floor(NinputV/(plateVelocity*dt*60));
		Ntd = (int) Math.floor(td_pump/dt);
		loss = (percFill==100 && loopSize!=0.4) ? 7 : 0;
		inputPerDt = flow * tm * 1000 / N;	
		tgsteps = (int) Math.round(tg/dt);
		slope=(Gf-Gi)/tgsteps;
		profileArea = trapz(profile);
		NgradientInit = NinputT_loop + Ntd;
		NgradientFinal = NgradientInit + tgsteps;
	}
	
	public void init(){
		// Initialize the matrices with the correct size
		analyteMat = new matrix(zsteps);
		analyteFinal = new matrix(tsteps);
		eluentMat = new matrix(zsteps); 
		eluentFinal = new matrix(tsteps);

		// Initializes the datasets
		analyteDistanceList = new ArrayList<XYDataset>();
		eluentDistanceList = new ArrayList<XYDataset>();
		analyteTimeList = new ArrayList<XYDataset>();
		eluentTimeList = new ArrayList<XYDataset>();
		
		// Fills the analyte and eluent profile matrices with the appropriate concentrations/percentages
		analyteMat.fill(0);
		analyteFinal.fill(0);
		eluentMat.fill(eluent); //Should this be filled with 0?
		
		// Assigns variables necessary for k calculation
		analyteMat.setNeueKuss(neueKuss);
		eluentMat.setNeueKuss(neueKuss);
		eluentFinal.setNeueKuss(neueKuss);
		eluentMat.setDtDz(dt, dz);
		analyteMat.setDtDz(dt, dz);
		
		// Established %ACN vs. time profile
		getACNprofile();
		
		double[][] ACNmpd = {
				{0,.004,.01, .05, .1, .2, .3, .4,.5, .6, .7, .8,.9,.95,.99,.995,1},
				{4,1.48,1.23,0.82,0.73,0.64,0.48,0.27,0.1,0.03,0.05,0.09,0.21,0.3,0.67,0.9,4}
		};		

		eluentMat.setACNmpd(ACNmpd);
		
		// Establishes the matrix of local k values
		kp = new double[tsteps];
	
		// Introduces the injection profile and disperses it
		analyteMat.inject((100-loss)/100*input*C*profile[0]/profileArea);
		analyteMat.disperse(false,eluentMat);
		eluentMat.set(0,gradient[0]);
		eluentMat.disperseEluent();
		kp[0] = analyteMat.getFinalK(eluentMat);
		//prevK = eluentMat.kCalc(0,currentK);
		
		// Used to determine the correct vertical max for the plot
		if (analyteMat.max() > absoluteMax) { absoluteMax=analyteMat.max();}
		
		// Stores the numbers for the next iteration
		analyteMat.transfer();
		eluentMat.transfer();
	}
	
	/*public double getUpdatedDelayTime(){
		NinputT_loop = (int) Math.floor((loopSize/dv)/(plateVelocity*dt*60));
		return NgradientInit - NinputT_loop;
	}*/ 
	
	public int getNinputTLoop(){
		return (int) Math.floor((loopSize/dv)/(plateVelocity*dt*60));
	}
	
	public double[] getACNprofile(){
		gradient = new double[tsteps];	
		NinputT_loop = (int) Math.floor((loopSize/dv)/(plateVelocity*dt*60));
		
		NgradientInit = NinputT_loop + Ntd;
		NgradientFinal = NgradientInit + tgsteps;
		slope=(Gf-Gi)/tgsteps;
		for (int i = 0; i < tsteps; i++){
			if (i <= NgradientInit){
				gradient[i] = eluent;
			} else if (NgradientInit < i && i <= NgradientFinal){
				gradient[i] = slope * (i - (NgradientInit)) + Gi;
			} else {
				gradient[i] = eluent;
			}
			/*if (NgradientFinal < i){
				double tail =-2*Math.pow(i-NgradientFinal,.5)+Gf;
				if (tail > 2){
					gradient[i] =tail;
				} else{
					gradient[i] = eluent;
				} 
			}*/
			gradient[i] = gradient[i] + NinputT*(sample-eluent)*(100-loss)/100*profile[i]/profileArea;
		}
		return gradient;
	}

	public void update(int m){
		Boolean test = false;
		// Solvent input & dispersion
		eluentMat.initPropagateEluent();
		eluentMat.set(0,gradient[m]);
		eluentMat.propagateEluentnonMPD(gradient[m]);
		//eluentMat.propagateEluent(gradient[m]);
		eluentMat.disperseEluent();
		//eluentMat.disperse(test, eluentMat);
		kp[m] = analyteMat.getFinalK(eluentMat);
		
		// Sample input & dispersion
		analyteMat.initPropagateAnalyte(eluentMat);
		analyteMat.inject((100-loss)/100*input*C*profile[m]/profileArea);
		analyteMat.propagateAnalyte(eluentMat);
		analyteMat.disperse(test, eluentMat);
		
		// To find max for profile plot dimensions
		//if (analyteMat.max() > absoluteMax){ absoluteMax = analyteMat.max();}
		for (int i = 0; i < analyteMat.length; i++){
			if (analyteMat.get(i) > absoluteMax){
				absoluteMax = analyteMat.get(i);
			}
		}
		
		analyteFinal.assign(m,analyteMat.get(zsteps-1));
		eluentFinal.assign(m, eluentMat.get(zsteps-1));
		
		analyteMat.transfer();
		eluentMat.transfer();
	}
	
	
	public void test(int m,int init, int fin){
		if (init <= m && m < fin){
			System.out.print("    ");
			System.out.print(m + ": ");
			//System.out.println(gradient[m]);
			if (m == 0){
				func.print(analyteMat.previousMatrix);
			}
			func.print(analyteMat.matrix);
			System.out.println("hello");
		}
	}
	
		
	public void addDataset(int frameCount) {
		final XYSeries analyteDistanceSeries = new XYSeries("Concentration");
		final XYSeries eluentDistanceSeries = new XYSeries("Concentration");
		final XYSeries analyteTimeSeries = new XYSeries("Concentration");
		final XYSeries eluentTimeSeries = new XYSeries("Concentration");
		
		for (int z = 0; z < zsteps; z++) {
			analyteDistanceSeries.add(z*dz, analyteMat.get(z));
			eluentDistanceSeries.add(z*dz, eluentMat.get(z));
		
		}
		
		for (int t = 0; t < tsteps; t++){
			analyteTimeSeries.add(t*dt, analyteFinal.get(t));
			eluentTimeSeries.add(t*dt, eluentFinal.get(t));
		}		
		analyteTimeSet = new XYSeriesCollection(analyteTimeSeries);
		eluentTimeSet = new XYSeriesCollection(eluentTimeSeries);
		analyteDistanceList.add(frameCount ,new XYSeriesCollection(analyteDistanceSeries));
		eluentDistanceList.add(frameCount ,new XYSeriesCollection(eluentDistanceSeries));
	}
	
	public XYDataset getGradient(){
		final XYSeries eluentTimeSeries = new XYSeries("Concentration");
		for (int t = 0; t < tsteps; t++){
			eluentTimeSeries.add(t*dt,gradient[t]);
		}
		gradientDataset = new XYSeriesCollection(eluentTimeSeries);
		return gradientDataset;
	}
	
	public XYDataset[] get(int m){
		XYDataset[] analyteEluent = new XYDataset[2];
		analyteEluent[0] = analyteDistanceList.get(m);
		analyteEluent[1] = eluentDistanceList.get(m);
		
		return analyteEluent;
	}
	
	public double[] getResults(double time){
		double[] results = new double[8];
		//for (int m = 244; m < 254; m++){
//			System.out.print(kp[m] + " ");
		//}
		analyteFinal.setDtDz(dt, dz);
		results[0] = analyteFinal.getPeak()*dt;
		results[1] = analyteFinal.getWidth(0.5);
		results[2] = analyteFinal.getWidth(0.04);
		results[3] = analyteFinal.trapz();
		results[4] = analyteFinal.sum();
		results[5] = analyteFinal.getHeight();
		results[6] = analyteMat.k2;
		results[7] = time;
		return results;
	}
	
	// Calculations
	public static double max(double[] profile){
		double max = 0;
		for (int i = 0; i < profile.length; i++){
			if (max < profile[i]){
				max = i;
			}
		}
		return max;
	}
	
	public static double trapz(double[] profile){
		double area = 0;
		if (profile.length == 1){
			area = profile[0];
		}
		for (int i = 1; i < profile.length; i++){
			area = area + (profile[i-1] + profile[i])/2;
		} 
		return area;
	}
	
	public void areaCorrection(){
		//k = eluentFinal.kCalc(analyteFinal.getPeak(),previousK);
		k = eluentFinal.kCalc(eluent);
		analyteFinal.divideByLocalK(kp);
		analyteFinal.divideBy(dvT);
		//analyteFinal.divideBy(1+k);
		System.out.println();
		System.out.println("conc_final: ");
		for (int i = 279; i <= 289; i++){
			System.out.print(analyteFinal.get(i) + " ");
		}
	}
	
	/*private static void transfer(matrix transferTo, matrix transferFrom) {
		for (int i = 0; i < transferTo.length; i++) {
			transferTo.assign(i, transferFrom.get(i));
		}
	}*/
}