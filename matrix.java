public class matrix{
	double[] matrix;
	double[] previousMatrix = null;
	int zsteps;
	int length;
	double max;
	int maxIndex;
	double[] neueKuss;
	double dt;
	double dz;
	double k;
	double k2;
	double[] x;
	double moment1;
	Boolean previous = true;
	double[][] ACNmpd;
	double[] kmatrix;
	double[] differences;
	double[] kDifferences;
	
	public matrix(int zsteps) {
		previousMatrix = new double[zsteps];
		matrix = new double[zsteps];
		kmatrix = new double[zsteps];
		differences = new double[zsteps];
		kDifferences = new double[zsteps];
		this.zsteps = zsteps;
		this.length = zsteps;
		x = new double[zsteps];
		
		ACNmpd = new double[2][15];
	}
	
	public void initX(double dt){
		// Initial x
		for (int i = 0; i < zsteps; i++){
			x[i] = i * dt;
		}
	}
	
	
	public void setACNmpd(double[][] ACNmpd){
		this.ACNmpd = ACNmpd;
	}
	
	public void divideBy(double val){
		for (int i = 0; i < zsteps; i++){
			matrix[i] = matrix[i] / val;
		}
	}
	
	public void divideByLocalK(double[] kp){
		for (int m = 0; m < zsteps; m++){
			matrix[m] = matrix[m] / (1 + kp[m]);
		}
	}
	
	public double sum(){
		double sum = matrix[0];
		for (int i = 0; i < length; i++){
			sum = sum + matrix[i];
		}
		return sum;
	}
	
	public double max(){
		return simulation.max(matrix);
	}
	
	
	
	public void setNeueKuss(double[] neueKuss){
		this.neueKuss = neueKuss;
	}
	
	public void setDtDz(double dt, double dz){
		this.dt = dt;
		this.dz = dz;
	}
	
	public void propagateEluentnonMPD(double fill){
		matrix = new double[zsteps];
		double value;
		
		for (int i = 1; i < zsteps;i++){
			value = previousMatrix[i-1];
			matrix[i] = value;
		}
		matrix[0] = fill;
	}
	
	public double getFinalK(matrix mobileMat){
		double[] ACN = new double[1];
		//ACN[0] = matrix[zsteps-1]/100;
		//k2 = func.interp1(ACNmpd[0], ACNmpd[1], ACN)[0];
		k2 = mobileMat.kCalc(zsteps-1, !previous);
		return k2;
	}
	
	public void propagateEluent(double fill){
		matrix = new double[zsteps];
		double[] ACN = new double[1];
		ACN[0] = fill/100;
		k = func.interp1(ACNmpd[0], ACNmpd[1], ACN)[0];
		ACN[0] = previousMatrix[0]/100;
		k2 = func.interp1(ACNmpd[0], ACNmpd[1], ACN)[0];
		matrix[0] = previousMatrix[0] * (k2/(k2+1)) + fill*(1/(k+1));
		kmatrix[0] = k2;
		differences[0] = matrix[0] - fill;
		kDifferences[0] = k2 - k;
		for (int i = 1; i < zsteps;i++){
			ACN[0] = previousMatrix[i-1]/100;
			k = func.interp1(ACNmpd[0], ACNmpd[1], ACN)[0];
			ACN[0] = previousMatrix[i]/100;
			k2 = func.interp1(ACNmpd[0], ACNmpd[1], ACN)[0];
			matrix[i] = previousMatrix[i] * (k2/(k2+1)) + previousMatrix[i-1]*(1/(k+1));
			
			kmatrix[i] = k2;  // Delete this?
			differences[i] = matrix[i] - matrix[i-1];// Delete this?
			kDifferences[i] = k2 - k;// Delete this?
	
		}
		matrix[0] = fill;
	}
	
	public void initPropagateEluent(){
		double[] ACN = new double[1];
		ACN[0] = previousMatrix[0]/100;
		k = func.interp1(ACNmpd[0], ACNmpd[1], ACN)[0];
		matrix[0] = previousMatrix[0]*(k/(k+1));
	}
	
	public void initPropagateAnalyte(matrix mobileMat){
		k = mobileMat.kCalc(0,previous);
		matrix[0] = previousMatrix[0]*(k/(k+1));
	}
	
	public void propagateAnalyte(matrix mobileMat){
		for (int i = 1; i < zsteps; i++){
			k = mobileMat.kCalc(i-1, previous);
			k2 = mobileMat.kCalc(i, previous);
			//matrix[i] = previousMatrix[i] * (k/(k2+1)) + previousMatrix[i-1]*(1/(k2+1));
			matrix[i] = previousMatrix[i] * (k2/(k2+1)) + previousMatrix[i-1]*(1/(k+1));
		}
	}

	public matrix(double[] profile){
		matrix = profile;
	}
	
	public void transfer(){
		for (int i = 0; i < zsteps;i++){
			previousMatrix[i] = matrix[i];
		}
		//matrix = new double[zsteps];
		//previousMatrix = matrix;
	}
	
	public void disperse(Boolean test, matrix eluentMat){
		boolean currentK = false;
		
		double[] dC = new double[zsteps];
		double k = eluentMat.kCalc(0,currentK);
		double kPrev = k;
		double D = dz * dz / (dt * (2 * (k +1) * (k + 1)));

		dC[0] = -D * matrix[0] + D * matrix[1]; //for first position and time
		for (int i = 1; i < zsteps-2; i++) {
			k = eluentMat.kCalc(i, currentK);
			if (k != kPrev){
				D = dz * dz / (dt * (2 * (k +1) * (k + 1)));
			}
			kPrev = k;
			dC[i] = D * (-2 * matrix[i] + matrix[i-1] + matrix[i+1]);
		}
		k = eluentMat.kCalc(zsteps-1,currentK);
		D = dz * dz / (dt * (2 * (k +1) * (k + 1)));
		dC[zsteps-1] = D * (-2 * matrix[zsteps-1] + matrix[zsteps-2] + previousMatrix[zsteps-1]); //for last position point
		for (int i = 0; i < matrix.length; i++){ // solves for Cz,t+1 on the left side of the equation
			matrix[i] = matrix[i] + (dC[i] * dt / (dz * dz));
		}
	};
	
	public void disperseEluent(){
		double[] dC = new double[matrix.length];
		double D = dz * dz / (dt * 2);
		dC[0] = D * (-1 * matrix[0] + matrix[1]); //for first position and time (using difference version of Fick's second law on p.198, dC represents the numerator on the right side of the equation)
		for (int i = 1; i < zsteps-2; i++) {
			dC[i] = D * (-2 * matrix[i] + matrix[i-1] + matrix[i+1]); //for positions from second position point to the second to last position point
		}
		dC[zsteps-1] = D * (-2 * matrix[zsteps-1] + matrix[zsteps-2] + previousMatrix[zsteps-1]);
		
		// Apply calculated dispersion
		for (int i = 0; i < matrix.length; i++){
			matrix[i] = matrix[i] + (dC[i] * dt / (dz * dz));
		}
	}
	
	public double get(int i){
		return matrix[i];
	}
	
	public double getPrevious(int i){
		return previousMatrix[i];
	}
	
	public void inject(double conc){
		matrix[0] = matrix[0] + conc;
	}
	
	public void set(int i, double conc){
		matrix[i] = conc;
	}
		
	public double kCalc(int i, boolean previous) {
		double ACN;
		ACN = previous ? previousMatrix[i] : matrix[i];
		ACN = ACN / 100;
		double k00 = neueKuss[0];
		double a = neueKuss[1];
		double B = neueKuss[2];
		k = k00*(1+a*ACN)*(1+a*ACN)*Math.exp(-B*ACN/(1+a*ACN));
		return k;
	}
	
	public double kCalc(double ACN){
		ACN = ACN / 100;
		double k00 = neueKuss[0];
		double a = neueKuss[1];
		double B = neueKuss[2];
		k = k00*(1+a*ACN)*(1+a*ACN)*Math.exp(-B*ACN/(1+a*ACN));
		return k;
	}
	
	public void fill(double fill){
		for (int i = 0; i < matrix.length; i++){
			matrix[i] = fill;
			//if (previousMatrix != null){
				previousMatrix[i] = fill;	
			//}
		}
	}
	
	public double trapz() {
		return func.trapz(dt, matrix);
	}

	public int getPeak() {
		maxIndex = 0;
		max = matrix[0];
		for (int i = 0; i < matrix.length; i++) {
			if (matrix[i] > max) {
				max = matrix[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	
	public double firstMoment(double dt){
		moment1 = 0;
		for (int i = 0; i < length; i++){
			moment1 = moment1 + (x[i] * matrix[i]);
		}
		moment1 = moment1 * dt/func.trapz(dt,matrix);
		return moment1;
	}
	
	public double secondMoment(double dt){
		double moment2 = 0;
		for (int i = 0; i < length; i++){
			moment2 = moment2 + ((x[i] - moment1) * (x[i] - moment1) * matrix[i]);
			/*if (i % 10 == 0){
				System.out.println(i);
				System.out.println(moment2);
				System.out.println(x[i]);
				System.out.println(matrix[i]);
			}*/
		}
		moment2 = moment2 * dt /func.trapz(dt,matrix);
		return moment2;
	}
	
	public void assign(int i, double conc){
		matrix[i] = conc;
	}
	
	public double getWidth(double ratio){
		double widthMax = max * ratio;
		int halfPointIndex1 = 0;
		int halfPointIndex2 = 0;
		double smallestDifference = max;
		double difference;
		boolean pastMax = false;
		for (int i = 0; i < matrix.length; i++){
			difference = Math.abs(matrix[i] - widthMax);
			if (matrix[i] == max){
				pastMax = true;
				smallestDifference = max;
			}
			if(smallestDifference > difference) {
				smallestDifference = difference;
				if (!pastMax){
					halfPointIndex1 = i;
				} else {
					halfPointIndex2 = i;
				}
			}
		}
		return (halfPointIndex2 - halfPointIndex1) * dt;		
	}

	public double getHeight(){
		return matrix[maxIndex];
	}
	
}