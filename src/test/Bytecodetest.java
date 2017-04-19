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
	File f;
	File out;

	public Bytecodetest(String[] args) {
		// a = Integer.parseInt(args[0]);
		// t = Boolean.parseBoolean(args[1]);

		// f = new File(args[1]);
		f = new File("test.jpg");
		out = new File("output.jpg");

		// url = PLPRuntimeImageIO.getURL(args, 1);

	}

	public static void main(String[] args) {
		Bytecodetest instance = new Bytecodetest(args);
		instance.run();
	}

	public void run() {
		BufferedImage p;
		BufferedImage p1;
		p = PLPRuntimeImageIO.readFromFile(f);
		p1 = p;
		if (p == p1) {
			PLPRuntimeImageIO.write(p, out);
		}

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
