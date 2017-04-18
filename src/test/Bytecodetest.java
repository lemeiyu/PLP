package test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import cop5556sp17.PLPRuntimeFilterOps;
import cop5556sp17.PLPRuntimeFrame;
import cop5556sp17.PLPRuntimeImageIO;
import cop5556sp17.PLPRuntimeImageOps;

public class Bytecodetest {

	// int a;
	// boolean t;

	boolean b0;
	boolean b1;

	public Bytecodetest(String[] args) {
		// a = Integer.parseInt(args[0]);
		// t = Boolean.parseBoolean(args[1]);

		// f = new File(args[1]);
		// f = new File("test.jpg");
		b0 = false;
		b1 = true;

		// url = PLPRuntimeImageIO.getURL(args, 1);
		/*
		 * try { url = new URL(args[3]); } catch (MalformedURLException e) { //
		 * TODO Auto-generated catch block // e.printStackTrace(); }
		 */

	}

	public static void main(String[] args) {
		Bytecodetest instance = new Bytecodetest(args);
		instance.run();
	}

	public void run() {
		boolean b3;
		boolean b4;
		b3 = b0 && b1;
		b4 = b0 || b1;
		// BufferedImage p;
		// p = PLPRuntimeImageIO.readFromFile(f);

		// BufferedImage p1 = PLPRuntimeFilterOps.convolveOp(p, null);
		// PLPRuntimeFrame fr1;
		// fr1 = PLPRuntimeFrame.createOrSetFrame(p1, null);
		// fr1.showImage();
		/*
		 * try { Thread.sleep(5000); } catch (InterruptedException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
		// PLPRuntimeFrame fr;
		// fr=PLPRuntimeFrame.createOrSetFrame(p, null).moveFrame(600, 300);
		// fr.showImage();
		// frame.showImage();
		// int i = PLPRuntimeFrame.getScreenHeight();
	}
}
