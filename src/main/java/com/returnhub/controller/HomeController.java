package com.returnhub.controller;

import com.returnhub.model.ItemStatus;
import com.returnhub.model.ItemType;
import com.returnhub.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ItemService itemService;

    public HomeController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping({"/", "/home"})
    public String index(Model model) {
        model.addAttribute("recentLostItems", itemService.getRecentItems(ItemType.LOST, 4));
        model.addAttribute("recentFoundItems", itemService.getRecentItems(ItemType.FOUND, 4));

        long lostCount = itemService.countByType(ItemType.LOST);
        long foundCount = itemService.countByType(ItemType.FOUND);
        long resolvedCount = itemService.countByStatus(ItemStatus.RESOLVED);

        model.addAttribute("totalLostCount", lostCount);
        model.addAttribute("totalFoundCount", foundCount);
        model.addAttribute("totalResolvedCount", resolvedCount);
        model.addAttribute("totalItemsCount", lostCount + foundCount);

        return "home";
    }
}
