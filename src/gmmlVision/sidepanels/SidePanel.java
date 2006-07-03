package gmmlVision.sidepanels;

import gmmlVision.GmmlVision;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Sash;

/**
 * This class can be extended to create a sidepanel with minimize button
 * for use as component of a {@link SashForm}
 */
public class SidePanel extends Composite {
	private GmmlVision gmmlVision;
	private SashForm parentSash;
	private Composite contentComposite;
	
	/**
	 * Constructor for this class
	 * @param parent	The parent composite (needs to be an {@link SashForm} for the
	 * minimize button to work
	 * @param style
	 * @param gmmlVision
	 */
	public SidePanel(Composite parent, int style, GmmlVision gmmlVision) {
		super(parent, style);
		this.gmmlVision = gmmlVision;
		if(parent instanceof SashForm) //Minimize button only works if parent is sashform
			parentSash = (SashForm)parent;
		createControls();
	}

	public Composite getContentComposite() { return contentComposite; }
	
	Composite buttonBar;
	Composite stackComposite;
	Composite emptyComposite;
	StackLayout stackLayout;
	Button minButton;
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
			GridLayout barLayout = new GridLayout(2, true);
			barLayout.marginBottom = barLayout.marginHeight = barLayout.marginWidth = 1;
			buttonBar.setLayout(barLayout);
						
			minButton = new Button(buttonBar, SWT.TOGGLE);
			minButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(minButton.getSelection())
						minimize();
					else
						restore();
				}
			});
			minButton.setImage(GmmlVision.imageRegistry.get("sidepanel.minimize"));
			final Button hideButton = new Button(buttonBar, SWT.PUSH);
			hideButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					hide();
					gmmlVision.showRightPanelAction.setChecked(false);
				}
			});
			hideButton.setImage(GmmlVision.imageRegistry.get("sidepanel.hide"));
			
			GridData buttonGrid = new GridData();
			buttonGrid.widthHint = 12;
			buttonGrid.heightHint =  12;
			hideButton.setLayoutData(buttonGrid);
			minButton.setLayoutData(buttonGrid);
			
			buttonBar.pack();
		}
		stackComposite = new Composite(this, SWT.NULL);
		stackComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		stackLayout = new StackLayout();
		stackComposite.setLayout(stackLayout);
		emptyComposite = new Composite(stackComposite, SWT.NULL);
		emptyComposite.setLayout(new FillLayout());
		contentComposite = new Composite(stackComposite, SWT.NULL);
		contentComposite.setLayout(new FillLayout());
		
		stackLayout.topControl = contentComposite;
	}
	
	/**
	 * Minimizes this panel, while the minimize button will still be visible
	 */
	public void minimize() {
		parentSash.setWeights(calculateWeights(WEIGHTS_MINIMIZE));
		stackLayout.topControl = emptyComposite;
		stackComposite.layout();
	}
	
	/**
	 * Hides this panel, the minimize button will not be visible anymore
	 */
	public void hide() {
		parentSash.setWeights(calculateWeights(WEIGHTS_HIDE));
	}
	
	/**
	 * Restores the size of the panel to its previous size
	 */
	public void restore() {
		if(oldWeights == null) return;
		parentSash.setWeights(oldWeights);
		stackLayout.topControl = contentComposite;
		stackComposite.layout();
	}
	
	private int[] oldWeights;
	private static final int WEIGHTS_HIDE = 0;
	private static final int WEIGHTS_MINIMIZE = 1;
	/**
	 * Calculates weights that have to be passed to the parent {@link SashForm#setWeights(int[])}
	 * to minimize or hide this panel
	 * @param method one of WEIGHTS_HIDE or WEIGHTS_MINIMIZE, in the first case the panel is hidden,
	 * so its weight is set to zero, in the second case the panel is miminized in a way the minimize 
	 * button is still visible
	 * @return
	 */
	private int[] calculateWeights(int method) {
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
		int newWidth;
		if(method == WEIGHTS_MINIMIZE) newWidth = minButton.getSize().x;
		else newWidth = 0;
		
		int thisWidth = getSize().x;
		
		//Calculate new weights
		int newWeight = (int)(((double)newWidth / thisWidth) * thisWeight);
		//Adjust the weight of this and the next control
		weights[thisIndex] = newWeight;
		weights[neighbourIndex] += thisWeight - newWeight;
		return weights;
	}

}
