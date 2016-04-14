package cn.edu.nlsde.experiment;

import de.bwaldvogel.liblinear.Predict;
import de.bwaldvogel.liblinear.Train;

/**
 * Created by zwj on 2016/4/11.
 */
public class ZWJExperiment {
    
    /**
     * String basePath = paths[0]; String modelPath = paths[1]; String trainPath
     * = paths[2]; String predictPath = paths[3]; String outputPath = paths[4];
     */
    private static double runlibLinear(String[] args, String resultPath,
                                       boolean train) throws Exception {
        if (train) {
            String[] argv1 = {"-s", "0", "-c", "1.0", args[0] + args[2], // train
                    args[0] + args[1] // model
            };
            Train.main(argv1);
            System.out.println("SVM model training is Done!  " + args[0]
                    + args[2]);
        }
        /*
         * String[] argv2 = { "-b", "1", args[0] + args[3] + randNum, // test
		 * args[0] + args[1], // model args[0] + args[4] // output };// usage:
		 * svm_predict [options] test_file model_file output_file
		 */
        String[] argv2 = {"-b", "1", args[0] + args[3], // test
                args[0] + args[1], // model
                args[0] + args[4] // output
        };// usage: svm_predict [options] test_file model_file output_file

        double result = Predict.main(argv2, resultPath);
        System.out.println("SVM model prediction is Done!  " + args[0]
                + args[3]);
        return result;
    }
}
