package org.pathvisio.wikipathways;

public interface UserInterfaceHandler {		
	public static final int Q_CANCEL = -1;
	public static final int Q_TRUE = 0;
	public static final int Q_FALSE = 1;
	
	public void showInfo(String title, String message);
	public void showError(String title, String message);
	public String askInput(String title, String message);
	public boolean askQuestion(String title, String message);
	public int askCancellableQuestion(String title, String message);
	
	public int startProgress();
	public void stopProgress(int id);
}
