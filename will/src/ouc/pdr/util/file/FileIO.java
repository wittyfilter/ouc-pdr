package ouc.pdr.util.file;

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
	 
	public static void WriteToFile(String filename, List<float[]> data) throws IOException
	{
		File f = new File(filename);
		int size = data.size();
		int length = data.get(0).length;
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
}
