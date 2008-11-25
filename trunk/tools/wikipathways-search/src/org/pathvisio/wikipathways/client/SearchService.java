package org.pathvisio.wikipathways.client;


import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("wikipathwaysSearch")
public interface SearchService extends RemoteService {
	public Result[] search(String query);
	public void waitForImage(String id);
}