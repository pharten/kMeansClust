package kMeansClust;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KMeans {

	/* Default filenames */
	static String propFilename = System.getProperty("user.dir") + "\\kMeans.properties";
	static String keyFilename = System.getProperty("user.dir") + "\\kMeans.key";
	// static String csvFilename = System.getProperty("user.dir") + "\\kMeans.csv";
	static String logFilename = System.getProperty("user.dir") + "\\kMeans.log";

	static String csvFilename = "./data/Multivariate_Imputed_Numerical_Columns.csv";
	//static String csvFilename = "./data/LD50_training_set-2d.csv";
	static String helpString = "User options:\njava -jar kMeans -h\njava -jar kMeans\njava -jar kMeans propFilename\n";

	protected Clusters clusters = null;
	protected Clusters clusters_train = new Clusters();
	protected Clusters clusters_test = new Clusters();
	protected double[] avgDescValues = null;

	/* Create an object of type Logger so we can log error or warning messages. */
	protected static Logger LOGGER = Logger.getLogger("kMeans", null);
	protected int k1min, k2min;
	protected double distmin;

	public static void main(String[] args) {
		try {

			if (testArgs(args)) {

				KMeans kMeans = new KMeans();

			}

		} catch (Exception ex) {

			ex.printStackTrace(); // This is the only case when the stack trace is sent to the console.

		}

	}

	public static void main() {
		try {

			KMeans kMeans = new KMeans();

		} catch (Exception ex) {

			ex.printStackTrace(); // This is the only case when the stack trace is sent to the console.

		}

	}

	private static boolean testArgs(String[] args) throws Exception {

		if (args == null || args.length == 0) { // Use default properties file.

			// System.out.println("Using default properties file: " + filename);

		} else if (args.length == 1 && args[0].trim().matches("-h")) { // respond with user options

			System.out.print(helpString);
			return false;

		} else if (args.length == 1) { // Using command-line entered properties file

			/*
			 * key file must be in same folder as properties file, but with "key" extension
			 */
			propFilename = args[0].trim();
			keyFilename = propFilename.substring(0, propFilename.lastIndexOf('.') + 1) + "key";

		} else { // Something is wrong

			throw new Exception("Invalid options are used");

		}

		return true;

	}

	public KMeans() throws Exception {

		startLogger();
		
		Clusters clusters = new Clusters(csvFilename);
		System.out.println("Number of cluster = "+clusters.size());
		
		randSplit(0.1, clusters);
		System.out.println("Number of training clusters = "+clusters_train.size());
		System.out.println("Number of testing clusters = "+clusters_test.size());
		
		clusters.clear();
		clusters = (Clusters) ObjectCloner.deepCopy(clusters_train);
		
		avgDescValues = normalize(clusters);
		
		// wardsDistances is an nclusters x nclusters triangle showing (wards)distancesq between clusters
		clusters.calcWardsDistances();
		
		int niter = clusters.size()-1;
		//niter = 958;
		for (int iter = 0; iter <= niter; iter++) {
			
			double extVariance = clusters.CalcTotalExternalVariance();
			double intVariance = clusters.CalcTotalClusterVariance();
			//System.out.println("iter = "+iter+" extVariance = "+extVariance+", intVariance = "+intVariance);
			if (iter==niter) break;
			if (intVariance>extVariance) break;
			
			double varmin = clusters.findMinVar();
			int k1min = clusters.getK1min();
			int k2min = clusters.getK2min();
			int nPoints1 = clusters.get(k1min).clusterPoints.size();
			int nPoints2 = clusters.get(k2min).clusterPoints.size();
			System.out.println("nclusters = "+clusters.size()+", varmin = "+varmin+", nPoints for cluster("+k1min+") = "+nPoints1+", cluster("+k2min+") = "+nPoints2);
			if (nPoints1+nPoints2>clusters.size()) break;
			//if (nPoints1+nPoints2>50) break;
			//if ((varmin>(extVarianceBefore-extVariance)) && (iter>0) ) break;
			//if the increase in internal variance becomes > the decrease in external variance then break.
			//if (((intVariance-intVarianceBefore)>(extVarianceBefore-extVariance)) && (iter>0) ) break;
			Cluster clustersJoined = new Cluster(clusters.get(k1min),clusters.get(k2min));

			if (k2min==clusters.size()-1) {
				clusters.set(k1min, clustersJoined);
				clusters.remove(clusters.size()-1);  // remove last element
				clusters.reCalcWardsDistances(k1min);  // recalculate distances
			} else {
				clusters.set(k1min, clustersJoined);
				clusters.set(k2min, clusters.lastElement());
				clusters.remove(clusters.size()-1); // remove last element
				clusters.reCalcWardsDistances(k1min);  // recalculate distances
				clusters.reCalcWardsDistances(k2min);  // recalculate distances
			}

			//System.out.println("Number of clusters = "+clusters.size());
		}
		
		int nclusters = clusters.size();
		for (int i=0; i<nclusters; i++) {
			Cluster cluster = clusters.get(i);
			cluster.CalcPredictionAvergeAndUncertainy();
			int nPoints = cluster.clusterPoints.size();
			System.out.println(i+") nPoints = "+nPoints+", "+cluster.avgPrediction+", "+cluster.predictionAvg+" +/- "+cluster.predictionUncertainty);
		}
		
		// First normalize all descriptors in original training clusters by avgDescValues
		clusters_train.normalize(avgDescValues);
		
		// Calculate R2 for training clusters predictions
		double Rsq = clusters_train.calculateR2(clusters);
		System.out.println(" Training Rsq = "+Rsq);
		
		// First normalize all descriptors in test clusters by avgDescValues
		clusters_test.normalize(avgDescValues);
		
		// Calculate R2 for test clusters predictions
		Rsq = clusters_test.calculateR2(clusters);
		System.out.println(" Testing Rsq = "+Rsq);

	}

	private void startLogger() throws IOException {

		/*
		 * Initialize log file information. Throw IOException and/or SecurityException
		 * if creation of file handler was not successful.
		 */
		LOGGER.setLevel(Level.INFO);
		if (LOGGER.getUseParentHandlers()) {
			LOGGER.addHandler(new FileHandler(logFilename));
			LOGGER.setUseParentHandlers(false); // This will prevent LOGGER from printing messages to the console.
		}

	}

	private double[] normalize(Clusters clusters) throws Exception {
		
		/*
		 * Normalize descriptors while there is still one point in each cluster
		 * 
		 */
		Cluster cluster = clusters.firstElement();
		Vector<Point> points = cluster.clusterPoints;
		Point point = points.firstElement();
		double[] descriptorValues = point.descriptorValues;
		int ndesc = point.descriptorValues.length;
		
		double[] avgDescValues = new double[ndesc];
		for (int j=0; j<ndesc; j++) {
			avgDescValues[j] = 0.0;
		}
		
		int nclust = clusters.size();
		for (int i=0; i<nclust; i++) {
			cluster = clusters.get(i);
			points = cluster.getClusterPoints();
			if (points.size()!=1) throw new Error("Should currently be only 1 point in cluster");
			point = points.firstElement();
			descriptorValues = point.descriptorValues;
			for (int j=0; j<ndesc; j++) {
				avgDescValues[j] += descriptorValues[j];	
			}
		}
		
		// get average values of descriptors
		for (int j=0; j<ndesc; j++) {
			avgDescValues[j] /= nclust;
		}
		
		// normalize all descriptor values by average values
		for (int i=0; i<nclust; i++) {
			cluster = clusters.get(i);
			points = cluster.getClusterPoints();
			if (points.size()!=1) throw new Exception("Should currently be only 1 point in cluster");
			point = points.firstElement();
			descriptorValues = point.descriptorValues;
			for (int j=0; j<ndesc; j++) {
				if (avgDescValues[j] < Float.MIN_VALUE) avgDescValues[j] = Float.MIN_VALUE;
				descriptorValues[j] /= avgDescValues[j];
			}
			double[] clusterCentroid = cluster.getCentroid();
	        for (int j=0; j<ndesc; j++) {
	        	clusterCentroid[j] = descriptorValues[j];
	        }
		}
		
		return avgDescValues;
		
	}
	
	private void randSplit(double fracTests, Clusters clusters) throws Exception {
		
		/*
		 * Randomly split clusters into training clusters and testing clusters
		 * 
		 */
		
		Random r = new Random(0);
		int nclust = clusters.size();
		for (int i=0; i<nclust; i++) {
			if (r.nextDouble()<fracTests) {
				clusters_test.add(clusters.get(i));
			} else {
				clusters_train.add(clusters.get(i));	
			};
		}
		
	}

}
