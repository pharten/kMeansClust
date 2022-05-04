package kMeansClust;

import java.io.FileReader;
import java.util.Vector;

import com.opencsv.CSVReader;

public class Clusters extends Vector<Cluster> {
	
	int k1min, k2min;
	double varmin, totalExternalDistSq, totalInternalDistSq, totalInternalDistSqCentroid;

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
	    k1min = 0;
	    k2min = 0;
	    
	    double[][] wardDist = new double[ncent][ncent];
	    double[] wght = new double[ncent];
	    
	    for (int k1=0; k1<ncent-1; k1++) {
	    	wght[k1] = this.get(k1).getClusterPoints().size();
		}
	    
	    for (int k1=0; k1<ncent-1; k1++) {
	    	for (int k2=0; k2<ncent-1; k2++) {
		        wardDist[k1][k2] = (wght[k1]*wght[k2])/(wght[k1]+wght[k2])*distanceSq(k1, k2);
	    	}
		}
	     
	    for (int k1=0; k1<ncent-1; k1++) {
	      for (int k2=k1+1; k2<ncent; k2++) {
	    	double totalVar = 0;
	        for (int k3=0; k3<ncent; k3++) {
	        	if (k3==k1 || k3==k2) continue;
	        	double invdenom = 1.0/(wght[k1]+wght[k2]+wght[k3]);
	        	double alpha1 = (wght[k1]+wght[k3])*invdenom;
	        	double alpha2 = (wght[k2]+wght[k3])*invdenom;
	        	double beta = -wght[k3]*invdenom;
	        	double possVar = alpha1*wardDist[k1][k3]+alpha2*wardDist[k2][k3]+beta*wardDist[k1][k2];
	        	totalVar += possVar;
	        }
	        if (totalVar < varmin) {
		          varmin = totalVar;
		          k1min = k1;
		          k2min = k2;
	//	          System.out.println("distmin = "+distmin+", k1min = "+k1min+", k2min = "+k2min);
		    }
	        throw new Error("How was this generated");
	      }
	    }
	    
	    // check to see if this is correct????
	    totalExternalDistSq = 0;
	    for (int k1=0; k1<ncent-1; k1++) {
		    for (int k2=k1+1; k2<ncent; k2++) {
	        	if (k1==k1min && k2==k2min) {
	        		totalExternalDistSq += varmin; 
	        	} else if (k1==k1min || k2==k2min) {
	        		// all distances involving cluster(k1min) or cluster(k2min)
	        		totalExternalDistSq -= wardDist[k1][k2];
	        	} else {
	        		totalExternalDistSq += wardDist[k1][k2];
	        	};
		    }
		}
	    
	    return varmin;
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
	    //dist = Math.sqrt(dist);
	    
	    return distsq;
    
    }
	
	public double CalcTotalInternalDistSq() throws Exception {
		  
		int nclust = this.size();
	
		totalInternalDistSq = 0;
	    for (int i=0; i<nclust; i++) {
	        totalInternalDistSq += this.get(i).getTotalInternalDistSq();
	    }
	    
	    return totalInternalDistSq;
    
    }
	
	public double CalcTotalInternalDistSqCentroid() throws Exception {
		  
		int nclust = this.size();
	
		totalInternalDistSqCentroid = 0;
	    for (int i=0; i<nclust; i++) {
	        totalInternalDistSqCentroid += this.get(i).getTotalInternalDistSqCentroid();
	    }
	    
	    return totalInternalDistSqCentroid;
    
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

	public double getVarmin() {
		return varmin;
	}

	public double getTotalExternalDistSq() {
		return totalExternalDistSq;
	}

	public double getTotalInternalDistSq() {
		return totalInternalDistSq;
	}
	
}
