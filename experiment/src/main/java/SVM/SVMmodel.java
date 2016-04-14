package SVM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import parser.SVMParser;

public class SVMmodel {
	
	public void iterationTrain(String trainPath, String labelPath, String targetPath) throws IOException{
		SVMParser svmParser = new SVMParser();
		BufferedReader trainReader = new BufferedReader(new FileReader(new File(trainPath)));
		BufferedReader labelReader = new BufferedReader(new FileReader(new File(labelPath)));
		FileWriter fw = new FileWriter(new File(targetPath));
		HashMap<Integer, Double> mapPos = new HashMap<Integer, Double>();
		HashMap<Integer, Double> mapNeg = new HashMap<Integer, Double>();
		HashMap<Integer, String> mapTrain = new HashMap<Integer, String>();
		ArrayList<Integer> poslist = null;
		ArrayList<Integer> neglist = null;
		String labelLine = null;
		String trainLine = null;
		double score = 0.0;
		int index = 0;
		int num = 3000;
		labelLine = labelReader.readLine();
		while((labelLine = labelReader.readLine()) != null){
			if((trainLine = trainReader.readLine()) == null){
				System.err.println("file match error!");
				return;
			}
			index ++;
//			System.out.println(index);
			score = Double.parseDouble(labelLine.split(" ")[1]);
			trainLine = trainLine.replace("-1.0 ", "");
			trainLine = trainLine.replace("1.0 ", "");
			mapTrain.put(index, trainLine);
			if(score > 0.5){
				mapPos.put(index, score);
			}else if(score < 0.5){
				mapNeg.put(index, score);
			}
		}
		poslist = svmParser.minValuesOfMapValues(mapPos, num);
		neglist = svmParser.maxValuesOfMapValues(mapNeg, num);
		for(int i = 1; i <= index; i ++){
			if(poslist.contains(i)){
				fw.write("1.0 "+ mapTrain.get(i) +"\n");
			}
			if(neglist.contains(i)){
				fw.write("-1.0 "+ mapTrain.get(i) +"\n");
			}
		}
		labelReader.close();
		trainReader.close();
		fw.close();
		
	}	
	
	
	public void tfidfModel(String xmlPath, String targetPath){}
	
	public void readModelFromARFF(String arffPath, String targetPath, String labelString) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(new File(arffPath)));
		FileWriter fw = new FileWriter(new File(targetPath));
		String line = null;
		String data = null;
		String[] itemlist = null;
		String[] word = null;
		ArrayList<String> trainVector = new ArrayList<String>();
		String label = "";
		boolean dataflag = false;
		while((line = reader.readLine()) != null){
			if(line.equals("@data")){
				line = reader.readLine();
				dataflag = true;
			}
			if(dataflag && line.length() > 2){
				data = line.substring(1, line.length()-2);
				if(!data.contains(",")) 
					continue;//null data is like this: {} or {pos}
				trainVector.clear();
				itemlist = data.split(",");
				if(itemlist[0].contains(labelString)){
					label = "1.0";
					trainVector.add(label);
					for(int i = 1; i < itemlist.length; i ++){
						if(!itemlist[i].contains(" ")) 
							continue;
						word = itemlist[i].split(" ");
						String newItem = word[0]+":"+word[1];
						trainVector.add(newItem);
					}
				}else{
					label = "-1.0";
					trainVector.add(label);
					for(String item : itemlist){
						if(!item.contains(" ")) 
							continue;
						word = item.split(" ");
						String newItem = word[0]+":"+word[1];
						trainVector.add(newItem);
					}
				}
				for(String trainData : trainVector){
					fw.write(trainData + " ");
				}
				fw.write("\n");
			}
		}
		reader.close();
		fw.close();
		System.out.println("SVM model read from ARFF is DONE!");
	}
	
	public static void main(String[] args) throws Exception{
		//svm
		String modelPath = null;
		String trainPath = null;
		String testPath = null;
		String outputPath = null;
		//SVM
		SVMmodel svm = new SVMmodel();
		String basePath = "E:\\MyEclipse\\cesa\\outfiles\\dvd\\test\\";
		modelPath = "en_l_model.dat";
		trainPath = "en_l_svm.dat";
//		String basePath = "E:\\MyEclipse\\cesa\\initfiles\\test\\";
//		modelPath = "model.txt";
//		trainPath = "train.txt";
		String[] argv1 = {
			"-s", "0", 
			"-t", "2", 
			"-c", "1", 
			"-r", "0", 
			"-b", "1",
			"-d", "3", 
			"-g", "0.0",
			"-r", "0.0",
			"-n", "0.5",
			"-c", "1",
			"-e", "0.001",
			"-p", "0.1",
			"-seed", "1",
			"-h", "0",
			basePath+trainPath, 
			basePath+modelPath
		};
		svm_train.main(argv1);
		System.out.println("SVM model training is Done!");
		
		//svm predict
		testPath = "en_l_svm.dat";
		modelPath = "en_l_model.dat";
		outputPath = "result.dat";
//		testPath = "train.txt";
//		modelPath = "model.txt";
//		outputPath = "result.txt";
		String[] argv2 = {
			"-b", "1", 
			basePath+testPath, 
			basePath+modelPath, 
			basePath+outputPath
		};//usage: svm_predict [options] test_file model_file output_file
		svm_predict.main(argv2, null);
		System.out.println("SVM model prediction is Done!");
	}
}
