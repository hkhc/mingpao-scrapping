package thinkpanda.mingpao;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.pithk.autoweb.AutoWebPage;
import com.pithk.autoweb.GenericPage;

import java.io.IOException;

public class MingPaoMenuPage extends GenericPage {
	
	public AutoWebPage getEpaperCalendarPage() throws IOException {
		
		HtmlAnchor anchor = (HtmlAnchor)getHtmlUnitHelper().getSingleNode("//a[contains(@href,'javascript:popupWindow') and contains(@href,'Epaper1.cfm')]", getPage());
		if (anchor==null) return null;
		
		try {
			HtmlPage resultPage = anchor.click();
			if (resultPage==null)
				return null;
			else {
				return getRegistry().lookup(resultPage);
			}
		}
		catch (FailingHttpStatusCodeException e) {
			return null;
		}	
		
	}

}
