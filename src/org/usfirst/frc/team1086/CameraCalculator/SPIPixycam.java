package org.usfirst.frc.team1086.CameraCalculator;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.SPI.Port;

public class SPIPixycam{
	public final int sync = 0x5a;
	// public final int dataSync = 0x5b;
	public final int startWord = 0xaa55;
	public final int startWordColor = 0xaa56;// CHANGE THIS WHEN CMU COMES BACK
												// UP
	public SPI pixyIn;
	private boolean foundPacket = false;
	ArrayList<Sighting> packet = new ArrayList<Sighting>();
	public int mostRecentIn = 1086;
	public Port SPIPort;
	public SPIPixycam(double horizontalOffset, double verticalOffset,double depthOffset, double hAngle, double vAngle) {
		//super(47.0, 75.0, 640.0, 400.0, horizontalOffset, verticalOffset, depthOffset, hAngle, vAngle);
		SPIPort = Port.kOnboardCS0;
		pixyIn = new SPI(SPIPort);

	}
	
	public void initializeCamera(String name) {
		new Thread (()->{
			
		}).start();
	}
	public void findNextPacket() {// each packet should start with 2 start bytes
		int lastIn = 1086;
		while (true) {
			getNextIn();

			if (mostRecentIn == startWord && lastIn == startWord) {
				break;
			}
			System.out.println("Searching for start of Packet");
			lastIn = mostRecentIn;
		}
	}

	public void readPacket() {
		if (!foundPacket) {
			findNextPacket();
		} else {
			foundPacket = false;
		}
		
		for (int i=0; i<130; i++){//130 is the highest number of objects that can be sent
			int trialsum=0;
			int checksum=getNextIn();
			if (checksum==startWord || checksum==startWordColor){
				foundPacket=true;
				return;
			}
			if (checksum==0){
				System.out.println("This Shouldn't happen.");
				return;
			}
			int[] object=new int[5];
			for (int i1=0; i1<5; i1++){
				object[i1] = getNextIn();
				trialsum+=object[i1];
				
			}
			if (checksum==trialsum){
				packet.add(new Sighting(object[0], object[1], object[2], object[3], object[4]));
			} else {
				System.out.println("Checksum!=TrialSum??");
			}
			
		}
	}
	
	public int getNextIn() {
		ByteBuffer outInBinary = ByteBuffer.allocate(2);
		outInBinary.putInt(sync);// 2?????????
		outInBinary.flip();
		ByteBuffer inInBinary = ByteBuffer.allocate(2);
		pixyIn.transaction(outInBinary, inInBinary, 2);
		mostRecentIn = inInBinary.getInt();
		return mostRecentIn;
	}
}
