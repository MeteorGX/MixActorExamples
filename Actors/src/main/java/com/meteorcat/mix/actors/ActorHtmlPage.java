package com.meteorcat.mix.actors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Actor调用页面
 * @author MeteorCat
 */
@Controller
public class ActorHtmlPage {
    /**
     * 功能页面
     * @return ViewPage
     */
    @GetMapping({"/",""})
    public String index(){
        return "index";
    }
}
