package com.returnhub.service;

import com.returnhub.dto.ItemReportDto;
import com.returnhub.model.*;
import com.returnhub.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final FileStorageService fileStorageService;

    public ItemService(ItemRepository itemRepository, FileStorageService fileStorageService) {
        this.itemRepository = itemRepository;
        this.fileStorageService = fileStorageService;
    }

    public Item createItem(ItemReportDto dto, User user) {
        Item item = new Item();
        item.setTitle(dto.getTitle().trim());
        item.setDescription(dto.getDescription().trim());
        item.setCategory(dto.getCategory());
        item.setType(dto.getType());
        item.setLocation(dto.getLocation().trim());
        item.setEventDate(dto.getEventDate());
        item.setContactPhone(dto.getContactPhone().trim());
        item.setContactEmail(dto.getContactEmail().trim().toLowerCase());
        item.setAdditionalContactInfo(dto.getAdditionalContactInfo() != null ? dto.getAdditionalContactInfo().trim() : null);
        item.setStatus(ItemStatus.OPEN);
        item.setUser(user);

        // Process uploaded image if present
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String uploadedUrl = fileStorageService.storeFile(dto.getImageFile());
            item.setImageUrl(uploadedUrl);
        } else if (dto.getImageUrl() != null && !dto.getImageUrl().trim().isEmpty()) {
            item.setImageUrl(dto.getImageUrl().trim());
        }

        return itemRepository.save(item);
    }

    public Item updateItem(Long id, ItemReportDto dto, User currentUser) {
        Item item = getItemById(id);

        if (!canUserModifyItem(item, currentUser)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to edit this item.");
        }

        item.setTitle(dto.getTitle().trim());
        item.setDescription(dto.getDescription().trim());
        item.setCategory(dto.getCategory());
        item.setLocation(dto.getLocation().trim());
        item.setEventDate(dto.getEventDate());
        item.setContactPhone(dto.getContactPhone().trim());
        item.setContactEmail(dto.getContactEmail().trim().toLowerCase());
        item.setAdditionalContactInfo(dto.getAdditionalContactInfo() != null ? dto.getAdditionalContactInfo().trim() : null);

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String uploadedUrl = fileStorageService.storeFile(dto.getImageFile());
            item.setImageUrl(uploadedUrl);
        } else if (dto.getImageUrl() != null && !dto.getImageUrl().trim().isEmpty()) {
            item.setImageUrl(dto.getImageUrl().trim());
        }

        return itemRepository.save(item);
    }

    public void deleteItem(Long id, User currentUser) {
        Item item = getItemById(id);

        if (!canUserModifyItem(item, currentUser)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to delete this item.");
        }

        itemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Item> searchItems(ItemType type, Category category, ItemStatus status, String keyword) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return itemRepository.searchItems(type, category, status, cleanKeyword);
    }

    @Transactional(readOnly = true)
    public List<Item> getItemsByType(ItemType type) {
        return itemRepository.findByTypeOrderByCreatedAtDesc(type);
    }

    @Transactional(readOnly = true)
    public List<Item> getRecentItems(ItemType type, int limit) {
        if (type == null) {
            return itemRepository.findTop8ByOrderByCreatedAtDesc();
        }
        return itemRepository.findTop6ByTypeOrderByCreatedAtDesc(type);
    }

    @Transactional(readOnly = true)
    public List<Item> getItemsByUser(User user) {
        return itemRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public long countByType(ItemType type) {
        return itemRepository.countByType(type);
    }

    @Transactional(readOnly = true)
    public long countByStatus(ItemStatus status) {
        return itemRepository.countByStatus(status);
    }

    public boolean canUserModifyItem(Item item, User user) {
        if (user == null || item == null) {
            return false;
        }
        return user.getId().equals(item.getUser().getId()) || user.getRole() == Role.ROLE_ADMIN;
    }
}
