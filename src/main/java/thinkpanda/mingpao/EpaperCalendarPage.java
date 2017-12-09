package thinkpanda.mingpao;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.pithk.autoweb.AutoWebPage;
import com.pithk.autoweb.GenericPage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class EpaperCalendarPage extends GenericPage {

	private SortedMap<String,HtmlAnchor> calendars = new TreeMap<String,HtmlAnchor>();
	
	public Map<String,HtmlAnchor> getAvailableDate() {
		
		System.out.println("getAvailableDate");
		
		List<? extends HtmlElement> monthBlocks = getHtmlUnitHelper().getNodes("//table[@width='125']", getPage());
		
		int year = 0;
		int month = 0;
		int day = 0;
		
		for(HtmlElement monthBlock : monthBlocks) {
			HtmlElement date = (HtmlElement)getHtmlUnitHelper().getSingleNode(".//tr[1]", monthBlock);
			String dateStr = date.asText();
			year = Integer.parseInt(dateStr.substring(0,4));
			month = Integer.parseInt(dateStr.substring(5,dateStr.length()-1));
			System.out.println("year " + year + " month " + month);
			
			List<? extends HtmlElement> days = getHtmlUnitHelper().getNodes(".//a[@target='mpepaper']", monthBlock);
			
			for(HtmlElement d : days) {
				
				day = Integer.parseInt(d.asText());
				

				calendars.put(""+year+"-"+(month<10 ? "0" : "")+month+"-"+(day<10?"0":"")+day, (HtmlAnchor)d);
				
			}

		}
		
		return calendars;
		
	}
	
	public String getYear() {
		HtmlElement year = (HtmlElement)getHtmlUnitHelper().getSingleNode(".//tr[@align='center']/td[2]", getPage());
		return year.asText();
	}
	
	public AutoWebPage getPreviousYearCalendar() throws IOException {

		HtmlAnchor a = (HtmlAnchor)getHtmlUnitHelper().getSingleNode(".//tr[@align='center']/td[1]/a", getPage());
		
		try {
			HtmlPage p = a.click();
			if (p==null)
				return null;
			else {
				return getRegistry().lookup(p);
			}
		}
		catch (FailingHttpStatusCodeException e) {
			return null;
		}	
		
		
	}

	public AutoWebPage getNextYearCalendar() throws IOException {

		HtmlAnchor a = (HtmlAnchor)getHtmlUnitHelper().getSingleNode(".//tr[@align='center']/td[3]/a", getPage());
		
		try {
			HtmlPage p = a.click();
			if (p==null)
				return null;
			else {
				return getRegistry().lookup(p);
			}
		}
		catch (FailingHttpStatusCodeException e) {
			return null;
		}	
		
	}
	
	public AutoWebPage getIssue(String c) throws IOException {
		HtmlAnchor a = calendars.get(c);
		System.out.println("Get "+c + " ("+a.getAttribute("href")+") :");

		if (a==null) {
			return null;
		}
		
		try {
			HtmlPage p = a.click();
			if (p==null)
				return null;
			else {
				return getRegistry().lookup(p);
			}
		}
		catch (FailingHttpStatusCodeException e) {
			return null;
		}	
		
		
	}
	
}