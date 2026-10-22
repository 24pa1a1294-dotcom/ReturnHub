package com.returnhub.controller;

import com.returnhub.model.Claim;
import com.returnhub.model.Item;
import com.returnhub.model.User;
import com.returnhub.service.ClaimService;
import com.returnhub.service.ItemService;
import com.returnhub.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ReportController {

    private final ItemService itemService;
    private final ClaimService claimService;
    private final UserService userService;

    public ReportController(ItemService itemService, ClaimService claimService, UserService userService) {
        this.itemService = itemService;
        this.claimService = claimService;
        this.userService = userService;
    }

    @GetMapping("/my-reports")
    public String myReports(Model model) {
        User currentUser = userService.getRequiredCurrentUser();

        List<Item> myItems = itemService.getItemsByUser(currentUser);
        List<Claim> receivedClaims = claimService.getClaimsForReporter(currentUser);
        List<Claim> mySubmittedClaims = claimService.getClaimsByClaimant(currentUser);

        model.addAttribute("myItems", myItems);
        model.addAttribute("receivedClaims", receivedClaims);
        model.addAttribute("mySubmittedClaims", mySubmittedClaims);

        return "reports/my-reports";
    }
}
