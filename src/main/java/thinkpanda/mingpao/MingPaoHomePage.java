package thinkpanda.mingpao;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import io.hkhc.autoweb.AutoWebPage;
import io.hkhc.autoweb.spi.commons.GenericGetPage;

import java.io.IOException;

public class MingPaoHomePage extends GenericGetPage {
	
	public MingPaoMenuPage login(String username, String password) throws IOException {

		System.out.println("Login mingpao.com");
		
//		System.out.println(this.getPage().getWebResponse().getContentAsString());
		
//		System.out.println("Hello "+getPage().getUrl());
//		HtmlElement home = (HtmlElement)getHtmlUnitHelper().getSingleNode("/html", getPage());
//		System.out.println("child count = " + home.getChildNodes().size());
//		for(Iterator<DomNode> i = home.getChildren().iterator();i.hasNext();) {
//			DomNode n = i.next();
//			System.out.println(n.getNodeName());
//		}
//		
//		System.out.println(home.asXml());
		
		HtmlForm searchForm = (HtmlForm)getHtmlUnitHelper().getSingleNode("//form[@method='post' and contains(@action,'EpaperLogin2.cfm')]", getPage());
		if (searchForm==null) {
			System.out.println("Login form is not found");
			return null;
		}

//		System.out.println("searchForm : " + searchForm.asXml());

		System.out.println("Login form is found");
		
		HtmlInput usernameField = searchForm.getInputByName("UserName");
		usernameField.setValueAttribute(username);
		HtmlInput passwordField = searchForm.getInputByName("Password");
		passwordField.setValueAttribute(password);

		HtmlSubmitInput submitField = (HtmlSubmitInput)getHtmlUnitHelper().getSingleNode(".//input[@type='submit']", searchForm);
		
		try {
			HtmlPage resultPage = (HtmlPage)submitField.click();
			if (resultPage==null)
				return null;
			else {
				return (MingPaoMenuPage)getRegistry().lookup(resultPage);
			}
		}
		catch (FailingHttpStatusCodeException e) {
			return null;
		}	
			
	}

}
