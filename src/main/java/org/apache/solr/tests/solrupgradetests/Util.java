package org.apache.solr.tests.solrupgradetests;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.apache.solr.tests.solrupgradetests.Util.MessageType;

class StreamGobbler extends Thread {

	InputStream is;
	String type;

	StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				Util.postMessage("  SubProcess: " + type + " >> " + line, MessageType.PROCESS, true);

		} catch (IOException ioe) {
			Util.postMessage(ioe.getMessage(), MessageType.RESULT_ERRROR, true);
		}
	}

}

public class Util {

	final static Logger logger = Logger.getLogger(Util.class);

	public enum MessageType {
		PROCESS, ACTION, RESULT_SUCCESS, RESULT_ERRROR, GENERAL
	};

	public static void postMessage(String message, MessageType type, boolean printInLog) {

		String ANSI_RESET = "\u001B[0m";
		String ANSI_RED = "\u001B[31m";
		String ANSI_GREEN = "\u001B[32m";
		String ANSI_YELLOW = "\u001B[33m";
		String ANSI_BLUE = "\u001B[34m";
		String ANSI_WHITE = "\u001B[37m";

		if (type.equals(MessageType.ACTION)) {
			System.out.println(ANSI_WHITE + message + ANSI_RESET);
		} else if (type.equals(MessageType.GENERAL)) {
			System.out.println(ANSI_BLUE + message + ANSI_RESET);
		} else if (type.equals(MessageType.PROCESS)) {
			System.out.println(ANSI_YELLOW + message + ANSI_RESET);
		} else if (type.equals(MessageType.RESULT_ERRROR)) {
			System.out.println(ANSI_RED + message + ANSI_RESET);
		} else if (type.equals(MessageType.RESULT_SUCCESS)) {
			System.out.println(ANSI_GREEN + message + ANSI_RESET);
		}

		if (printInLog) {
			logger.info(message);
		}

	}

	public static void postMessageOnLine(String message) {

		System.out.print(message);

	}

	public int deleteDirectory(String directory) throws IOException, InterruptedException {

		postMessage("Deleting directory: " + directory, MessageType.ACTION, true);
		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		StreamGobbler errorGobbler = null;
		StreamGobbler outputGobbler = null;

		try {

			proc = rt.exec("rm -r -f " + directory);

			errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();
			proc.waitFor();
			return proc.exitValue();

		} catch (Exception e) {

			postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);
			return -1;

		}

	}
	
	
	
	public static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {

		BufferedOutputStream bos = null;
		try {

			bos = new BufferedOutputStream(new FileOutputStream(filePath));
			byte[] bytesIn = new byte[4096];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
			bos.close();

		} catch (Exception e) {

			Util.postMessage(e.getMessage(), MessageType.RESULT_ERRROR, true);

		}
	}


}