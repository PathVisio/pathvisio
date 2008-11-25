package org.pathvisio.wikipathways.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SearchServiceAsync {
	  void search(String query, AsyncCallback<Result[]> callback);
	  void waitForImage(String id, AsyncCallback<Void> callback);
}
