package org.pathvisio.cytoscape.superpathways;

import java.awt.Color;
import java.io.File;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.pathvisio.preferences.GlobalPreference;

public class PieGenerator{ // extends ApplicationFrame {

	Color[] colors;
	static String imageLocation = GlobalPreference.getApplicationDir() .toString()+ "/" ;
	
	public PieGenerator(Color[] c){                //(final String title) {
		colors=c;
	}
	
	public void generatePie(int number){
		// Defining the dataset
		DefaultPieDataset dataset = new DefaultPieDataset();
		for (int i=0; i<number; i++){
        	String temp=String.valueOf(i);
        	dataset.setValue(temp, 10);
        }

		// Defining the chart
		JFreeChart chart = ChartFactory.createPieChart("", dataset, false,false, false);

		// Defining the chartPanel
		//final ChartPanel chartPanel = new ChartPanel(chart);
		//chartPanel.setPreferredSize(new java.awt.Dimension(350, 350));
		//setContentPane(chartPanel);

		// Defining the plot
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setLabelGenerator(null);
		plot.setInteriorGap(0.0);

		// Specify the colors here
		
		PieRenderer renderer = new PieRenderer(colors);
		renderer.setColor(plot, dataset);
        
		
		try {
			// This will create a PNG image
			ChartUtilities.saveChartAsPNG(new File(imageLocation+"chart.png"), chart, 280,
					280);
		} catch (Exception e) {
			System.out.println("Exception while creating the chart");
		}
	}

	// A simple renderer for setting custom colors
	// for a pie chart.

	public static class PieRenderer {
		private Color[] color;

		public PieRenderer(Color[] color) {
			this.color = color;
		}

		public void setColor(PiePlot plot, DefaultPieDataset dataset) {
			List<Comparable> keys = dataset.getKeys();
			int aInt;

			for (int i = 0; i < keys.size(); i++) {
				aInt = i % this.color.length;
				plot.setSectionPaint(keys.get(i), this.color[aInt]);
			}
		}
	}

}
