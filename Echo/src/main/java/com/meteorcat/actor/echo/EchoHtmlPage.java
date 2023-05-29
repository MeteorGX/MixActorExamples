package com.meteorcat.actor.echo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面功能
 * @author MeteorCat
 */
@Controller
public class EchoHtmlPage {


    /**
     * 功能页面
     * @return ViewPage
     */
    @GetMapping({"/",""})
    public String index(){
        return "index";
    }

}
