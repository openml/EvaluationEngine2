# EvaluationEngine2
Evaluation Engine for the attribute-based meta-features

Usage : java main [-id did] [-x] [-config config_string]

With did an openML dataset id.
And config_string the config string describing the settings for API interaction (server and api key).

If not given a config string, the engine tries to load it from .openml/openml.conf
If given a dataset id, the engine computes and uploads its attribute meta-features.
Else it prompts the server for either the next (in id order) or a random (if -x is set) dataset missing some attribute meta-features, then computes and uploads them.
