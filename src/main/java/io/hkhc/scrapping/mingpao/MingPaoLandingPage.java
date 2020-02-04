package io.hkhc.scrapping.mingpao;

import io.hkhc.autoweb.spi.commons.GenericGetPage;

import java.io.IOException;

public class MingPaoLandingPage extends GenericGetPage {

    private PageUtils pageUtils;

    public MingPaoLandingPage() {
        pageUtils = new PageUtils(this);
    }

    public MingPaoHomePage loginPage() throws IOException {

        System.out.println("Looking for login page");

        return (MingPaoHomePage)pageUtils.click("//li[@id='headerlogin']/a");

    }


}
