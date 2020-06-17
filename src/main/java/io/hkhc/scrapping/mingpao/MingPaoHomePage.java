package io.hkhc.scrapping.mingpao;

import com.gargoylesoftware.htmlunit.html.*;
import io.hkhc.autoweb.spi.commons.GenericGetPage;

import java.io.IOException;

public class MingPaoHomePage extends GenericGetPage {

	private PageUtils pageUtils;

	public MingPaoHomePage() {
		pageUtils = new PageUtils(this);
	}

	public EpaperCalendarPage login(String username, String password) throws IOException {

		System.out.println("Login mingpao.com");
		
		HtmlForm searchForm = (HtmlForm)getHtmlUnitHelper().getSingleNode("//form[@method='post' and contains(@action,'../php/login2.php')]", getPage());
		if (searchForm==null) {
			System.out.println("Login form is not found");
			return null;
		}

		System.out.println("Login form is found");
		
		HtmlInput usernameField = searchForm.getInputByName("UserName");
		usernameField.setValueAttribute(username);
		HtmlInput passwordField = searchForm.getInputByName("Password");
		passwordField.setValueAttribute(password);

		HtmlRadioButtonInput landingField = searchForm.getRadioButtonsByName("Landing").get(1);
		landingField.setChecked(true);

		HtmlButton submitField = (HtmlButton)getHtmlUnitHelper().getSingleNode(".//button[@type='submit']", searchForm);

		return (EpaperCalendarPage)pageUtils.click(submitField);

	}

}
