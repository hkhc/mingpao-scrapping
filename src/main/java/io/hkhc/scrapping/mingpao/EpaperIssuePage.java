package io.hkhc.scrapping.mingpao;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import io.hkhc.autoweb.GenericPage;
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
		String link;
	}

	private PageUtils pageUtils;
	private SortedMap<String,HtmlAnchor> sections = null;
	private SortedMap<String,EpaperInfo> pages = new TreeMap<String,EpaperInfo>();

	public EpaperIssuePage() {
		pageUtils = new PageUtils(this);
	}

	private String getSectionFromQuery(String query) {

		return getSectionFromQuery(query, "File");
	}

	private String getPageFromQuery(String query) {

		return getSectionFromQuery(query, "file");
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
		
		sections = new TreeMap<>();
		
		List<? extends HtmlElement> anchors = getHtmlUnitHelper().getNodes("//div[@class='number_wrap']/a[.//img[contains(@src,'icon_')] ][position()<last()]", getPage());
		if (anchors==null) return null;
		
		for(HtmlElement e : anchors) {
			HtmlAnchor a = (HtmlAnchor)e;
			String link = a.getHrefAttribute();
			String s = getSectionFromQuery(link.substring(link.indexOf("?")+1),"file");
			sections.put(s, a);
		}
		
		List<String> result = new ArrayList<String>();
		result.addAll(sections.keySet());
		
		return result;
		
	}

	public void refeshSectionList(String s) throws IOException  {

		if (s.equals(getCurrentSection())) return;
		
		if (sections==null) getSectionList();
		
	}

	public EpaperIssuePage getPageForSection(String s) throws IOException {

		return (EpaperIssuePage)pageUtils.click(sections.get(s));

	}

	public EpaperIssuePage getPage(String pageNumber) throws IOException {

		HtmlAnchor anchor = (HtmlAnchor)getHtmlUnitHelper().getSingleNode("//div[@id='holder_"+pageNumber+"']//a[1]", getPage());
		return (EpaperIssuePage)pageUtils.click(anchor);

	}

	public EpaperIssuePage toV4() throws IOException {

		HtmlImage icon = (HtmlImage)getHtmlUnitHelper().getSingleNode("//div[@class='logo']/img", getPage());
		if (icon.getSrcAttribute().indexOf("v4")!=-1)
			return this;
		else {
			HtmlAnchor anchor = (HtmlAnchor)getHtmlUnitHelper().getSingleNode("//div[@class='logo_txt color_1']/a", getPage());
			return (EpaperIssuePage)pageUtils.click(anchor);
		}


	}

	public SortedMap<String,EpaperInfo> getPages() {
		
		pages = new TreeMap<>();
		
		List<? extends HtmlElement> pageBlocks = getHtmlUnitHelper().getNodes("//div[@style='float: left;']//a[1]", getPage());
		
		for(HtmlElement e : pageBlocks) {

			HtmlPage p = (HtmlPage)getPage();
			NodeList nl = p.getElementsByTagName("body");
			HtmlBody b = (HtmlBody)nl.item(0);

			HtmlElement thumbnail = (HtmlElement)getHtmlUnitHelper().getSingleNode("img", e);
			HtmlAnchor a = (HtmlAnchor)e;


			HtmlAnchor newAnchor = (HtmlAnchor)p.createElement("a");
			String url = a.getHrefAttribute().replaceAll("content5", "content2");
			String pageNumber = getPageFromQuery(url.substring(url.indexOf('?')+1));
			newAnchor.setAttribute("href", url);
			newAnchor.setId("page-"+pageNumber);
			newAnchor.setTextContent(a.getTextContent());
			b.appendChild(newAnchor);


			EpaperInfo info = new EpaperInfo();
			info.anchor = newAnchor;
			info.name = a.asText();
			info.pageNumber = pageNumber;
			info.link = url;
			pages.put(info.pageNumber, info);


//			a.setAttribute("href", a.getHrefAttribute().replaceAll("content1", "content2"));
//			//System.out.println(a.getAttribute("href") + " " + thumbnail.getAttribute("src"));
//			// a hack to fix the problem that link to a skipped page contained gabrage URL
//			if (thumbnail.getAttribute("src").indexOf("path.ashx")==-1) continue;
//			info.anchor = a;
//			info.name = a.asText();
//			info.pageNumber = getPageFromQuery(url.substring(url.indexOf('?')+1));
//			pages.put(info.pageNumber, info);
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
		String originalLink = pages.get(pageNumber).link;
		String date = getSectionFromQuery(originalLink.substring(originalLink.indexOf("?")+1),"date");
		String file= getSectionFromQuery(originalLink.substring(originalLink.indexOf("?")+1),"file");
//		if (suppLink==null) {
			HtmlAnchor a = (HtmlAnchor)p.createElement("a");
			String url = "path.ashx?type=page&date=@DATE@&file=@NUM@";
			String pageLink = url.replaceAll("@DATE@", date).replaceAll("@NUM@", file);
			a.setAttribute("href", pageLink);
			a.setId("page-"+pageNumber);
			b.appendChild(a);
			suppLink = a;
//		}

		Page imagePage = null;
		try {
			imagePage = suppLink.click();
			System.out.println("page class " + imagePage.getClass().getName());
//			System.out.println("page result header ");
//			for(NameValuePair pair : imagePage.getWebResponse().getResponseHeaders()) {
//				System.out.println(pair.getName() + " : " + pair.getValue());
//			}
			if (imagePage instanceof TextPage) {
				System.out.println("text page");
				System.out.println(((TextPage)imagePage).getContent());
				return null;
			}
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