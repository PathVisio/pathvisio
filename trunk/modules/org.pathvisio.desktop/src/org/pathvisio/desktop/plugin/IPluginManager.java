package org.pathvisio.desktop.plugin;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.pathvisio.desktop.PvDesktop;

public interface IPluginManager {

	public void installLocalPlugins(List<File> plugins, List<File> dependencies);
	public void init(File localRepo, Set<URL> onlineRepo, PvDesktop desktop);
	public void showGui(JFrame parent);
}
