package kMeansClust;

import java.io.FileReader;
import java.util.Vector;

import com.opencsv.CSVReader;

public class Clusters extends Vector<Cluster> {
	
	int k1min, k2min;
	double distmin, totalExternalDist, totalInternalDist;

	public Clusters() throws Exception {
		super();

		throw new Error("Should not get here");

	}
	
	public Clusters(String filename) throws Exception {
		super();

		CSVReader csvReader = null;

		try {

			/* create a new CSVReader for the fileName */
			csvReader = new CSVReader(new FileReader(filename));

			/* read the headers from the csv file */
			String[] header = (csvReader.readNext()).clone();

			String[] line = null;
			/* Loop over lines in the csv file */
			while ((line = csvReader.readNext()) != null) {
				/* Initially each cluster is a single point */
				this.add(new Cluster(new Point(line)));
			}

			/* Close the writer. */
			csvReader.close();

		} catch (Exception ex) {

//			lOGGER.log(Level.SEVERE, "FileWriter for " + filename + " could not be constructed.", ex);	
			throw ex;

		}
		
		return;
	}
	
	public double findMinDist() throws Exception {
		  
	    int ncent = this.size();
	    double dist = 0;
	    
	    totalExternalDist = 0;
	    distmin = Double.MAX_VALUE;
	    k1min = 0;
	    k2min = 0;
	    
	    for (int k1=0; k1<ncent-1; k1++) {
	      for (int k2=k1+1; k2<ncent; k2++) {
	        dist = distance(k1, k2);
	        if (dist < distmin) {
	          distmin = dist;
	          k1min = k1;
	          k2min = k2;
//	          System.out.println("distmin = "+distmin+", k1min = "+k1min+", k2min = "+k2min);
	        }
	        totalExternalDist += dist;
	      }
	    }
	    
	    return distmin;
	}
	
	private double distance(int k1, int k2) throws Exception {
		  
	    int ncent = this.size();
	    if (ncent==0) throw new Error("The number of cluster is equal to 0");
	    
	    Cluster cluster1 = this.get(k1);
	    if (cluster1==null) throw new Error("cluster1 is null");
	    
	    Cluster cluster2 = this.get(k2);
	    if (cluster2==null) throw new Error("cluster2 is null");
	    
	    double[] centroid1 = cluster1.centroid;
	    if (centroid1==null) {
	    	throw new Error("centroid1 is null");
	    }
	    
	    double[] centroid2 = cluster2.centroid;
	    if (centroid2==null) {
	    	throw new Error("centroid2 is null");
	    }
	
	    int ndesc1 = centroid1.length;
	    int ndesc2 = centroid2.length;
	    
	    if (ndesc1!=ndesc2) throw new Error("centroids are not of equal length");
	    
	    double diff;
	    double dist = 0.0;
	    for (int i=0; i<ndesc1; i++) {
	        diff = centroid1[i]-centroid2[i];
	        dist += diff*diff;
	    }
	    dist = Math.sqrt(dist);
	    
	    return dist;
    
    }
	
	public double CalcTotalInternalDist() throws Exception {
		  
		int nclust = this.size();
	
		totalInternalDist = 0;
	    for (int i=0; i<nclust; i++) {
	        totalInternalDist += this.get(i).getTotalInternalDist();
	    }
	    
	    return totalInternalDist;
    
    }
	
	public void CalcPredictionAvgAndUncertainty() throws Exception {
		  
		int nclust = this.size();
	
	    for (int i=0; i<nclust; i++) {
	        this.get(i).CalcPredictionAvergeAndUncertainy();
	    }
	    
	    return;
    
    }

	public int getK1min() {
		return k1min;
	}

	public int getK2min() {
		return k2min;
	}

	public double getDistmin() {
		return distmin;
	}

	public double getTotalExternalDist() {
		return totalExternalDist;
	}

	public double getTotalInternalDistances() {
		return totalInternalDist;
	}
	
}
