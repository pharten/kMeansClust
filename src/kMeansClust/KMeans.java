package kMeansClust;

import java.io.IOException;
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
	static String helpString = "User options:\njava -jar kMeans -h\njava -jar kMeans\njava -jar kMeans propFilename\n";

	protected Vector<Cluster> clusters = new Vector<Cluster>();

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
		
		normalize(clusters);

		double extVarianceBefore=0;
		int niter = clusters.size()-1;
		for (int iter = 0; iter < niter; iter++) {
			double varmin = clusters.findMinVar();
			int k1min = clusters.getK1min();
			int k2min = clusters.getK2min();
			double extVariance = clusters.CalcTotalExternalVariance();
			double intVariance = clusters.CalcTotalClusterVariance();
			System.out.println("varmin = "+varmin+", "+k1min+", "+k2min+", extVariance = "+extVariance+", intVariance = "+intVariance);
			if (intVariance>extVariance) break;
			if ((varmin>(extVarianceBefore-extVariance)) && (iter>0) ) break;
			Cluster clustersJoined = new Cluster(clusters.get(k1min),clusters.get(k2min));
			clusters.set(k1min, clustersJoined);
			clusters.set(k2min, clusters.lastElement());
			clusters.remove(clusters.size()-1);
			System.out.println("Number of clusters = "+clusters.size());
			extVarianceBefore = extVariance;
		}
		
		for (int i=0; i<clusters.size(); i++) {
			Cluster cluster = clusters.get(i);
			cluster.CalcPredictionAvergeAndUncertainy();
			int nPoints = cluster.clusterPoints.size();
			System.out.println(i+") nPoints = "+nPoints+", "+cluster.avgPrediction+", "+cluster.predictionAvg+" +/- "+cluster.predictionUncertainty);
		}

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

	private void normalize(Clusters clusters) throws Exception {
		
		/*
		 * Normalize descriptors while there is still one point in each cluster
		 * 
		 */
		Cluster cluster = clusters.firstElement();
		Vector<Point> points = cluster.clusterPoints;
		Point point = points.firstElement();
		double[] descriptorValues = point.descriptorValues;
		int ndesc = point.descriptorValues.length;
		
		double[] centroid = new double[ndesc];
		for (int j=0; j<ndesc; j++) {
			centroid[j] = 0.0;
		}
		
		int nclust = clusters.size();
		for (int i=0; i<nclust; i++) {
			cluster = clusters.get(i);
			points = cluster.getClusterPoints();
			if (points.size()!=1) throw new Error("Should currently be only 1 point in cluster");
			point = points.firstElement();
			descriptorValues = point.descriptorValues;
			for (int j=0; j<ndesc; j++) {
				centroid[j] += descriptorValues[j];	
			}
		}
		
		// get average values of descriptors
		for (int j=0; j<ndesc; j++) {
			centroid[j] /= nclust;
		}
		
		// normalize all descriptor values by average values
		for (int i=0; i<nclust; i++) {
			cluster = clusters.get(i);
			points = cluster.getClusterPoints();
			if (points.size()!=1) throw new Exception("Should currently be only 1 point in cluster");
			point = points.firstElement();
			descriptorValues = point.descriptorValues;
			for (int j=0; j<ndesc; j++) {
				if (centroid[j]!=0.0) {
					descriptorValues[j] /= centroid[j];
				} else {
					throw new Exception("centroid["+j+"] = 0.0");
				}
			}
			double[] clusterCentroid = cluster.getCentroid();
	        for (int j=0; j<ndesc; j++) {
	        	clusterCentroid[j] = descriptorValues[j];
	        }
		}
		
	}

}
