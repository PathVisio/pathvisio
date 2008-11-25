package org.pathvisio.wikipathways.client;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class ImageLink extends Widget {

	private Image img;
	private String url;
	private String target;

	private DivElement element;
	private AnchorElement aEl;

	public ImageLink(Image img, String url){
		initElements();
		setImg(img);
		setUrl(url);
	}

	private void initElements() {
		element = Document.get().createDivElement();
		aEl = Document.get().createAnchorElement();

		element.appendChild(aEl);
		setElement(element);

		sinkEvents(Event.MOUSEEVENTS);
		setTarget("_self");
	}

	public void onBrowserEvent(Event event) {
		if(event.getTypeInt() == Event.ONMOUSEOVER){
			aEl.getStyle().setProperty("cursor", "hand");
		}
		super.onBrowserEvent(event);
	}

	public ImageLink(){
		this(null, "");
	}

	/**
	 * @return the img
	 */
	public Image getImg() {
		return img;
	}

	/**
	 * @param img the img to set
	 */
	public void setImg(Image img) {
		this.img = img;
		aEl.appendChild(img.getElement());
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
		aEl.setHref(url);
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
		aEl.setTarget(target);
	}

} 