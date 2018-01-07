package io.hkhc.scrapping.mingpao;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import io.hkhc.autoweb.GenericPage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class EpaperCalendarPage extends GenericPage {

	private PageUtils pageUtils;

	private SortedMap<String,HtmlAnchor> calendars = new TreeMap<String,HtmlAnchor>();

	public EpaperCalendarPage() {
		pageUtils = new PageUtils(this);
	}

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
	
	public int getYear() {
		HtmlElement year = (HtmlElement)getHtmlUnitHelper().getSingleNode(".//tr[@align='center']/td[2]", getPage());
		return Integer.parseInt(year.asText());
	}

	public EpaperCalendarPage getPreviousYearCalendar() throws IOException {

		return (EpaperCalendarPage)pageUtils.click(".//tr[@align='center']/td[1]/a");

	}

	public EpaperCalendarPage getNextYearCalendar() throws IOException {

		return (EpaperCalendarPage)pageUtils.click(".//tr[@align='center']/td[3]/a");

	}
	
	public EpaperIssuePage getIssue(String c) throws IOException {

		HtmlAnchor a = calendars.get(c);
		System.out.println("Get "+c + " ("+a.getAttribute("href")+") :");

		return (EpaperIssuePage)pageUtils.click(a);

	}

	public EpaperCalendarPage toYear(int expectedYear) throws IOException {

		int currentYear = getYear();
		EpaperCalendarPage cp = this;

		while (expectedYear!=currentYear) {
			if (expectedYear < currentYear) {
				System.out.println("Retrieve last year calendar...");
				cp = cp.getPreviousYearCalendar();
			}
			else if (expectedYear > currentYear) {
				System.out.println("Retrieve next year calendar...");
				cp = cp.getNextYearCalendar();
			}
			currentYear = cp.getYear();
		}

		return cp;

	}
	
}