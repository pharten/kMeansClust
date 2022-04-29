package kMeansClust;

import java.util.Vector;

public class Cluster {
	
	protected double[] centroid = null;
	protected double avgPrediction;
	protected Vector<Point> clusterPoints = new Vector<Point>();
	protected double totalInternalDist;
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
		totalInternalDist = 0;
		
		return;
	}
	
	public Cluster(Cluster cluster1, Cluster cluster2) throws Exception {
		super();

		int nPoints1 = cluster1.clusterPoints.size();
		int nPoints2 = cluster2.clusterPoints.size();
		
		clusterPoints.addAll(cluster1.clusterPoints);
		clusterPoints.addAll(cluster2.clusterPoints);
		
		int nPoints = clusterPoints.size();
		
		if (nPoints != nPoints1+ nPoints2) throw new Error("Problem adding clusters together");
		
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
		
		totalInternalDist = totalInternalDistance();

		return;
	}
	
	private double totalInternalDistance() throws Exception {
		  
	    if (clusterPoints==null || clusterPoints.size()==0) throw new Error("There are no points in this cluster");
	    
	    int nPoints = clusterPoints.size();
	    
	    double dist;
	    double totalDist = 0;
	    for (int k1=0; k1<nPoints-1; k1++) {
	    	Point point1 = clusterPoints.get(k1);
		    for (int k2=k1+1; k2<nPoints; k2++) {
		    	Point point2 = clusterPoints.get(k2);
		        dist = distance(point1, point2);
		        totalDist += dist;
		    }
		}
	    
	    return totalDist;
    
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
	
	private double distance(Point point1, Point point2) throws Exception {
	    
	    if (point1==null) throw new Error("cluster1 is null");
	    if (point2==null) throw new Error("cluster2 is null");
	    
	    double[] descriptorValues1 = point1.descriptorValues;
	    if (descriptorValues1==null) {
	    	throw new Error("point1 descriptorValues is null");
	    }
	    
	    double[] descriptorValues2 = point2.descriptorValues;
	    if (descriptorValues2==null) {
	    	throw new Error("point2 descriptorValues is null");
	    }
		
	    int ndesc = descriptorValues1.length;
	    
	    double diff;
	    double dist = 0.0;
	    for (int i=0; i<ndesc; i++) {
	        diff = descriptorValues1[i]-descriptorValues2[i];
	        dist += diff*diff;
	    }
	    dist = Math.sqrt(dist);
	    
	    return dist;
    
    }

	public double getTotalInternalDist() {
		return totalInternalDist;
	}

	public Vector<Point> getClusterPoints() {
		return clusterPoints;
	}
	
	public double[] getCentroid() {
		return centroid;
	}

}
