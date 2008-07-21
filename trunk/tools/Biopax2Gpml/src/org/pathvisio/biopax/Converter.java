package org.pathvisio.biopax;

import java.io.File;

import org.pathvisio.model.Pathway;

public class Converter {
	public static void main(String[] args) {
		try {
			BiopaxFormat bpf = new BiopaxFormat(new File(args[0]));
			for(Pathway p : bpf.convert()) {
				p.writeToXml(p.getSourceFile(), true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
