package org.pathvisio.reactome;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
