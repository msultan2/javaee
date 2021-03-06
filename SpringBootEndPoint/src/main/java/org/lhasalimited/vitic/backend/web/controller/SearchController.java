package org.lhasalimited.vitic.backend.web.controller;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import model.Structure;

@Controller
@RequestMapping("/search")
public class SearchController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(method=RequestMethod.GET)
    public @ResponseBody Structure sayHello(@RequestParam(value="name", required=false, defaultValue="Stranger") String name) {
        return new Structure(counter.incrementAndGet(), String.format(template, name));
    }

}
