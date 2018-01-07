package io.hkhc.scrapping.mingpao;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import io.hkhc.autoweb.spi.commons.GenericGetPage;

import java.io.IOException;

public class MingPaoHomePage extends GenericGetPage {

	private PageUtils pageUtils;

	public MingPaoHomePage() {
		pageUtils = new PageUtils(this);
	}

	public MingPaoMenuPage login(String username, String password) throws IOException {

		System.out.println("Login mingpao.com");
		
		HtmlForm searchForm = (HtmlForm)getHtmlUnitHelper().getSingleNode("//form[@method='post' and contains(@action,'EpaperLogin2.cfm')]", getPage());
		if (searchForm==null) {
			System.out.println("Login form is not found");
			return null;
		}

		System.out.println("Login form is found");
		
		HtmlInput usernameField = searchForm.getInputByName("UserName");
		usernameField.setValueAttribute(username);
		HtmlInput passwordField = searchForm.getInputByName("Password");
		passwordField.setValueAttribute(password);

		HtmlSubmitInput submitField = (HtmlSubmitInput)getHtmlUnitHelper().getSingleNode(".//input[@type='submit']", searchForm);

		return (MingPaoMenuPage)pageUtils.click(submitField);

	}

}
