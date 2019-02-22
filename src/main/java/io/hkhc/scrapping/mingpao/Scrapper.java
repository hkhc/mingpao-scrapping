package io.hkhc.scrapping.mingpao;

import io.hkhc.autoweb.SourceLocator;
import io.hkhc.utils.FileOptions;
import io.hkhc.utils.FileUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Scrapper {

    private String username;
    private String password;
    private String startPage = "A01";
    private String selectedDate;
    private String outputDirectory;

    public Scrapper username(String username) {
        this.username = username;
        return this;
    }

    public Scrapper password(String password) {
        this.password = password;
        return this;
    }

    public Scrapper startPage(String startPage) {
        this.startPage = startPage;
        return this;
    }

    public Scrapper selectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
        return this;
    }

    public Scrapper outputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public void scrap() throws IOException {

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
        cp.getAvailableDate();

        EpaperIssuePage ip = cp.getIssue(selectedDate);
        if (ip==null) {
            System.out.println("Content for the date '"+selectedDate+"' is not available.");
            return;
        }

        String targetDirStr = outputDirectory+"/mingpao-"+selectedDate;
        FileUtils.ensureDirectory(targetDirStr);

        PDFTarget target = new PDFTarget();
        target.start();

        List<String> sections = ip.getSectionList();
        for(String s : sections) {
            System.out.println("Get Section " + s);
            ip = ip.getSectionPage(s);
            Map<String,EpaperIssuePage.EpaperInfo> pages = ip.getPages();
            for(Map.Entry<String, EpaperIssuePage.EpaperInfo> e : pages.entrySet()) {

                String pageKey = e.getKey();;
                EpaperIssuePage.EpaperInfo pageInfo = e.getValue();

                if (pageInfo.pageNumber.compareTo(startPage) < 0) continue;
                System.out.print(pageInfo.anchor.asText().replace("\n", ""));
                System.out.print(" ");

                String pageName = getPageName(pageInfo);

                String filename = targetDirStr+"/mingpao-"+pageName+".jpg";

                FileOutputStream fos = new FileOutputStream(filename);
                // TODO add progress indicator
                // TODO custom buffer size (		int bufferSize = 102400;)
                FileUtils.writeStreamToStream(fos, ip.getPageImage(pageKey),
                        new FileOptions()
                                .bufferSize(102400)
                                .progressCallback(count -> System.out.print("#")));
                System.out.println();

                target.addPage(pageName, filename);

            }
        }

        target.finish();
        target.saveDocument(new FileOutputStream(targetDirStr+"/mingpao-"+selectedDate+".pdf"));
        target.cleanup();

    }

    private String getPageName(EpaperIssuePage.EpaperInfo paperInfo) {

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
