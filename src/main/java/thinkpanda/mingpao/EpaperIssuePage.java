package thinkpanda.mingpao;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlBody;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.pithk.autoweb.AutoWebPage;
import com.pithk.autoweb.GenericPage;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class EpaperIssuePage extends GenericPage {
	
	class EpaperInfo {
		String name;
		String pageNumber;
		HtmlAnchor anchor;
	}
	
	private SortedMap<String,HtmlAnchor> sections = null;
	private SortedMap<String,EpaperInfo> pages = new TreeMap<String,EpaperInfo>();

	private String getSectionFromQuery(String query) {

		return getSectionFromQuery(query, "File");
	}

	private String getSectionFromQuery(String query, String paramName) {

		String[] params = query.split("&");

		for(int i=0;i<params.length;i++) {
			String[] pair = params[i].split("=");
			if (paramName.equals(pair[0])) return pair[1];
		}
		
		return null;
		
	}
	
	public String getCurrentSection() {
		
		return getSectionFromQuery(getResponseUrl().getQuery());
		
	}
	
	public List<String> getSectionList() {
		
		sections = new TreeMap<String, HtmlAnchor>();
		
		List<? extends HtmlElement> anchors = getHtmlUnitHelper().getNodes("//div[@class='number_wrap']/a[.//img[contains(@src,'icon_')] ]", getPage());
		if (anchors==null) return null;
		
		for(HtmlElement e : anchors) {
			HtmlAnchor a = (HtmlAnchor)e;
			String link = a.getHrefAttribute();
			String s = getSectionFromQuery(link.substring(link.indexOf("?")+1),"File");
			sections.put(s, a);
		}
		
		List<String> result = new ArrayList<String>();
		result.addAll(sections.keySet());
		
		return result;
		
	}
	
	public AutoWebPage getSectionPage(String s) throws IOException  {

		if (s.equals(getCurrentSection())) return this;
		
		if (sections==null) getSectionList();
		
		HtmlAnchor anchor = sections.get(s);
		
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

	public SortedMap<String,EpaperInfo> getPages() {
		
		pages = new TreeMap<String,EpaperInfo>();
		
		List<? extends HtmlElement> pageBlocks = getHtmlUnitHelper().getNodes("//div[@style='float: left;']//a[1]", getPage());
		
		for(HtmlElement e : pageBlocks) {
			HtmlElement thumbnail = (HtmlElement)getHtmlUnitHelper().getSingleNode("img", e);
			HtmlAnchor a = (HtmlAnchor)e;
			//System.out.println(a.getAttribute("href") + " " + thumbnail.getAttribute("src"));
			// a hack to fix the problem that link to a skipped page contained gabrage URL
			if (thumbnail.getAttribute("src").indexOf("Epaper1.cfm")==-1) continue;
			EpaperInfo info = new EpaperInfo();
			info.anchor = a;
			info.name = a.asText();
			String url = a.getHrefAttribute();
			info.pageNumber = getSectionFromQuery(url.substring(url.indexOf('?')+1));
			pages.put(info.pageNumber, info);
		}

		return pages;
		
	}

	public InputStream getPageImage(String pageNumber) throws IOException {
		
		HtmlPage p = (HtmlPage)getPage();
		NodeList nl = p.getElementsByTagName("body");
		HtmlBody b = (HtmlBody)nl.item(0);
//		HtmlElement e = p.getDocumentElement();
		HtmlElement suppLink = null;
		try {
			suppLink = p.getHtmlElementById("page-"+pageNumber);
		}
		catch (ElementNotFoundException ex) {
			// do nothing
		}
		String originalLink = pages.get(pageNumber).anchor.getHrefAttribute();
		String date = getSectionFromQuery(originalLink.substring(originalLink.indexOf("?")+1),"Date");
		String file= getSectionFromQuery(originalLink.substring(originalLink.indexOf("?")+1),"File");
		if (suppLink==null) {
			HtmlAnchor a = (HtmlAnchor)p.createElement("a");
			String url = "/cfm2/epaper2.cfm?date=@DATE@&file=@NUM@";
			a.setAttribute("href", url.replaceAll("@DATE@", date).replaceAll("@NUM@", file) );
			a.setId("page-"+pageNumber);
			b.appendChild(a);
			suppLink = a;
		}

		Page imagePage = null;
		try {
			imagePage = ((HtmlAnchor)suppLink).click();
			UnexpectedPage up = (UnexpectedPage)imagePage;
			if (imagePage==null)
				return null ;
			else {
				return up.getInputStream();
			}
		}
		catch (FailingHttpStatusCodeException ex) {
			ex.printStackTrace();
			return null;
		}
		catch (ClassCastException e) {
			HtmlPage hp = (HtmlPage)imagePage;
			System.out.println(hp.asXml());
			throw e;
		}
		
	}
	
}