package com.nexes.test;

import com.nexes.wizard.*;

import java.awt.*;


public class TestPanel1Descriptor extends WizardPanelDescriptor {

    public static final String IDENTIFIER = "INTRODUCTION_PANEL";

    public TestPanel1Descriptor() {
        super(IDENTIFIER);
    }

    public Object getNextPanelDescriptor() {
        return TestPanel2Descriptor.IDENTIFIER;
    }

    public Object getBackPanelDescriptor() {
        return null;
    }

	@Override
	protected Component createContents()
	{
		return new TestPanel1();
	}

}
