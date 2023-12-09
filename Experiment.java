import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import spmf.*;
public class Experiment {
	static HashMap<String,Integer> datasets = new HashMap<String,Integer>();
	static String output = "Codes/java/wafi/res.txt";
	
	public static void main(String []args) throws IOException{
				
		
		//boolean bwafi = false;
		
		//boolean bfpgrowth = false;

	//	boolean bdeclat = false;

		//boolean bprepost = true;
		//datasets.put("c:\\java codes\\wafi\\datasets\\mushrooms.txt", 8416);	
		//datasets.put("c:\\java codes\\wafi\\datasets\\accidents.txt",340183);
		//datasets.put("c:\\java codes\\wafi\\datasets\\BMS1_itemset_mining.txt",59602);
		//datasets.put("c:\\java codes\\wafi\\datasets\\BMS2_itemset_mining.txt",77512);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\c73d10k.txt",10000);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\c20d10k.txt",10000);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\retail.txt",88162);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\t25i10d10k.txt",9976);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\t20i6d100k.txt",99922);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\pumsb.txt",49046);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\kddcup99.txt",1000000);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\foodmartFIM.txt",4141);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\webdocs.dat",1692083);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\chess.txt",3196);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\connect.txt",67557);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\kosarak.dat",990002);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\T40I10D100K.dat",100000);
		//datasets.put( "c:\\java codes\\wafi\\datasets\\T10I4D100K.dat",100000);
		datasets.put( "c:\\java codes\\wafi\\datasets\\in_16_100000.txt",100000);
		
		Locale.setDefault(new Locale("en", "US"));
	
		int pas = 100;
		int max = 1;
		
		long time = 0 , mem = 0;
		boolean error = false;
		double minsupp = 0;
		//String algo ="";
		File theDir = new File("d://experiments//");

		// if the directory does not exist, create it
		if (!theDir.exists()) {
		    System.out.println("creating directory: " + "d://experiments//");
		    boolean result = false;
		    try{
		        theDir.mkdir();
		        result = true;
		    } 
		    catch(SecurityException se){
		        //handle it
		    }        
		    if(result) {    
		        System.out.println("DIR created");  
		    }
		}

		for (int t = 0; t <= 0; t++)   // Pour tous les algos
		for (String ds:datasets.keySet())  // pour chaque dataset
			{
			BufferedWriter writer1 = new BufferedWriter(new FileWriter("d://experiments//"+t+"_"+ds.substring(28, ds.length()-4)+"_exp_time.txt"));
			BufferedWriter writer2 = new BufferedWriter(new FileWriter("d://experiments//"+t+"_"+ds.substring(28, ds.length()-4)+"_exp_mem.txt"));

			writer1.write("minsupp time\n");
			writer2.write("minsupp memory\n");
				int i = pas-10;
				boolean possible = true;
				while(  i >= 1 && possible)   // répéter le benchmark max fois
				{
					time = 0;
					mem = 0;
					long delta = -1;
					minsupp =  (double) i/pas;
					for  (int j = 1; j <= max; j++)
					{
						System.out.println("Algo : "+t+" DataSet : "+ds.substring(28, ds.length()-4)+" Supp : "+String.format( "%.2f",minsupp)+" It : "+j);
						//stop = (double)minsupp/datasets.get(ds);
						
						
						
						if (t == 0)
						{	Dataset database = new Dataset(ds);
						//algo = algoDEclat.toString();
						//database.loadFile(ds);//algo = algoPrePost.toString();

							AlgoLCMMax algo = new AlgoLCMMax();
							//algo.setUsePrePostPlus(true);
							try{
								algo.runAlgorithm(minsupp, database,output);
							}
							catch (OutOfMemoryError e) {
							    e.printStackTrace();
								error = true;
								algo = null;
								break;
							  } 
							delta = algo.getEndTime() -  algo.getStartTime();
							time += delta;
							mem += MemoryLogger.getInstance().getMaxMemory();
							algo = null;
						}
						/*if (t == 0)
						{	//algo = algoPrePost.toString();
							PrePost  algo = new PrePost();
							//algo.setUsePrePostPlus(true);
							try{
								algo.runAlgorithm(ds, minsupp, output);
							}
							catch (java.lang.OutOfMemoryError e) {
							    e.printStackTrace();
								error = true;
								algo = null;
								break;
							  } 
							delta = algo.getEndTime()-  algo.getstartTimestamp();
							time += delta;
							mem += MemoryLogger.getInstance().getMaxMemory();
							algo = null;
						}*/
						else if (t == 1)
						{
							//algo = algoFPGrowth.toString();
								AlgoFPGrowth algo = new AlgoFPGrowth();
							  try {
									  algo.runAlgorithm(ds,output,minsupp);
								  } catch (OutOfMemoryError e) {
								    e.printStackTrace();
									error = true;
									algo = null;
									break;
								  }
							  	delta = algo.getEndTime()-  algo.getStartTimestamp();
								time += delta;
								mem += MemoryLogger.getInstance().getMaxMemory();
								algo = null;
						} 
						else	if (t == 2)
						{
							TransactionDatabase database = new TransactionDatabase();
							//algo = algoDEclat.toString();
							AlgoDEclat algo = new AlgoDEclat();
							database.loadFile(ds);
							 try {
								 algo.runAlgorithm(output, database, minsupp,false);
							 }catch (OutOfMemoryError e) {
								    e.printStackTrace();
									error = true;
									algo = null;
									break;
								  }
							 	 delta = algo.getEndTime()-  algo.getstartTimestamp();
							 	 time += delta;
							 	 mem += MemoryLogger.getInstance().getMaxMemory();
							 	 algo = null;	
						}
						if ( t== 3)
						{
							wafi algo = new wafi(ds,output);
							//algo = algoWafi.toString();
							try{
								algo.discoverFI(ds, minsupp, output);
							}
							catch (OutOfMemoryError e) {
							    e.printStackTrace();
							   
								error = true;
								algo = null;
								break;
							  }
							 delta = algo.getEndTime()-  algo.getstartTimestamp();
							 time += delta;
							 mem += MemoryLogger.getInstance().getMaxMemory();
							 algo = null;
						}
						}
						if (i >= 20) i -= 10;
						else 
							i -= 1;
						if (error) 
						{
							writer1.write(String.format( "%.2f", minsupp ) +" "+-1+"\n");
							writer2.write(String.format( "%.2f", minsupp ) +" "+-1+"\n");
							error = false;
							System.gc();
							possible = false;
							continue;
						}
						else   // écrire la moyenne des résultats
						{
							writer1.write(String.format( "%.2f",minsupp) +" "+String.format( "%.2f",(double)time/(max*1000))+"\n");
							writer2.write(String.format( "%.2f",minsupp) +" "+String.format( "%.2f",(double)mem/max)+"\n");
							if (delta > 300000)
							{
								possible = false;
								continue;	
							}
							System.gc();
						}
					}
					writer1.close();
					writer2.close();
			}
			}
}

