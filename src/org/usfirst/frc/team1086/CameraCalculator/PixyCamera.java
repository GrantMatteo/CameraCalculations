package org.usfirst.frc.team1086.CameraCalculator;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import edu.wpi.first.wpilibj.SPI;

public class PixyCamera extends Camera {
    public final byte sync = 0x5a;
    public final int startWord = 0xaa55;
    public final int startWordColor = 0xaa56;
    private SPI pixyIn;
    private boolean foundPacket = false;
    public ArrayList<Sighting> sightings = new ArrayList<>();
    public int mostRecentIn = 1086;//This initial value shouldn't ever be used
    private SPI.Port SPIPort;
    
    /**
     * Instantiates the CVCamera object
     * @param refreshRate the number of frames to process per second
     * @param vFOV the vertical FOV on the CVCamera
     * @param hFOV the horizontal FOV on the CVCamera
     * @param xPixels the number of pixels in the x direction
     * @param yPixels the number of pixels in the y direction
     * @param horizontalOffset the number of inches from the center of the robot the front bottom center of the lens of the CVCamera is (in the horizontal direction)
     * @param verticalOffset the number of inches from the ground the center of the CVCamera lens is.
     * @param depthOffset the number of inches how deep in to the robot the CVCamera is. Currently unused.
     * @param hAngle the horizontal angle of the CVCamera
     * @param vAngle the vertical angle of the CVCamera
     */
    
    public PixyCamera(int refreshRate, double vFOV, double hFOV, double horizontalOffset, double verticalOffset, double depthOffset, double hAngle, double vAngle){
        super(refreshRate, vFOV, hFOV, 320, 200, horizontalOffset, verticalOffset, depthOffset, hAngle, vAngle);
        SPIPort = SPI.Port.kOnboardCS0;
        pixyIn = new SPI(SPIPort);
        pixyIn.setMSBFirst();
        pixyIn.setChipSelectActiveLow();
        pixyIn.setClockRate(1000);
        pixyIn.setSampleDataOnFalling();
        pixyIn.setClockActiveLow();

    }
    /**
     * Outputs the largest sighting it can find in the most recent frame
     */
    public Sighting getBestSighting() throws Exception{
		if (sightings.size()<1) {
			throw new Exception();
		}
    	int largest=0;
    	for (int i=0; i<sightings.size(); i++) {
			if (sightings.get(i).area>sightings.get(largest).area) {
				largest=i;
			}
		}
    	return sightings.get(largest);
	}
    public void initializeCamera(String name) {
        new Thread(() -> {
            try {
                while (true) {
                    readPacket();
                    Thread.sleep((int) (1000.0 / REFRESH_RATE));
                }
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Pixy camera encountered an error!");
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
        sightings=new ArrayList<Sighting>();
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
                MatOfPoint points = new MatOfPoint();
                points.fromArray(new Point(object[1], object[2]), new Point(object[1] + object[3], object[2]),
                        new Point(object[1] + object[3], object[2] + object[4]), new Point(object[1], object[2] + object[4]));
                sightings.add(new Sighting(points));
            } else {
                System.out.println("Checksum!=TrialSum??");
            }

        }
    }

    public int getNextIn() {
        ByteBuffer outInBinary = ByteBuffer.allocate(2);
        outInBinary.put(sync);
        outInBinary.flip();
        ByteBuffer inInBinary = ByteBuffer.allocate(2);
        pixyIn.transaction(outInBinary, inInBinary, 2);
        inInBinary.rewind();
        mostRecentIn = (inInBinary.getShort() & 0xffff);
        return mostRecentIn;
    }
}