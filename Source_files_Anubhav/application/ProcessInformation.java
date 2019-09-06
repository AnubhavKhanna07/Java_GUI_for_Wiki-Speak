package application;

public class ProcessInformation {
	
	// A ProcessInformation object stores the output of a Bash process
	
	private String _stdout;
	private String _stderr;
	private int _exitStatus;

	public ProcessInformation(String stdout, String stderr, int exitStatus) {
		_stdout = stdout;
		_stderr = stderr;
		_exitStatus = exitStatus;
	}
	
	public String getStdout() {
		return _stdout;
	}
	
	public String getStderr() {
		return _stderr;
	}
	
	public int getExitStatus() {
		return _exitStatus;
	}
}
