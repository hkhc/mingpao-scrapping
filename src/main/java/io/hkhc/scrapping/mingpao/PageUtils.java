package io.hkhc.scrapping.mingpao;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import io.hkhc.autoweb.AutoWebPage;
import io.hkhc.autoweb.GenericPage;

import java.io.IOException;

public class PageUtils {

    private GenericPage genericPage;

    public PageUtils(GenericPage genericPage) {
        this.genericPage = genericPage;
    }

    public AutoWebPage click(String xpath) throws IOException {

        HtmlAnchor anchor = (HtmlAnchor)genericPage.getHtmlUnitHelper().getSingleNode(xpath, genericPage.getPage());
        if (anchor==null) return null;

        return click(anchor);

    }

    public AutoWebPage click(HtmlElement anchor) throws IOException {

        if (anchor==null) return null;

        try {
            HtmlPage p = anchor.click();
            if (p==null)
                return null;
            else {
                return genericPage.getRegistry().lookup(p);
            }
        }
        catch (FailingHttpStatusCodeException e) {
            return null;
        }
    }


}
