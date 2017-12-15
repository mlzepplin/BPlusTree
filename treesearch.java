import java.io.*;
import java.util.*;

public class treesearch{


	public static void main(String args[]) throws IOException{

			//this initialises input and output files and streams
			FileHandler fh = new FileHandler(args[0],"output_file.txt");

			BPlusTree bpt;
			
			//TODO , implement something so that filePath can be 
			//out of directory as well
			//OR NOT
			ArrayList<String> sentences;
			sentences = fh.readInput();

			//first line in input file is the order int
			int order  = Integer.parseInt(sentences.get(0));
		
			//initialise bplustree
			bpt = new BPlusTree(order);
			

			for(int i=1;i<sentences.size();i++){

				//represents a line gotten from the input file
				String line = sentences.get(i);
				
				//tokentised the line based on the following delimiters
				StringTokenizer st = new StringTokenizer(line,"(,)");

				//operation is either Insert or Search
				String operation = st.nextToken();

				//parameter list will be used to provide inputs and
				//distinguish between different types of searches
				Vector<String> parameterList = new Vector<String>();


				while(st.hasMoreTokens()){
	            	parameterList.add(st.nextToken());
	   		     }

	   		     if(operation.equals("Insert")){
	   		     	
	   		     	//inserting key value pair into the tree for insert op
	   		     	bpt.insertPair(Double.parseDouble(parameterList.get(0)),parameterList.get(1));
	   		     	

	   		     }
	   		     else if(operation.equals("Search")){

	   		     	if(parameterList.size()==1){

	   		     		String toWrite;
	   		     		toWrite = bpt.getSearch(Double.parseDouble(parameterList.get(0)),bpt.root);
	   		     			   		     		//writing the output of search on one key, to the file
	   		     		fh.writeOutput(toWrite);
	   		     	}
	   		     	else if(parameterList.size()==2){
	   		     		String toWrite;
	   		     		try{
	   		     		 toWrite = bpt.rangeSearch(Double.parseDouble(parameterList.get(0)),Double.parseDouble(parameterList.get(1)));
	   		     		 //writing the output of range search to the output file
	   		     		fh.writeOutput(toWrite);
	   		     		}catch(NullPointerException e){}

	   		     	}	
	   		     	else{
	   		     		//will be intered of there is a discrepency in the number of 
	   		     		//search parameters
	   		     		System.out.println("wrong number of parameters in search");
	   		     		// System.out.println(parameterList.size());
	   		     		// for(int s=0;s<parameterList.size();s++)
	   		     		// 	System.out.println(parameterList.get(s));
	   		     	}
	   		     }
				
				

			}
			
			fh.closeOutputBuffer();
			// System.out.println("LIST-++++++____++++");
			// bpt.printLL();
			


	}
}