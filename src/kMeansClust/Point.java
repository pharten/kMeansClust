package kMeansClust;

import java.io.Serializable;

public class Point extends Object implements Serializable, Cloneable {
	
	protected double[] descriptorValues = null;
	protected double prediction;
	
	public Point(String[] values) throws Exception {
		super();
		int length = values.length;
		int descriptorValuesLength = length-2;
		descriptorValues = new double[descriptorValuesLength];
		for (int i=0; i<descriptorValuesLength; i++) {
			if (values[i]!=null && values[i]!="") {
				descriptorValues[i] = Double.parseDouble(values[i]);
			} else {
				throw new Exception("This descriptor value is null or blank");
				//descriptorValues[i] = 0.0;
			}
		}
		int predictionPosition = descriptorValuesLength;
		if (values[predictionPosition]!=null && values[predictionPosition]!="") {
			prediction = Double.parseDouble(values[predictionPosition]);
		} else {
			throw new Exception("This observed prediction is null or blank");
			//prediction = 0.0;
		}
	}

	public double[] getDescriptorValues() {
		return descriptorValues;
	}

	public double getPrediction() {
		return prediction;
	}

	public void setDescriptorValues(double[] descriptorValues) {
		this.descriptorValues = descriptorValues;
	}

	public void setPrediction(double prediction) {
		this.prediction = prediction;
	}

}
