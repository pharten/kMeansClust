package kMeansClust;

import java.io.FileReader;
import java.util.Vector;

import com.opencsv.CSVReader;

public class Clusters extends Vector<Cluster> {
	
	int k1min, k2min;
	double varmin, totalExternalVariance, totalClusterVariance;
	
	double[] wght = null;
	double[][] wardDist = null;

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
	
	public double findMinVar() throws Exception {
		  
	    int ncent = this.size();
	    
	    varmin = Double.MAX_VALUE;
	    double var;
	    k1min = 0;
	    k2min = 0;
	    
	    for (int k2=1; k2<ncent; k2++) {
	    	for (int k1=0; k1<k2; k1++) {
	    		var = wardDist[k2][k1];
		        if (var < varmin) {
			          varmin = var;
			          k1min = k1;
			          k2min = k2;
			    }
	    	}
		}
	    
	    return varmin;
	}
	
	public void calcWardsDistances() throws Exception {
		  
	    int ncent = this.size();
	     
	    wardDist = new double[ncent][ncent];
	    wght = new double[ncent];
	    
	    for (int k1=0; k1<ncent; k1++) {
	    	wght[k1] = this.get(k1).getClusterPoints().size();
		}
	    
	    for (int k2=1; k2<ncent; k2++) {
	    	for (int k1=0; k1<k2; k1++) {
		        wardDist[k2][k1] = (wght[k1]*wght[k2])/(wght[k1]+wght[k2])*distanceSq(k1, k2);
	    	}
		}
	    
	}
	
	public void reCalcWardsDistances(int k1new) throws Exception {
		  
	    int ncent = this.size();
	    
	    wght[k1new] = this.get(k1new).getClusterPoints().size();
	    
    	for (int k1=0; k1<k1new; k1++) {
	        wardDist[k1new][k1] = (wght[k1]*wght[k1new])/(wght[k1]+wght[k1new])*distanceSq(k1, k1new);
    	}
    	for (int k1=k1new+1; k1<ncent; k1++) {
	        wardDist[k1][k1new] = (wght[k1new]*wght[k1])/(wght[k1new]+wght[k1])*distanceSq(k1new, k1);
    	}
	    
	}
	
	private double distanceSq(int k1, int k2) throws Exception {
		  
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
	    
	    if (k1==k2) return 0.0;
	    
	    double diff;
	    double distsq = 0.0;
	    for (int i=0; i<ndesc1; i++) {
	        diff = centroid1[i]-centroid2[i];
	        distsq += diff*diff;
	    }
	    
	    return distsq;
    
    }
	
	public double CalcTotalExternalVariance() throws Exception {
		
		double[] clusterCentroid = this.firstElement().centroid;
		int ndesc = clusterCentroid.length;
		
		double[] externalCentroid = new double[ndesc];
		
		for (int j=0; j<ndesc; j++) {
			externalCentroid[j] = clusterCentroid[j];
		}
		
		int nclust = this.size();
	    for (int i=1; i<nclust; i++) {
	    	clusterCentroid = this.get(i).centroid;
	    	for (int j=0; j<ndesc; j++) {
	    		externalCentroid[j] += clusterCentroid[j];
	    	}
	    }
	    
	    double invNclust = 1.0/nclust;
		for (int j=0; j<ndesc; j++) {
			externalCentroid[j] *= invNclust;
		}
	    
		totalExternalVariance = 0;
	    for (int i=0; i<nclust; i++) {
	    	clusterCentroid = this.get(i).centroid;
	        totalExternalVariance += distanceSq(externalCentroid, clusterCentroid);
	    }
	    
	    return totalExternalVariance;
    
    }
	
	private double distanceSq(double[] externalCentroid, double[] clusterCentroid) throws Exception {
		
	    if (externalCentroid==null) throw new Exception("externalCentroid is null");
	    
	    if (clusterCentroid==null) throw new Exception("clusterCentroid is null");
	    
	    int ndesc = externalCentroid.length;
	    if (ndesc==0) throw new Exception("externalCentroid length is equal to 0");
	    
	    if (ndesc!=clusterCentroid.length) throw new Exception("Centroids are not of equal lengths");
	    
	    double diff;
	    double distsq = 0.0;
	    for (int i=0; i<ndesc; i++) {
	        diff = externalCentroid[i]-clusterCentroid[i];
	        distsq += diff*diff;
	    }
	    
	    return distsq;
    
    }
	
	public double CalcTotalClusterVariance() throws Exception {
		  
		int nclust = this.size();
	
		totalClusterVariance = 0;
	    for (int i=0; i<nclust; i++) {
	        totalClusterVariance += this.get(i).getClusterVariance();
	    }
	    
	    return totalClusterVariance;
    
    }
	
	public int getK1min() {
		return k1min;
	}

	public int getK2min() {
		return k2min;
	}

	public double getVarmin() {
		return varmin;
	}

	public double getTotalExternalVariance() {
		return totalExternalVariance;
	}

	public double getTotalClusterVariance() {
		return totalClusterVariance;
	}
	
}
