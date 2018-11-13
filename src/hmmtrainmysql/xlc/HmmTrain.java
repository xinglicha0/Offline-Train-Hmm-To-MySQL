package hmmtrainmysql.xlc;
/**
* @author xinglichao
* @author email:xinglicha0@163.com
* @version 1.0
* @version 创建时间：2018年11月7日 上午10:33:07
* @classname 类名称
* @description 类描述
*/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

class HmmTrainToMysql {
	public List<String> wordlist;      //
	public List<String> labellist;     //
	public int wordSize;               //
	public int labelSize;              //
	public double[] pi;
	public double[][] A;
	public double[][] B;
	
	HmmTrainToMysql() {
		wordlist = new ArrayList<String>();
		labellist = new ArrayList<String>();
	}
	
	
	/*
	public void creatlist(String train) throws IOException{
		System.out.println("----- 创建List -----");
		System.out.println(".......... . .  .");
		File file = new File(train);
		if(!file.exists()) {
			throw new IOException(file + "不存在！！！");
		}
		if(!file.isFile()) {
			throw new IOException(file + "不是文件！！！");
		}
		BufferedReader br = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(file)));
		String str = null;
		while((str = br.readLine()) != null) {
			String[] strarray = str.split(" ");
			for (String substr: strarray) {
				String[] tempstr = substr.split("/");
				if (tempstr.length == 2) {
					String word = tempstr[0];
					String label = tempstr[1];
					if(!wordlist.contains(word)) {
						wordlist.add(word);
					}
					if(!labellist.contains(label)) {
						labellist.add(label);
					}
				}
			}
		}
		br.close();
		wordSize = wordlist.size();
		labelSize = labellist.size();
//		listtodocument(labellist, "labellist.dat");     //写文件
//		listtodocument(wordlist, "wordlist.dat"); 
		System.out.println("----- 创建List完成 -----");
		
//		return twonum;
	}
	
	*/
	
	
	//list 写到 文件
//	public void listtodocument(List<String> list, String filename) throws IOException{
//		PrintWriter pw = new PrintWriter(filename);
//		for (String string: list) {
//			pw.print(string + " ");
//		}
//		pw.flush();
//		pw.close();
//	}
	
	public void learn(String train) throws IOException{
		
		System.out.println("----- 创建List -----");
		System.out.println(".......... . .  .");
		File file = new File(train);
		if(!file.exists()) {
			throw new IOException(file + "不存在！！！");
		}
		if(!file.isFile()) {
			throw new IOException(file + "不是文件！！！");
		}
		BufferedReader br = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(file)));
		String str = null;
		br.mark((int)file.length());//(int)file.length()
		int flag = 0;
		while((str = br.readLine()) != null) {
			System.out.println(++flag);
			String[] strarray = str.split(" ");
			for (String substr: strarray) {
				String[] tempstr = substr.split("/");
				if (tempstr.length == 2) {
					String word = tempstr[0];
					String label = tempstr[1];
					if(!wordlist.contains(word)) {
						wordlist.add(word);
					}
					if(!labellist.contains(label)) {
						labellist.add(label);
					}
				}
			}
		}
		
		wordSize = wordlist.size();
		labelSize = labellist.size();
//		listtodocument(labellist, "labellist.dat");     //写文件
//		listtodocument(wordlist, "wordlist.dat"); 
		System.out.println("----- 创建List完成 -----");
		//----------文件指针回到开头------------------
		br.reset();
		System.out.println(br.readLine());
		System.out.println("----- 开始训练 -----");
		//System.out.println(twonum[0] +"---------" + twonum[1]);
		pi = new double[labelSize];
		A = new double[labelSize][labelSize];
		B = new double[labelSize][wordSize];
		for (int i = 0; i < labelSize; i++) {
			pi[i] = 1;
			for (int j = 0; j < labelSize; j++) {
				A[i][j] = 1;
			}
			for (int k = 0; k < wordSize; k++) {
				B[i][k] = 1;
			}
		}
