package org.kow.controller;

import org.kow.domain.Jjal;
import org.kow.service.GoogleSheetUserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BotResourceController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    GoogleSheetUserServiceImpl googleSheetUserService;

    @RequestMapping("/jjals")
    public List<String> getAllJjals() {
        return googleSheetUserService.getJjals();
    }

    @RequestMapping(value = "/jjal", method = RequestMethod.POST)
    public void addJjal(@RequestBody Jjal jjal) {
        logger.info(jjal.toString());
        googleSheetUserService.addJjal(jjal.getContent());
    }

}
