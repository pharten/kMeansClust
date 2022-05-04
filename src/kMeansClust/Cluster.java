package kMeansClust;

import java.util.Vector;

public class Cluster {
	
	protected double[] centroid = null;
	protected double avgPrediction;
	protected Vector<Point> clusterPoints = new Vector<Point>();
	protected double clusterVariance;
	protected double predictionAvg;
	protected double predictionUncertainty;
	
	public Cluster(Point point) throws Exception {
		super();
		
		double[] descriptorValues = point.descriptorValues;
		int ndesc = descriptorValues.length;
		
		// allocate and set separate cluster centroid initially		
		centroid = new double[ndesc];
		for (int i=0; i<ndesc; i++) {
			centroid[i] = descriptorValues[i];
		}
		
		avgPrediction = point.prediction;
		clusterPoints.add(point);
		clusterVariance = 0.0;
		
		return;
	}
	
	public Cluster(Cluster cluster1, Cluster cluster2) throws Exception {
		super();

		int nPoints1 = cluster1.clusterPoints.size();
		int nPoints2 = cluster2.clusterPoints.size();
		
		clusterPoints.addAll(cluster1.clusterPoints);
		clusterPoints.addAll(cluster2.clusterPoints);
		
		int nPoints = clusterPoints.size();
		
		if (nPoints != nPoints1 + nPoints2) throw new Error("Problem adding clusters together");
		
		double fract1 = ((double)nPoints1)/nPoints;
		double fract2 = ((double)nPoints2)/nPoints;
		
		avgPrediction = fract1*cluster1.avgPrediction + fract2*cluster2.avgPrediction;
		
		double[] centroid1 = cluster1.centroid;
		double[] centroid2 = cluster2.centroid;
		
		int ndesc1 = centroid1.length;
		int ndesc2 = centroid2.length;
		
		if (ndesc1 != ndesc2) throw new Error("Centroids are not the same length");
		
		centroid = new double[ndesc1];
		for (int i=0; i<ndesc1; i++) {
			centroid[i] = fract1*centroid1[i] + fract2*centroid2[i];
		}
		
		clusterVariance = clusterVariance();

		return;
	}
	
	private double clusterVariance() throws Exception {
		  
	    if (clusterPoints==null || clusterPoints.size()==0) throw new Error("There are no points in this cluster");
	    
	    int nPoints = clusterPoints.size();
	    
	    if (nPoints==1) return 0.0;
	    
	    double distsq;
	    double variance = 0;
	    for (int k1=0; k1<nPoints; k1++) {
	    	Point point = clusterPoints.get(k1);
	    	distsq = distanceSq(point);
	    	variance += distsq;
		}
	    
	    return variance;
    
    }
	
	public void CalcPredictionAvergeAndUncertainy() throws Exception {
		  
	    if (clusterPoints==null || clusterPoints.size()==0) throw new Error("There are no points in this cluster");
	    
	    int nPoints = clusterPoints.size();
	    double prediction;
	    double predictionSqAvg = 0;
	    for (int k1=0; k1<nPoints; k1++) {
	    	prediction = clusterPoints.get(k1).prediction;
	    	predictionAvg += prediction;
	    	predictionSqAvg += prediction*prediction;
	    }
	    predictionAvg = predictionAvg / nPoints;
	    predictionSqAvg = predictionSqAvg / nPoints;
	    
	    double stdsq = predictionSqAvg - predictionAvg*predictionAvg;
	    
	    predictionUncertainty = Math.sqrt(stdsq/nPoints);
    
    }
	
	private double distanceSq(Point point) throws Exception {
	    
	    if (point==null) throw new Error("point is null");
	    
	    double[] descriptorValues = point.descriptorValues;
	    if (descriptorValues==null) {
	    	throw new Error("point descriptorValues is null");
	    }
		
	    int ndesc = descriptorValues.length;
	    
	    double diff;
	    double distsq = 0.0;
	    for (int i=0; i<ndesc; i++) {
	        diff = descriptorValues[i]-centroid[i];
	        distsq += diff*diff;
	    }
	    
	    return distsq;
    
    }
	
	public double getClusterVariance() {
		return clusterVariance;
	}

	public Vector<Point> getClusterPoints() {
		return clusterPoints;
	}
	
	public double[] getCentroid() {
		return centroid;
	}

}
