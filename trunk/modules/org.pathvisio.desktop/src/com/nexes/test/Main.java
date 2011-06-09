package com.nexes.test;

import com.nexes.wizard.*;

public class Main {

    public static void main(String[] args) {

        Wizard wizard = new Wizard();
        wizard.getDialog().setTitle("Test Wizard Dialog");

        WizardPanelDescriptor descriptor1 = new TestPanel1Descriptor();
        wizard.registerWizardPanel(descriptor1);

        WizardPanelDescriptor descriptor2 = new TestPanel2Descriptor();
        wizard.registerWizardPanel(descriptor2);

        WizardPanelDescriptor descriptor3 = new TestPanel3Descriptor();
        wizard.registerWizardPanel(descriptor3);

        wizard.setCurrentPanel(TestPanel1Descriptor.IDENTIFIER);

        int ret = wizard.showModalDialog(null);

        System.out.println("Dialog return code is (0=Finish,1=Cancel,2=Error): " + ret);
        System.out.println("Second panel selection is: " +
            (((TestPanel2)descriptor2.getPanelComponent()).getRadioButtonSelected()));

        System.exit(0);

    }

}
