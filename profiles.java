import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class profiles{
	double[] profile;
	ArrayList<String[]> data;
	int noOfLabels;
	Hashtable<String,double[]> profileMap;
	static int N;
	static double Tm;
	static double tmax;
	double profileTime;

	// Parameter profiles such as injection profile and neue-kuss parameters
	public profiles(int noOfLabels,String csvFile) {
		data = getCsvFile(csvFile);
		this.noOfLabels = noOfLabels;
		profileMap = profileMapper(data,noOfLabels);
		profileTime = 0.5;
	}
	
	public XYDataset profileToDataset(double[] profile, double tmax){
		XYSeries profileSeries = new XYSeries("profile");
	    double profileIncrement = tmax/profile.length;
	    double profileIncCount = 0;
	    for (int i = 0; i < profile.length; i++){
	    	profileSeries.add(profileIncCount,profile[i]);
	    	profileIncCount = profileIncCount + profileIncrement;
	    }
	    XYSeriesCollection profileData = new XYSeriesCollection(profileSeries);
	    return profileData;
	}
		
	public double[] profileExtraction(String loopSize, String percFill, int N, double Tm, double tmax) {
		String profileParameters = "L," + loopSize + "," + percFill;
		//int injProfileIndex = profileMap.get(profileParameters);
		//String[] injProfileStringData = data.get(injProfileIndex);
		//profile = dataToDouble(injProfileStringData);
		profile = profileMap.get(profileParameters);
		profile = processInjProfile(profile, profileTime, N, Tm, tmax);
		return profile;
	}

	public String[] get(Hashtable<String[],Integer> profileMap, String[] labels){
		int profileIndex = profileMap.get(labels);
		return data.get(profileIndex);
	}
	
	public double[] dataToDouble(String[] stringProfile){
		int profileLength = stringProfile.length - noOfLabels;
		double[] doubleProfile = new double[profileLength];
		for (int i = 0; i < profileLength; i++){
			doubleProfile[i] = Double.parseDouble(stringProfile[i]);
		}
		return doubleProfile;
	}
	
	public Hashtable<String,double[]> profileMapper(ArrayList<String[]> data, int noOfLabels){
		profileMap = new Hashtable<String,double[]>();
		int noOfProfiles = data.size();
		//int noOfDataPoints = data.get(0).length;
		String labels = "";
		String[] stringData = new String[data.get(0).length];
		int j;
		for (int i = 0; i < noOfProfiles; i++){
			j = 0;
			while (j < noOfLabels - 1){
				labels = labels + data.get(i)[j] + ",";
				j++;
			}
			labels = labels + data.get(i)[j];
			while (j < data.get(i).length - 1){
				j++;
				stringData[j - noOfLabels] = data.get(i)[j];
			}
			profileMap.put(labels, dataToDouble(stringData));
			labels = "";
		}	
		return profileMap;
	}
	
	// Divides dataset into keys and values.
	public Hashtable<String[],double[]> labelPartition(ArrayList<String[]> data, int noOfLabels){
		//Hashtable<int,double[]> injProfiles = new Hashtable<int,double[]>();
		int noOfDataPoints = data.get(0).length;
		int noOfProfiles = data.size();
		String[] labels = new String[noOfLabels];
		double[] dataPoints = new double[noOfDataPoints];
		//double[][] injProfiles = new double[noOfProfiles][noOfDataPoints];
		//double[] profile = new double[noOfDataPoints];
		Hashtable<String[],double[]> profile = new Hashtable<String[],double[]>();
		for (int i = 0; i < noOfProfiles; i++){
			for (int j = 0; j < noOfDataPoints; j++){
				if (j < noOfLabels){
					labels[j] = data.get(i)[j];
				} else{
					
					dataPoints[j-noOfLabels] = Double.parseDouble(data.get(i)[j]);
				}
				profile.put(labels, dataPoints);
				labels = new String[noOfLabels];
				dataPoints = new double[noOfDataPoints];
			}
		}		
		return profile;
	}
	
	// Processes csv file into array list.
	public ArrayList<String[]> getCsvFile(String csvFile){
		ArrayList<String[]> data = new ArrayList<String[]>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		//String[] dataLine;
		//String[] empty = {};
		//int i = 0;
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
			//while ((dataLine = br.readLine().split(cvsSplitBy)) != empty){
				String[] dataLine = line.split(cvsSplitBy);
				if (dataLine.length == 0) break;
				data.add(dataLine);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data;
	}
	
	public static double[] processInjProfile(double[] injProfile,double injProfTime, int N, double Tm, double tmax){
		double[] scaledInjProfile;
		
		double dt = Tm/N;
		int tsteps = (int) Math.floor(tmax / dt);
		int cutOffPoint = (int) Math.round(injProfile.length*tmax/injProfTime);
		double[] tempProf = new double[cutOffPoint+1];
		
		//if (tmax > injProfTime){
			//cutOffPoint = injProfile.length;
		//}
		
		/*for (int i = 0; i < cutOffPoint; i++){
			if (i < injProfile.length){
				tempProf[i] = injProfile[i];
			}
*/
		for (int i = 0; i < cutOffPoint; i++){
			if (i < injProfile.length){
				tempProf[i] = injProfile[i];
			} else {
				tempProf[i] = 0;
			}
		}
		if (injProfile.length > cutOffPoint){
			tempProf[cutOffPoint] = injProfile[cutOffPoint-1];
		} 
		
		
		// Initializes the time component of the truncated injection profile
		double[] tempTime = new double[cutOffPoint];
		for (int i = 0; i < cutOffPoint; i++){
			tempTime[i] = i * (tmax/cutOffPoint);
		}
		
		// Initializes timevec which is used for interpolation
		double[] timevec = new double[tsteps];
		
		for (int i = 0; i < tsteps; i++){
			timevec[i] = i * dt + dt;
		}
		
		// Scales the profile to the simulation's time vector
		scaledInjProfile = func.interp1(tempTime,tempProf,timevec);
		
		// Horizontal profile correctional shift
		double injProDelay = 0.04055;
		int NshiftT = (int) Math.floor(injProDelay/dt);
		double[] shiftedProfile = new double[tsteps];
		int i = 0;
		while (i < tsteps - NshiftT){
			shiftedProfile[i] = scaledInjProfile[i + NshiftT];
			i++;
		}		
		
		// Vertical profile correctional shift
		double sum = 0;
		for (int j = (int) Math.floor(tsteps/2) - 1; j <= tsteps - NshiftT; j++){ // the -1 is accounting for the syntax difference between MATLAB and java
			sum = sum + shiftedProfile[j];
		}
		
		double mean = sum / (tsteps - NshiftT + 1 - (int) Math.floor(tsteps/2));
		for (int j = 0; j < shiftedProfile.length; j++){
			shiftedProfile[j] = shiftedProfile[j] - mean;
		}
		
		// Extend the zeros
		while (i < tsteps){
			shiftedProfile[i] = 0;
			i++;
		}
		
		return shiftedProfile;
	}
	
	
	
}