package org.openml.attributeCharacterization;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.openml.apiconnector.algorithms.Conversion;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.settings.Config;
import org.openml.apiconnector.xml.DataUnprocessed;

public class Main {

	public static final int EVALUATION_ENGINE_ID = 2;

	public static void main(String[] args) {

		CommandLineParser parser = new GnuParser();
		Options options = new Options();
		options.addOption("id", true, "The id of the dataset/run used");
		options.addOption("config", true, "The config string describing the settings for API interaction");
		options.addOption("x", false, "Flag determining whether we should pick a random id");

		try {
			CommandLine cli = parser.parse(options, args);

			Config config;
			if (cli.hasOption("-config") == false) {
				config = new Config();
			} else {
				config = new Config(cli.getOptionValue("config"));
			}
			config.updateStaticSettings();
			// Settings.CACHE_ALLOWED = false;

			OpenmlConnector apiconnector;
			if (config.getServer() != null) {
				apiconnector = new OpenmlConnector(config.getServer(), config.getApiKey());
			} else {
				apiconnector = new OpenmlConnector(config.getApiKey());
			}
			Connector metaFeaturesConnector = new Connector(apiconnector);

			if (cli.hasOption("-id")) {

				// compute attribute meta-features for dataset_id
				int dataset_id = Integer.parseInt(cli.getOptionValue("id"));
				Conversion.log("OK", "Compute attribute meta-features", "Processing dataset " + dataset_id + " on special request. ");
				metaFeaturesConnector.uploadMetaFeatures(dataset_id, EVALUATION_ENGINE_ID);

			} else {

				// compute attribute meta-features for datasets missing them
				String mode = cli.hasOption("x") ? "random" : "normal";

				DataUnprocessed du = apiconnector.dataqualitiesUnprocessed(EVALUATION_ENGINE_ID, mode, true, AttributeMetafeatures.getAttributeMetafeatures().subList(0, 3));
				while (du != null) {

					int dataset_id = du.getDatasets()[0].getDid();
					Conversion.log("OK", "Compute attribute meta-features", "Processing dataset " + dataset_id + " as obtained from database. ");
					metaFeaturesConnector.uploadMetaFeatures(dataset_id, EVALUATION_ENGINE_ID);

					du = apiconnector.dataqualitiesUnprocessed(EVALUATION_ENGINE_ID, mode, true, AttributeMetafeatures.getAttributeMetafeatures());
				}
				Conversion.log("OK", "Compute attribute meta-features", "No more datasets to process. ");

			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		System.exit(0);
	}

}
