// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
package org.pathvisio.reactome;
import java.util.List;


public class ReactionlikeEvent extends Event {
	List<PhysicalEntity> input;
	List<PhysicalEntity> output;

	int inputX;
	int inputY;
	int outputX;
	int outputY;

	public ReactionlikeEvent(int id) {
		super(id);
	}

	List<PhysicalEntity> getInput() {
		return input;
	}

	void setInput(List<PhysicalEntity> input) {
		this.input = input;
	}

	List<PhysicalEntity> getOutput() {
		return output;
	}

	void setOutput(List<PhysicalEntity> output) {
		this.output = output;
	}

	int getInputX() {
		return inputX;
	}

	void setInputX(int inputX) {
		this.inputX = inputX;
	}

	int getInputY() {
		return inputY;
	}

	protected void setInputY(int inputY) {
		this.inputY = inputY;
	}

	protected int getOutputX() {
		return outputX;
	}

	protected void setOutputX(int outputX) {
		this.outputX = outputX;
	}

	protected int getOutputY() {
		return outputY;
	}

	protected void setOutputY(int outputY) {
		this.outputY = outputY;
	}
}
