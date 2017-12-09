package thinkpanda.mingpao;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.pithk.autoweb.AutoWebPage;
import com.pithk.autoweb.SourceLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import thinkpanda.mingpao.EpaperIssuePage.EpaperInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		System.setProperty("file.encoding", "utf-8");
		System.setProperty("user.language", "zh");

		String selectedDate ;
		String baseDirectory;
		String username ;
		String password ;
		String startPage;

		String[] finalArgs;

		finalArgs = args;

		selectedDate = finalArgs[0];
		baseDirectory = finalArgs[1];
		username = finalArgs[2];
		password = finalArgs[3];
		startPage = (finalArgs.length>=5) ? finalArgs[4] : null;
		startPage = startPage.trim();
		
		
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "warning");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "warning");

		InputStream iss = Main.class.getClass().getResourceAsStream("config/spring/app-spring.xml");
		if (iss==null)
			System.out.println("iss is null");

		ApplicationContext ac = new ClassPathXmlApplicationContext(new String[] {
				"config/spring/app-spring.xml",
				"config/spring/autoweb-spring.xml"
		});

		SourceLocator sourceLocator = (SourceLocator)ac.getBean("sourceLocator");
		
		MingPaoSite site = (MingPaoSite)sourceLocator.getSite("mingPao");
		site.setJavascriptEnabled(true);
		
		AutoWebPage page = site.getCurrentPage();
		
//		Assert.assertNotNull(page);
//		Assert.assertTrue(page instanceof MingPaoHomePage);

		MingPaoHomePage hp = (MingPaoHomePage)page;
		AutoWebPage menuPage = hp.login(username, password);
		
//		Assert.assertNotNull(menuPage);
//		Assert.assertTrue(menuPage instanceof MingPaoMenuPage);
		
		MingPaoMenuPage mp = (MingPaoMenuPage)menuPage;
		AutoWebPage epaperCalendarPage = mp.getEpaperCalendarPage();
		
//		Assert.assertNotNull(epaperCalendarPage);
//		Assert.assertTrue(epaperCalendarPage instanceof EpaperCalendarPage);

		System.out.println("Retrieve Calendar year");
		EpaperCalendarPage cp = (EpaperCalendarPage)epaperCalendarPage;
		
		int expectedYear = Integer.parseInt(selectedDate.substring(0,4));
		int currentYear = Integer.parseInt(cp.getYear());
		
		while (expectedYear!=currentYear) {
			if (expectedYear < currentYear) {
				System.out.println("Retrieve last year calendar...");
				cp = (EpaperCalendarPage)cp.getPreviousYearCalendar();
				currentYear = Integer.parseInt(cp.getYear());
			}
			else if (expectedYear > currentYear) {
				System.out.println("Retrieve next year calendar...");
				cp = (EpaperCalendarPage)cp.getNextYearCalendar();
				currentYear = Integer.parseInt(cp.getYear());
			}
		}
		
		Map<String, HtmlAnchor> calendars = cp.getAvailableDate();

		
//		for(Map.Entry<String,HtmlAnchor> e : calendars.entrySet()) {
//			System.out.println(e.getKey());
//		}

		AutoWebPage epaperIssuePage = cp.getIssue(selectedDate);
		if (epaperIssuePage==null) {
			System.out.println("Content for the date '"+selectedDate+"' is not available.");
			return;
		}
		
//		Assert.assertNotNull(epaperIssuePage);
//		Assert.assertTrue(epaperIssuePage instanceof EpaperIssuePage);
		
		EpaperIssuePage ip = (EpaperIssuePage)epaperIssuePage;

		String targetDirStr = baseDirectory+"/mingpao-"+selectedDate;
		File targetDir = new File(targetDirStr);
		targetDir.mkdirs();
		
		int bufferSize = 102400;
		List<String> sections = ip.getSectionList();
		for(String s : sections) {
			System.out.println("Get Section " + s);
			ip = (EpaperIssuePage)ip.getSectionPage(s);
			Map<String,EpaperInfo> pages = ip.getPages();
			for(Map.Entry<String, EpaperInfo> e : pages.entrySet()) {
				if (startPage!=null && e.getValue().pageNumber.compareTo(startPage) < 0) continue;
				System.out.print(e.getValue().anchor.asText().replace("\n", ""));
				System.out.print(" ");
				
				String filename = null;
				if (e.getValue().pageNumber.indexOf("-----")!=-1) 
					filename = e.getValue().pageNumber;
				else {
					filename = e.getValue().anchor.asText().replaceAll("\n", "");
					filename = filename.replaceAll("/", "-");
					int idx = filename.lastIndexOf("(");
					filename = filename.substring(0,idx-1).trim();
					filename = e.getValue().pageNumber + "-"+filename;
				}
				
				FileOutputStream fos = new FileOutputStream(targetDirStr+"/mingpao-"+filename+".jpg");
				byte[] buffer = new byte[bufferSize];
				int len;
				InputStream is = ip.getPageImage(e.getKey());
				while ((len=is.read(buffer, 0, bufferSize))!=-1) {
					System.out.print("#");
					fos.write(buffer, 0, len);
				}
				System.out.println();
				fos.close();
				
			}
		}
		
	}
	
	

}
