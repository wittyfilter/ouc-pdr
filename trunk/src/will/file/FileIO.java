package will.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class FileIO {
	
	/**
	 * Write to file
	 * @param filename
	 * @param data
	 * @throws IOException
	 */
	 
	public static void WriteToFile(String filename, List<double[]> data, int length) throws IOException
	{
		File f = new File(filename);
		int size = data.size();
		FileOutputStream fileOS = new FileOutputStream(f, true);
		if(length == 1) {
			for(int i = 0; i < size; i++) {
			fileOS.write((String.valueOf(data.get(i)[0] + "\n").getBytes()));
			}
			fileOS.close();
		}
		else
		{
		for(int i = 0; i < size; i++)
		{
			for(int j = 0; j < length-1; j++) {
				fileOS.write((String.valueOf(data.get(i)[j] + " ").getBytes()));
			}
			fileOS.write(String.valueOf(data.get(i)[length-1] + "\n").getBytes());		
		}
		fileOS.close();
		}
	}
	
	public static void WriteToFile2(String filename, List<Double> data) throws IOException
	{
		File f = new File(filename);
		FileOutputStream fileOS = new FileOutputStream(f, true);
		int length = data.size();
		for(int i = 0; i < length; i++)
		{
			fileOS.write((String.valueOf(data.get(i) + "\n").getBytes()));
		}	
			fileOS.close();
	}
	
	/**
	 * Delete File
	 * @param fileName
	 */
	public static void DeleteFile(String fileName) 
	{
		File delFile = new File(fileName);
		delFile.delete();
	}
}
