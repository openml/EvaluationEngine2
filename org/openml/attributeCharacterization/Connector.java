package org.openml.attributeCharacterization;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.openml.apiconnector.algorithms.Conversion;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataQuality;
import org.openml.apiconnector.xml.DataQualityUpload;
import org.openml.apiconnector.xml.DataSetDescription;
import org.openml.apiconnector.xml.DataQuality.Quality;
import org.openml.apiconnector.xstream.XstreamXmlMapping;
import com.thoughtworks.xstream.XStream;

import weka.core.Instances;

public class Connector {

	private XStream xstream;
	private OpenmlConnector openML;

	public Connector(OpenmlConnector apiconnector) throws Exception {
		openML = apiconnector;
		xstream = XstreamXmlMapping.getInstance();
	}

	public void uploadMetaFeatures(int datasetId, int evaluationEngineId) throws Exception {
		Conversion.log("OK", "Download", "Start downloading dataset: " + datasetId);
		DataSetDescription dsd = openML.dataGet(datasetId);
		Instances dataset = new Instances(new FileReader(dsd.getDataset(openML)));
		String targetFeature = dsd.getDefault_target_attribute();
		if (dataset.attribute(targetFeature) != null) {
			dataset.setClass(dataset.attribute(targetFeature));
		} else if (dataset.attribute("class") != null) {
			dataset.setClass(dataset.attribute("class"));
		} else {
			throw new Exception("Dataset " + datasetId + " has no target feature");
		}

		// parallel computation of attribute meta-features
		Conversion.log("OK", "Compute attribute meta-features", "Start computation on dataset: " + dsd.getId());
		List<Quality> qualities = new ArrayList<>();
		AttributeMetafeatures attributeMetafeatures = new AttributeMetafeatures(dataset, dsd);
		int threads = Runtime.getRuntime().availableProcessors();
		attributeMetafeatures.computeAndAppendAttributeMetafeatures(dataset, threads, qualities);

		Conversion.log("OK", "Compute attribute meta-features", "Done, uploading...");
		if (qualities.size() > 0) {
			DataQuality dataQuality = new DataQuality(datasetId, evaluationEngineId, qualities.toArray(new Quality[qualities.size()]));
			String dataQualityXml = xstream.toXML(dataQuality);
			File dataQualityFile = Conversion.stringToTempFile(dataQualityXml, "qualities_did_" + datasetId, "xml");
			DataQualityUpload dqu = openML.dataQualitiesUpload(dataQualityFile);
			Conversion.log("OK", "Compute attribute meta-features", "DONE: " + dqu.getDid());
		} else {
			Conversion.log("OK", "Compute attribute meta-features", "DONE: Nothing to upload");
		}
	}
}
