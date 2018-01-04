package thinkpanda.mingpao;

import io.hkhc.autoweb.SourceLocator;
import io.hkhc.utils.FileUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import thinkpanda.mingpao.EpaperIssuePage.EpaperInfo;

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

		MingPaoHomePage hp = (MingPaoHomePage)site.getCurrentPage();
		MingPaoMenuPage mp = hp.login(username, password);

		System.out.println("Retrieve Calendar year");
		EpaperCalendarPage cp = mp.getEpaperCalendarPage();

		int expectedYear = Integer.parseInt(selectedDate.substring(0,4));
		cp = cp.toYear(expectedYear);

		EpaperIssuePage ip = cp.getIssue(selectedDate);
		if (ip==null) {
			System.out.println("Content for the date '"+selectedDate+"' is not available.");
			return;
		}

		String targetDirStr = baseDirectory+"/mingpao-"+selectedDate;
		FileUtils.ensureDirectory(targetDirStr);

		List<String> sections = ip.getSectionList();
		for(String s : sections) {
			System.out.println("Get Section " + s);
			ip = ip.getSectionPage(s);
			Map<String,EpaperInfo> pages = ip.getPages();
			for(Map.Entry<String, EpaperInfo> e : pages.entrySet()) {

				String pageKey = e.getKey();;
				EpaperInfo pageInfo = e.getValue();

				if (startPage!=null && pageInfo.pageNumber.compareTo(startPage) < 0) continue;
				System.out.print(pageInfo.anchor.asText().replace("\n", ""));
				System.out.print(" ");

				String pageName = getPageName(pageInfo);

				FileOutputStream fos = new FileOutputStream(targetDirStr+"/mingpao-"+pageName+".jpg");
				// TODO add progress indicator
				// TODO custom buffer size (		int bufferSize = 102400;)
				FileUtils.writeStreamToStream(fos, ip.getPageImage(pageKey));
				System.out.println();

			}
		}

	}

	private static String getPageName(EpaperInfo paperInfo) {

		String filename = null;
		if (paperInfo.pageNumber.indexOf("-----")!=-1)
			filename = paperInfo.pageNumber;
		else {
			filename = paperInfo.anchor.asText().replaceAll("\n", "");
			filename = filename.replaceAll("/", "-");
			int idx = filename.lastIndexOf("(");
			filename = filename.substring(0,idx-1).trim();
			filename = paperInfo.pageNumber + "-"+filename;
		}

		return filename;

	}

}
