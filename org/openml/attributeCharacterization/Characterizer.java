package org.openml.attributeCharacterization;

import java.util.Map;

import weka.core.Instances;

public abstract class Characterizer {

	public abstract String[] getIDs();

	public abstract Map<String, Double> characterize(Instances instances) throws Exception;

	public int getNumMetaFeatures() {
		return getIDs().length;
	}
}
