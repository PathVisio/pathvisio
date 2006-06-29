package gmmlVision.sidepanels;

import gmmlVision.GmmlVision;

import java.awt.BorderLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Sash;

/**
 * This class can be extended to create a sidepanel with minimize button
 * for use as component of a {@link SashForm}
 */
public abstract class SidePanel extends Composite {
	private SashForm parentSash;
	
	/**
	 * Constructor for this class
	 * @param parent	The parent composite (needs to be an {@link SashForm} for the
	 * minimize button to work
	 * @param style	
	 */
	public SidePanel(Composite parent, int style) {
		super(parent, style);
		if(parent instanceof SashForm) //Minimize button only works if parent is sashform
			parentSash = (SashForm)parent;
		createControls();
	}
	
	Composite buttonBar;
	Composite contentComposite;
	/**
	 * Creates the button controls to minimize the sidepanel and a {@link Composite} for the contents
	 */
	public void createControls() {
		GridLayout topLayout = new GridLayout();
		topLayout.verticalSpacing = topLayout.marginTop = topLayout.marginWidth = 0;
		setLayout(topLayout);
		
		if(parentSash != null) {
			//Create minimize control on top of content
			buttonBar = new Composite(this, SWT.NULL);
			GridLayout barLayout = new GridLayout();
			barLayout.marginBottom = barLayout.marginHeight = barLayout.marginWidth = 1;
			buttonBar.setLayout(barLayout);
			final Button minButton = new Button(buttonBar, SWT.TOGGLE);
			minButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(minButton.getSelection())
						parentSash.setWeights(calculateWeights());
					else
						parentSash.setWeights(oldWeights);
				}
			});
			GridData buttonGrid = new GridData();
			buttonGrid.widthHint = 17;
			buttonGrid.heightHint = 17;
			minButton.setLayoutData(buttonGrid);
			minButton.setImage(GmmlVision.imageRegistry.get("sidepanel.minimize"));
			buttonBar.pack();
		}
		contentComposite = new Composite(this, SWT.NULL);
		contentComposite.setLayout(new FillLayout());
		contentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	private int[] oldWeights;
	/**
	 * Calculates weights that have to be passed to the parent {@link SashForm#setWeights(int[])}
	 * to minimize this panel in a way that the minimize button is still visible
	 * @return
	 */
	private int[] calculateWeights() {
		Control[] controls = parentSash.getChildren();
		int[] weights = parentSash.getWeights();
		oldWeights = weights.clone();
		//Get the index of this control in the sashform
		int thisIndex = 0;
		for(int i = 0; i < controls.length; i++) {
			if(controls[i] == this) break;
			if(!(controls[i] instanceof Sash)) thisIndex++; //Don't count sash controls
		}
		
		int thisWeight = weights[thisIndex];
		
		//Get the index of the neighbouring composite
		int neighbourIndex = -1;
		if(thisIndex == weights.length - 1) neighbourIndex = thisIndex - 1;
		else neighbourIndex = thisIndex + 1;
		
		//Calculate widths needed to calculate new weight
		int newWidth = buttonBar.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		int thisWidth = getSize().x;
		
		//Calculate new weights
		int newWeight = (int)(((double)newWidth / thisWidth) * thisWeight);
		//Adjust the weight of this and the next control
		weights[thisIndex] = newWeight;
		weights[neighbourIndex] += thisWeight - newWeight;
		
		return weights;
	}

}
