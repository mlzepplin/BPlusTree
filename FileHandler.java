import java.io.*;
import java.util.ArrayList;

public class FileHandler{

	
	public  ArrayList<String> sentences;
	public File inputFile;
	public File outputFile;
	public BufferedWriter outputBufferedWriter;

	public FileHandler(String inputFilePath, String outputFilePath) throws IOException{

		//input file
		inputFile = new File(inputFilePath);

		//output file
		outputFile = new File(outputFilePath);

		//create new output file if it does not exist
		outputFile.createNewFile();

		//initialising output stream
		FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
		outputBufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));


	}

	public void writeOutput(String stringToWrite) throws IOException{
	
		//writing ot output file
		outputBufferedWriter.write(stringToWrite);
		outputBufferedWriter.newLine();
		
	}
	public void closeOutputBuffer() throws IOException{
		outputBufferedWriter.close();
	}

	public ArrayList<String> readInput() throws IOException {
	
		FileInputStream fileInputStream = new FileInputStream(inputFile);
 
		
		BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
 

		ArrayList<String> sentences = new ArrayList<String>();

		String line = null;
		while ((line = br.readLine()) != null) {

			sentences.add(line);
		}
 	
		br.close();

		return sentences;

	}

	

}