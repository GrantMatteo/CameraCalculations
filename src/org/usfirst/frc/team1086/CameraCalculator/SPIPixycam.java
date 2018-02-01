package org.usfirst.frc.team1086.CameraCalculator;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.opencv.core.Mat;

import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.SPI.Port;

public class SPIPixycam extends Camera {
	public final byte sync = 0x5a;
	// public final int dataSync = 0x5b;
	public final int startWord = 0xaa55;
	public final int startWordColor = 0xaa56;// CHANGE THIS WHEN CMU COMES BACK
												// UP
	private SPI pixyIn;
	private boolean foundPacket = false;
	public ArrayList<Sighting> sightings = new ArrayList<Sighting>();
	public int mostRecentIn = 1086;
	private Port SPIPort;

	public SPIPixycam(double horizontalOffset, double verticalOffset, double depthOffset, double hAngle,
			double vAngle) {
		super(47.0, 75.0, 640.0, 400.0, horizontalOffset, verticalOffset, depthOffset, hAngle, vAngle);
		SPIPort = Port.kOnboardCS0;
		pixyIn = new SPI(SPIPort);
		pixyIn.setMSBFirst();
		pixyIn.setChipSelectActiveLow();
		pixyIn.setClockRate(1000);
		pixyIn.setSampleDataOnFalling();
		pixyIn.setClockActiveLow();

	}
	/**
	 * <b>DO NOT USE -- DOES NOT APPLY TO PIXYCAM</b>. Instead, use numberOfSightings(int id)
	 * @param vt a completely inconsequential input
	 * @return ALWAYS 0
	 */
	public int numberOfSightings(VisionTarget vt) {
		return 0;
	}
	/**
     * <b>DO NOT USE -- DOES NOT APPLY TO PIXYCAM</b>. Instead, use getSightings(int id)
     * @param vt A completely inconsequential vision target
     * @return An empty ArrayList
     */
    public ArrayList<Sighting> getSightings(VisionTarget vt){
       return new ArrayList<Sighting>();
    }
    /**
     * <b>DO NOT USE -- DOES NOT APPLY TO PIXYCAM</b>.
     * @param p A completely inconsequential pipeline
     * 
     */
    public void addPipeline(Pipeline p){
    }
    /**
     * <b>DO NOT USE -- DOES NOT APPLY TO PIXYCAM</b>.
     * @param source A completely inconsequential Mat
     * 
     */
    /**
     * <b>DO NOT USE -- DOES NOT APPLY TO PIXYCAM</b>.     
     * @param target Not used
     */
    public void addVisionTarget(VisionTarget target){
    	
    }
    /**
     * <b>DO NOT USE -- DOES NOT APPLY TO PIXYCAM</b>.
     * @param target Not used
     * @param distance Not used
     * 
     */
    public void configure(VisionTarget target, double distance) {
        
    }
    
	public void initializeCamera(String name) {
		new Thread(() -> {
			while (true) {
				readPacket();
				//System.out.println("X:" + sightings.get(sightings.size() - 1).centerX);
			}
		}).start();
	}

	public void findNextPacket() {// each packet should start with 2 start bytes
		int lastIn = 1086;
		while (true) {
			getNextIn();
			// System.out.println("Most Recent: "+ Integer.toHexString(mostRecentIn)+"|"
			// +Integer.toHexString(startWord));
			if (mostRecentIn == startWord && lastIn == startWord) {

				break;
			}
			// System.out.println("Searching for start of Packet");
			lastIn = mostRecentIn;
		}
	}

	public void readPacket() {
		if (!foundPacket) {
			findNextPacket();
		} else {
			foundPacket = false;
		}

		for (int i = 0; i < 130; i++) {// 130 is the highest number of objects that can be sent
			int trialsum = 0;
			int checksum = getNextIn();
			if (checksum == startWord || checksum == startWordColor) {
				foundPacket = true;
				return;
			}
			if (checksum == 0) {
				System.out.println("Checksum is 0");
				return;
			}
			int[] object = new int[5];
			for (int i1 = 0; i1 < 5; i1++) {
				object[i1] = getNextIn();
				trialsum += object[i1];

			}
			if (checksum == trialsum) {
				sightings.add(new Sighting(object[0], object[1], object[2], object[3], object[4]));
			} else {
				System.out.println("Checksum!=TrialSum??");
			}

		}
	}

	public int getNextIn() {
		ByteBuffer outInBinary = ByteBuffer.allocate(2);
		outInBinary.put(sync);
		// System.out.println("Buffered!");// 2?????????
		outInBinary.flip();
		ByteBuffer inInBinary = ByteBuffer.allocate(2);
		pixyIn.transaction(outInBinary, inInBinary, 2);
		inInBinary.rewind();
		mostRecentIn = (inInBinary.getShort() & 0xffff);
		// System.out.println(inInBinary);
		return mostRecentIn;
	}
}