//		File file = new File(train);
//		if(!file.exists()) {
//			throw new IOException(file + "不存在！！！");
//		}
//		if(!file.isFile()) {
//			throw new IOException(file + "不是文件！！！");
//		}
//		BufferedReader br = new BufferedReader(
//				new InputStreamReader(
//						new FileInputStream(file)));
//		PrintWriter pw = new PrintWriter(model);
//		String str = null;
		int frontindex = -1;
		int rowpi = 0;
		while((str = br.readLine()) != null) {
			rowpi ++;
			System.out.println("--learn读取到文件的行号： " + rowpi);
			String[] strarray = str.split(" ");
			for (String substr: strarray) {
				String[] tempstr = substr.split("/");
				if (tempstr.length == 2) {
					String word = tempstr[0];
					String label = tempstr[1];
					int wordindex = wordlist.indexOf(word);
					int labelindex = labellist.indexOf(label);
					B[labelindex][wordindex] += 1;
					if (frontindex != -1) {
						A[frontindex][labelindex] += 1;
					}
					frontindex = labelindex;
				}
			}
			String firstlabel = strarray[0].split("/")[1];
		    int firstlabelindex = labellist.indexOf(firstlabel);
		   // System.out.println(firstlabel);
		    pi[firstlabelindex] += 1;
		    
		}
		System.out.println("----- 写参数到model -----");
		//计算概率   写入model文件
		int factor = 1000;
		for (int i = 0; i < labelSize; i++) {
			pi[i] = factor * pi[i] / rowpi;
		}
		double rowsumA = 0;
		//pw.println("A");
		for (int i = 0; i < labelSize; i++) {
			
			for (int j = 0; j < labelSize; j++) {
				rowsumA += A[i][j];
			}
            for (int j = 0; j < labelSize; j++) {
            	A[i][j] = factor * A[i][j] / rowsumA;
			}
            rowsumA = 0;
		}
		double rowsumB = 0;
		//pw.println("B");
		for (int i = 0; i < labelSize; i++) {
            for (int k = 0; k < wordSize; k++) {
				rowsumB += B[i][k];
			}
            for (int k = 0; k < wordSize; k++) {
            	B[i][k] = factor * B[i][k] / rowsumB;
			}
            rowsumB = 0;
		}
		br.close();
//		System.out.println("--- 文件写入完毕 训练完成 ---");
	}
	
	//训练  写 参数 到model文件中
//	public void tomodel(String allfile, String train, String model) throws IOException{
//		//int[] twonum = creatlist(train);
//		
//		int[] twonum = creatlist(allfile);
//		learn(train, model, twonum);
//		
//	}
	
	//训练的入口
//	public void pleaselearn(String filename) throws IOException{
//		double start = System.currentTimeMillis();
//		
//		String train = filename;
//		String model = "model.dat";
//		String allfile = "dataa.dat";
//		tomodel(allfile, train, model);
//		
//		
//		double end = System.currentTimeMillis();
//		System.out.println("训练用时(s)： " + (end - start) / 1000);
//	}
}

public class HmmTrain{
	public static void main(String... args) throws Exception{
		String fileName = "data.dat";
//		String fileName = "datatest.dat";
		HmmTrainToMysql httm = new HmmTrainToMysql();
		httm.learn(fileName);
		
		String strLabel = "";
		for(String str: httm.labellist) {
			strLabel += str + " ";
		}
		String strWord = "";
		for(String str: httm.wordlist) {
			strWord += str + " ";
		}
		
		String strPi = "";
		for(double db: httm.pi) {
			strPi += db + " ";
		}
		
		
		
		Configuration configuration = new Configuration();

        //不给参数就默认加载hibernate.cfg.xml文件，
        configuration.configure();
        SessionFactory factory = configuration.buildSessionFactory();
    	Session session = factory.openSession();
    	Transaction transaction = session.getTransaction();
    	transaction.begin();
    	
        Table1 table1 = new Table1();
        table1.setLabelSize(httm.labelSize);
        table1.setWordSize(httm.wordSize);
        table1.setLabelList(strLabel);
        //解决默认存储格式存不下的问题
        table1.setWordList(strWord);
        table1.setPi(strPi);
        
    	session.save(table1);
    	
    	
        for (int i = 0; i < httm.labelSize; i++) {
        	Table2 table2 = new Table2();
        	Table3 table3 = new Table3();
        	String strA = "";
        	String strB = "";
            for (int j = 0; j < httm.labelSize; j++) {
            	strA += httm.A[i][j] + " ";
			}
            for (int k = 0; k < httm.wordSize; k++) {
            	strB += httm.B[i][k] + " ";
			}
            System.out.println(i);
            table2.setRowA(strA);
            session.save(table2);
            table3.setRowB(strB);
            session.save(table3);
		}
        
    	transaction.commit();
    	session.close();
		
	}
}


