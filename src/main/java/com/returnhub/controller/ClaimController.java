package com.returnhub.controller;

import com.returnhub.dto.ClaimDto;
import com.returnhub.model.Claim;
import com.returnhub.model.Item;
import com.returnhub.model.ItemStatus;
import com.returnhub.model.User;
import com.returnhub.service.ClaimService;
import com.returnhub.service.ItemService;
import com.returnhub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ClaimController {

    private final ClaimService claimService;
    private final ItemService itemService;
    private final UserService userService;

    public ClaimController(ClaimService claimService, ItemService itemService, UserService userService) {
        this.claimService = claimService;
        this.itemService = itemService;
        this.userService = userService;
    }

    @GetMapping("/items/{id}/claim")
    public String showClaimForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Item item = itemService.getItemById(id);
            User currentUser = userService.getRequiredCurrentUser();

            if (item.getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You cannot claim an item that you reported.");
                return "redirect:/items/" + id;
            }

            if (item.getStatus() == ItemStatus.RESOLVED) {
                redirectAttributes.addFlashAttribute("errorMessage", "This item has already been marked as resolved/returned.");
                return "redirect:/items/" + id;
            }

            ClaimDto dto = new ClaimDto();
            dto.setItemId(item.getId());
            dto.setContactEmail(currentUser.getEmail());
            dto.setContactPhone(currentUser.getPhone());

            model.addAttribute("item", item);
            model.addAttribute("claimDto", dto);
            return "items/claim";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/items/{id}/claim")
    public String processClaim(
            @PathVariable Long id,
            @Valid @ModelAttribute("claimDto") ClaimDto claimDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Item item = itemService.getItemById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("item", item);
            return "items/claim";
        }

        try {
            claimDto.setItemId(id);
            User currentUser = userService.getRequiredCurrentUser();
            claimService.submitClaim(claimDto, currentUser);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Your claim has been submitted to the finder/reporter! They will review your proof and contact you once verified.");
            return "redirect:/items/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/items/" + id;
        }
    }

    @PostMapping("/claims/{claimId}/approve")
    public String approveClaim(
            @PathVariable Long claimId,
            @RequestParam(required = false) String responseNote,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User currentUser = userService.getRequiredCurrentUser();
            Claim claim = claimService.approveClaim(claimId, currentUser, responseNote);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Claim approved for \"" + claim.getItem().getTitle() + "\"! The item is now marked as Resolved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-reports";
    }

    @PostMapping("/claims/{claimId}/reject")
    public String rejectClaim(
            @PathVariable Long claimId,
            @RequestParam(required = false) String responseNote,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User currentUser = userService.getRequiredCurrentUser();
            Claim claim = claimService.rejectClaim(claimId, currentUser, responseNote);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Claim has been rejected for \"" + claim.getItem().getTitle() + "\".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-reports";
    }
}
