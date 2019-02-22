package io.hkhc.scrapping.mingpao;

import io.hkhc.autoweb.GenericPage;

import java.io.IOException;

public class MingPaoMenuPage extends GenericPage {

	private PageUtils pageUtils;

	public MingPaoMenuPage() {
		pageUtils = new PageUtils(this);
	}

	public EpaperCalendarPage getEpaperCalendarPage() throws IOException {
		
		return (EpaperCalendarPage)pageUtils.click(
				"//a[contains(@href,'javascript:popupWindow') and contains(@href,'epaper.mingpao.com/index1.htm')]"
		);

	}

}
