package SVM;

import libsvm.*;
import java.io.*;
import java.util.*;

public class svm_predict {
	private static double atof(String s)
	{
		return Double.valueOf(s).doubleValue();
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}

	private static double predict(BufferedReader input, DataOutputStream output, svm_model model, int predict_probability, String resultPath) throws IOException
	{
		int correct = 0;
		int total = 0;
		double result = 0.0;
		double error = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
		double vv = 0, yy = 0, vy = 0, yv = 0; //add by hxn 2014
		double accP = 0, recallP = 0, fP = 0; //add by hxn 2014
		double accN = 0, recallN = 0, fN = 0; //add by hxn 2014

		int svm_type=svm.svm_get_svm_type(model);
		int nr_class=svm.svm_get_nr_class(model);
		double[] prob_estimates=null;

		if(predict_probability == 1)
		{
			if(svm_type == svm_parameter.EPSILON_SVR ||
			   svm_type == svm_parameter.NU_SVR)
			{
				System.out.print("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
			}
			else
			{
				int[] labels=new int[nr_class];
				svm.svm_get_labels(model,labels);
				prob_estimates = new double[nr_class];
				output.writeBytes("labels");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(" "+labels[j]);
				output.writeBytes("\n");
			}
		}
		while(true)
		{
			String line = input.readLine();
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			double target = atof(st.nextToken());
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}

			double v;
			if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
			{
				v = svm.svm_predict_probability(model,x,prob_estimates);
				output.writeBytes(v+" ");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(prob_estimates[j]+" ");
				output.writeBytes("\n");
			}
			else
			{
				v = svm.svm_predict(model,x);
				output.writeBytes(v+"\n");
			}

			
			if(v == target)
				++correct;
			error += (v-target)*(v-target);
			sumv += v;
			sumy += target;
			sumvv += v*v;
			sumyy += target*target;
			sumvy += v*target;
			++total;
			
			//add by hxn 2014, compute ACC, ReCall, F-measure
			if(target == 1.0 && v == 1.0) vv++;
			if(target == -1.0 && v == -1.0) yy++;
			if(target == -1.0 && v == 1.0) yv++;
			if(target == 1.0 && v == -1.0) vy++;
			accP =  vv/(vv+yv);
			recallP = vv/(vv+vy);
			fP = 2*(accP*recallP)/(accP+recallP);
			accN =  yy/(yy+vy);
			recallN = yy/(yy+yv);
			fN = 2*(accN*recallN)/(accN+recallN);
		}
		if(svm_type == svm_parameter.EPSILON_SVR ||
		   svm_type == svm_parameter.NU_SVR)
		{
			System.out.print("Mean squared error = "+error/total+" (regression)\n");
			System.out.print("Squared correlation coefficient = "+
				 ((total*sumvy-sumv*sumy)*(total*sumvy-sumv*sumy))/
				 ((total*sumvv-sumv*sumv)*(total*sumyy-sumy*sumy))+
				 " (regression)\n");
		}
		else{
			System.out.println("Accuracy = "+(double)correct/total*100+
					 "% ("+correct+"/"+total+") (classification)");
			System.out.println("Negtive: acc="+accN+" recall="+recallN+" f-measure="+fN);
			System.out.println("Positive: acc="+accP+" recall="+recallP+" f-measure="+fP);
			if(resultPath != null){
				FileWriter fw = new FileWriter(new File(resultPath), true);
				fw.write("Accuracy = "+(double)correct/total*100+
						 "% ("+correct+"/"+total+") (classification)\n");
				fw.write("Negtive: acc="+accN+" recall="+recallN+" f-measure="+fN+"\n");
				fw.write("Positive: acc="+accP+" recall="+recallP+" f-measure="+fP+"\n");
				fw.close();
			}
			result = (double)correct/total;
		}
		return result;
	}

	private static void exit_with_help()
	{
		System.err.print("usage: svm_predict [options] test_file model_file output_file\n"
		+"options:\n"
		+"-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n");
		System.exit(1);
	}

	public static double main(String argv[], String resultPath) throws IOException
	{
		int i, predict_probability=0;
		double result = 0.0;
		// parse options
		for(i=0;i<argv.length;i++)
		{
			if(argv[i].charAt(0) != '-') break;
			++i;
			switch(argv[i-1].charAt(1))
			{
				case 'b':
					predict_probability = atoi(argv[i]);
					break;
				default:
					System.err.print("Unknown option: " + argv[i-1] + "\n");
					exit_with_help();
			}
		}
		if(i>=argv.length-2)
			exit_with_help();
		try 
		{
			BufferedReader input = new BufferedReader(new FileReader(argv[i]));
			DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(argv[i+2])));
			svm_model model = svm.svm_load_model(argv[i+1]);
			if(predict_probability == 1)
			{
				if(svm.svm_check_probability_model(model)==0)
				{
					System.err.print("Model does not support probabiliy estimates\n");
					System.exit(1);
				}
			}
			else
			{
				if(svm.svm_check_probability_model(model)!=0)
				{
					System.out.print("Model supports probability estimates, but disabled in prediction.\n");
				}
			}
			result = predict(input,output,model,predict_probability, resultPath);
			input.close();
			output.close();
		} 
		catch(FileNotFoundException e) 
		{
			exit_with_help();
		}
		catch(ArrayIndexOutOfBoundsException e) 
		{
			exit_with_help();
		}
		return result;
	}
}
