package will.util;

import java.util.ArrayList;
import java.util.List;


public class RotationMatrix {
	
	public static List<double[]> getTransformedAcceleration(List<double[]> accelerometer, List<double[]> orientation)
	{		
		int size = orientation.size();
		List<double[]> newAccelerometer = new ArrayList<double[]>();
		for(int i = 0; i < size; i++){
			double[] R = {0, 0, 0, 0, 0, 0, 0, 0, 0};
			double[] temp = {0, 0, 0, 0};
			
			double c0 = Math.cos(orientation.get(i)[0]*Math.PI/180);
			double c1 = Math.cos(orientation.get(i)[1]*Math.PI/180);
			double c2 = Math.cos(orientation.get(i)[2]*Math.PI/180);
			double s0 = Math.sin(orientation.get(i)[0]*Math.PI/180);
			double s1 = Math.sin(orientation.get(i)[1]*Math.PI/180);
			double s2 = Math.sin(orientation.get(i)[2]*Math.PI/180);
			R[0] = c0*c2;
			R[1] = s0*c1+s1*s2*c0;
			R[2] = s0*s1-c1*s2*c0;
			R[3] = -c2*s0;
			R[4] = c1*c0-s1*s2*s0;
			R[5] = s1*c0+c1*s2*s0;
			R[6] = s2;
			R[7] = -s1*c2;
			R[8] = c1*c2;			

			temp[0] = accelerometer.get(i)[0]*R[0] + accelerometer.get(i)[1]*R[1] + 
			accelerometer.get(i)[2]*R[2];
			temp[1] = accelerometer.get(i)[0]*R[3] + accelerometer.get(i)[1]*R[4] + 
			accelerometer.get(i)[2]*R[5];
			temp[2] = accelerometer.get(i)[0]*R[6] + accelerometer.get(i)[1]*R[7] + 
			accelerometer.get(i)[2]*R[8];
			temp[3] = orientation.get(i)[3];
			newAccelerometer.add(temp);
		}
			return newAccelerometer;
	}	
}
