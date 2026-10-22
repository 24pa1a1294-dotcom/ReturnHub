package com.returnhub.controller;

import com.returnhub.dto.ItemReportDto;
import com.returnhub.model.*;
import com.returnhub.service.ClaimService;
import com.returnhub.service.ItemService;
import com.returnhub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class ItemController {

    private final ItemService itemService;
    private final ClaimService claimService;
    private final UserService userService;

    public ItemController(ItemService itemService, ClaimService claimService, UserService userService) {
        this.itemService = itemService;
        this.claimService = claimService;
        this.userService = userService;
    }

    @GetMapping("/lost-items")
    public String listLostItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) ItemStatus status,
            Model model
    ) {
        List<Item> items = itemService.searchItems(ItemType.LOST, category, status, keyword);
        model.addAttribute("items", items);
        model.addAttribute("pageTitle", "Lost Items");
        model.addAttribute("itemType", ItemType.LOST);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        return "items/list";
    }

    @GetMapping("/found-items")
    public String listFoundItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) ItemStatus status,
            Model model
    ) {
        List<Item> items = itemService.searchItems(ItemType.FOUND, category, status, keyword);
        model.addAttribute("items", items);
        model.addAttribute("pageTitle", "Found Items");
        model.addAttribute("itemType", ItemType.FOUND);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        return "items/list";
    }

    @GetMapping("/search")
    public String generalSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) ItemStatus status,
            Model model
    ) {
        List<Item> items = itemService.searchItems(type, category, status, keyword);
        model.addAttribute("items", items);
        model.addAttribute("pageTitle", "Search Results");
        model.addAttribute("itemType", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        return "items/list";
    }

    @GetMapping("/items/{id}")
    public String itemDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Item item = itemService.getItemById(id);
            User currentUser = userService.getCurrentUser();

            boolean isOwner = currentUser != null && item.getUser().getId().equals(currentUser.getId());
            boolean canViewContact = claimService.canViewPrivateContact(item, currentUser);
            Optional<Claim> userClaim = claimService.getUserClaimForItem(item, currentUser);

            model.addAttribute("item", item);
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("canViewContact", canViewContact);
            model.addAttribute("userClaim", userClaim.orElse(null));

            return "items/details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Item not found.");
            return "redirect:/";
        }
    }

    @GetMapping("/items/report/lost")
    public String reportLostForm(Model model) {
        User currentUser = userService.getRequiredCurrentUser();
        ItemReportDto dto = new ItemReportDto();
        dto.setType(ItemType.LOST);
        dto.setContactEmail(currentUser.getEmail());
        dto.setContactPhone(currentUser.getPhone());

        model.addAttribute("itemDto", dto);
        model.addAttribute("formType", "LOST");
        model.addAttribute("formTitle", "Report a Lost Item");
        return "items/report";
    }

    @GetMapping("/items/report/found")
    public String reportFoundForm(Model model) {
        User currentUser = userService.getRequiredCurrentUser();
        ItemReportDto dto = new ItemReportDto();
        dto.setType(ItemType.FOUND);
        dto.setContactEmail(currentUser.getEmail());
        dto.setContactPhone(currentUser.getPhone());

        model.addAttribute("itemDto", dto);
        model.addAttribute("formType", "FOUND");
        model.addAttribute("formTitle", "Report a Found Item");
        return "items/report";
    }

    @PostMapping("/items/report")
    public String processReport(
            @Valid @ModelAttribute("itemDto") ItemReportDto itemDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formType", itemDto.getType() != null ? itemDto.getType().name() : "LOST");
            model.addAttribute("formTitle", itemDto.getType() == ItemType.FOUND ? "Report a Found Item" : "Report a Lost Item");
            return "items/report";
        }

        User currentUser = userService.getRequiredCurrentUser();
        Item createdItem = itemService.createItem(itemDto, currentUser);

        redirectAttributes.addFlashAttribute("successMessage",
                "Your " + createdItem.getType().getDisplayName().toLowerCase() + " item report has been published successfully!");
        return "redirect:/items/" + createdItem.getId();
    }

    @GetMapping("/items/{id}/edit")
    public String editItemForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Item item = itemService.getItemById(id);
            User currentUser = userService.getRequiredCurrentUser();

            if (!itemService.canUserModifyItem(item, currentUser)) {
                redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to edit this item.");
                return "redirect:/items/" + id;
            }

            ItemReportDto dto = new ItemReportDto();
            dto.setId(item.getId());
            dto.setTitle(item.getTitle());
            dto.setDescription(item.getDescription());
            dto.setCategory(item.getCategory());
            dto.setType(item.getType());
            dto.setLocation(item.getLocation());
            dto.setEventDate(item.getEventDate());
            dto.setContactPhone(item.getContactPhone());
            dto.setContactEmail(item.getContactEmail());
            dto.setAdditionalContactInfo(item.getAdditionalContactInfo());
            dto.setImageUrl(item.getImageUrl());

            model.addAttribute("itemDto", dto);
            model.addAttribute("formType", item.getType().name());
            model.addAttribute("formTitle", "Edit " + item.getTitle());
            model.addAttribute("isEdit", true);
            model.addAttribute("existingImageUrl", item.getImageUrl());

            return "items/report";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/my-reports";
        }
    }

    @PostMapping("/items/{id}/edit")
    public String processEdit(
            @PathVariable Long id,
            @Valid @ModelAttribute("itemDto") ItemReportDto itemDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formType", itemDto.getType() != null ? itemDto.getType().name() : "LOST");
            model.addAttribute("formTitle", "Edit Item");
            model.addAttribute("isEdit", true);
            return "items/report";
        }

        try {
            User currentUser = userService.getRequiredCurrentUser();
            Item updated = itemService.updateItem(id, itemDto, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Item updated successfully!");
            return "redirect:/items/" + updated.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/items/" + id;
        }
    }

    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getRequiredCurrentUser();
            itemService.deleteItem(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Item deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-reports";
    }
}
