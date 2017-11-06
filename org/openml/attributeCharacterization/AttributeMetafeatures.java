package org.openml.attributeCharacterization;

import org.openml.apiconnector.xml.DataQuality;
import org.openml.apiconnector.xml.DataSetDescription;
import weka.core.Instances;
import java.util.*;
import java.util.concurrent.*;

public class AttributeMetafeatures {

	public static List<String> getAttributeMetafeatures() {
		return Arrays.asList(AttributeCharacterizer.ids);
	}

	private List<AttributeCharacterizer> attributeCharacterizers = new ArrayList<>();

	public AttributeMetafeatures(Instances dataset, DataSetDescription dsd) {

		// create the list of characterizers for the interesting attributes
		for (int indexNr = 0; indexNr < dataset.numAttributes(); indexNr++) {
			boolean processAtt = true;
			if (dsd.getRow_id_attribute() != null && dataset.attribute(dsd.getRow_id_attribute()) != null
					&& dataset.attribute(dsd.getRow_id_attribute()).index() == indexNr) {
				processAtt = false;
			}
			if (dsd.getIgnore_attribute() != null) {
				for (String att : dsd.getIgnore_attribute()) {
					if (dataset.attribute(att) != null && dataset.attribute(att).index() == indexNr) {
						processAtt = false;
					}
				}
			}
			if (processAtt) {
				AttributeCharacterizer attributeCharacterizer = new AttributeCharacterizer(indexNr);
				attributeCharacterizers.add(attributeCharacterizer);
			}
		}

	}

	public void computeAndAppendAttributeMetafeatures(Instances fullDataset, int threads, List<DataQuality.Quality> result) throws Exception {

		ExecutorService service = Executors.newFixedThreadPool(threads);

		List<Future<List<DataQuality.Quality>>> futures = new ArrayList<>();
		for (final AttributeCharacterizer attributeCharacterizer : attributeCharacterizers) {
			Callable<List<DataQuality.Quality>> callable = () -> {
				// Conversion.log("OK", "Computing meta-features","thread " + Thread.currentThread().getName() + " for attribute " +
				// attributeCharacterizer.getIndex());
				List<DataQuality.Quality> output = new ArrayList<>();
				Map<String, QualityResult> qualities = characterize(fullDataset, attributeCharacterizer);
				output.addAll(qualityResultToList(qualities));
				return output;
			};
			futures.add(service.submit(callable));
		}

		service.shutdown();

		for (Future<List<DataQuality.Quality>> future : futures) {
			result.addAll(future.get());
		}
	}

	public List<DataQuality.Quality> qualityResultToList(Map<String, QualityResult> map) {
		List<DataQuality.Quality> result = new ArrayList<>();
		for (String quality : map.keySet()) {
			QualityResult qualityResult = map.get(quality);
			result.add(new DataQuality.Quality(quality, qualityResult.value, null, null, qualityResult.index));
		}
		return result;
	}

	public Map<String, QualityResult> characterize(Instances instances, AttributeCharacterizer characterizer) {
		Map<String, Double> values = characterizer.characterize(instances);
		Map<String, QualityResult> result = new HashMap<>();
		values.forEach((s, v) -> result.put(s, new QualityResult(v, characterizer.getIndex())));
		return result;
	}

	class QualityResult {
		private final Double value;
		private final Integer index;

		public QualityResult(Double value, Integer index) {
			this.value = value;
			this.index = index;
		}
	}
}
