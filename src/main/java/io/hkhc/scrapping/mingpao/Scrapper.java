package io.hkhc.scrapping.mingpao;

import io.hkhc.autoweb.SourceLocator;
import io.hkhc.utils.FileOptions;
import io.hkhc.utils.FileUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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

    class PageInfo {
        String pageName;
        String fileName;
        public PageInfo(String pageName, String filename) {
            this.pageName = pageName;
            this.fileName = filename;
        }
    }

    private void configure() {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "warning");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "warning");
    }

    public void scrap() throws IOException {

        configure();

        InputStream iss = Main.class.getClass().getResourceAsStream("config/spring/app-spring.xml");
        if (iss==null)
            System.out.println("config/spring/app-spring.xml is not available");

        ApplicationContext ac = new ClassPathXmlApplicationContext(new String[] {
                "config/spring/app-spring.xml",
                "config/spring/autoweb-spring.xml"
        });

        SourceLocator sourceLocator = (SourceLocator)ac.getBean("sourceLocator");

        MingPaoSite site = (MingPaoSite)sourceLocator.getSite("mingPao");
        site.setJavascriptEnabled(true);

        MingPaoLandingPage lp = (MingPaoLandingPage)site.getCurrentPage();

        MingPaoHomePage hp = (MingPaoHomePage)lp.loginPage();
        EpaperCalendarPage cp = hp.login(username, password);

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

        Map<String, List<PageInfo>> sectionPageMap = new HashMap();

        List<String> sections = ip.getSectionList();
        for(String section : sections) {
            System.out.println("Get Section " + section);
            ip.refeshSectionList(section);
            ip = ip.getPageForSection(section);
            ip.getSectionList();

            sectionPageMap.put(section, new ArrayList());

            Map<String,EpaperIssuePage.EpaperInfo> pages = ip.getPages();
            for(Map.Entry<String, EpaperIssuePage.EpaperInfo> e : pages.entrySet()) {

                EpaperIssuePage issuePage = ip;

                EpaperIssuePage singlePage = issuePage.getPage(e.getValue().pageNumber);
                singlePage = singlePage.toV4();

                String pageKey = e.getKey();;
                EpaperIssuePage.EpaperInfo pageInfo = e.getValue();

                if (pageInfo.pageNumber.compareTo(startPage) < 0) continue;
                System.out.print(pageInfo.anchor.asText().replace("\n", ""));
                System.out.print(" ");

                String pageName = getPageName(pageInfo);

                String filename = targetDirStr+"/mingpao-"+pageName+".jpg";

                InputStream imageStream = issuePage.getPageImage(pageKey);
                if (imageStream!=null) {
                    FileOutputStream fos = new FileOutputStream(filename);
                    FileUtils.writeStreamToStream(fos, imageStream,
                            new FileOptions()
                                    .bufferSize(102400)
                                    .progressCallback(count -> System.out.print("#")));
                    target.addPage(pageName, filename);
                    sectionPageMap.get(section).add(new PageInfo(pageName, filename));
                }
                else
                    System.out.println("image is not found");
                System.out.println();

                ip = issuePage.getPageForSection(section);
                ip.getSectionList();
                ip.getPages();

            }
        }

        target.finish();
        target.saveDocument(new FileOutputStream(targetDirStr+"/mingpao-"+selectedDate+".pdf"));
        target.cleanup();

        List<String> subpapers = new ArrayList();
        subpapers.add("通通識");
        subpapers.add("智叻中文Smarties'");
        subpapers.add("常識天下");
        subpapers.add("Smarties' Power English");

        for(String sub : subpapers ) {
            for(Map.Entry<String, List<PageInfo>> entry: sectionPageMap.entrySet()) {

                if (sectionMatch(entry.getValue(), sub)) {
                    PDFTarget subTarget = new PDFTarget();
                    subTarget.start();
                    for(PageInfo info: entry.getValue()) {
                        subTarget.addPage(info.pageName, info.fileName);
                    }
                    subTarget.finish();
                    subTarget.saveDocument(
                            new FileOutputStream(targetDirStr+"/mingpao-"+selectedDate+"-"+sub+".pdf")
                    );
                    subTarget.cleanup();
                }

            }
        }

    }

    private Boolean sectionMatch(List<PageInfo> pages, String name)  {

        for(PageInfo p: pages) {
            if (p.pageName.contains(name)) return true;
        }
        return false;

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
