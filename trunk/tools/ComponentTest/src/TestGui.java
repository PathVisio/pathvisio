// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;


public class TestGui extends TestMain {
	JFrame frame;
	public TestGui(int w, int h, int nr) {
		super(w, h, nr);

		frame = new JFrame("Component test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container content = frame.getContentPane();
		content.setLayout(new BorderLayout());

		content.add(panel);
		frame.setSize(w, h);
	}
}
