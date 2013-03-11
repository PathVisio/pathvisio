package org.pathvisio.pluginmanager.impl;

import org.osgi.framework.Bundle;

public class StatusMessage {

	private Bundle bundle;
	private boolean success = true;
	private String message;
	
	public Bundle getBundle() {
		return bundle;
	}
	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
