package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import application.GUI;

public class ProcessCreator {
	
	private static String stdoutText = "";
	private static String stderrText = "";
	private static ProcessInformation processInfo;
	
	// Following method creates a bash process using the parameter "command"
	public static ProcessInformation executeProcess(String command) {
		
		stdoutText = "";
		stderrText = "";
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command);
		builder.directory(new File(GUI._jarDir));
		try {
			Process process = builder.start();
			InputStream stdout = process.getInputStream();
			InputStream stderr = process.getErrorStream();
			BufferedReader stdoutBuffered = new BufferedReader(new InputStreamReader(stdout));
			BufferedReader stderrBuffered = new BufferedReader(new InputStreamReader(stderr));
			process.waitFor();
			String line = null;
			
			while ((line = stdoutBuffered.readLine()) != null) {
				stdoutText+=line+"\n";
			}
			line = null;
			while ((line = stderrBuffered.readLine()) != null) {
				stderrText+=line+"\n";
			}
			
			int exitStatus = process.exitValue();
			processInfo = new ProcessInformation(stdoutText, stderrText, exitStatus);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return processInfo;
	}
}
