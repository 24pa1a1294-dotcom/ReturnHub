package com.returnhub.service;

import com.returnhub.dto.ItemReportDto;
import com.returnhub.model.*;
import com.returnhub.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ItemService itemService;

    private User user;
    private ItemReportDto reportDto;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@example.com", "pass", "+1555123456");
        user.setId(1L);

        reportDto = new ItemReportDto();
        reportDto.setTitle("Lost Black Backpack");
        reportDto.setDescription("Black North Face backpack with laptop inside");
        reportDto.setCategory(Category.WALLETS_BAGS);
        reportDto.setType(ItemType.LOST);
        reportDto.setLocation("Library 1st floor");
        reportDto.setEventDate(LocalDate.now().minusDays(1));
        reportDto.setContactPhone("+1555123456");
        reportDto.setContactEmail("alice@example.com");
    }

    @Test
    @DisplayName("Should successfully create a new lost item")
    void testCreateItem_Success() {
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item i = invocation.getArgument(0);
            i.setId(10L);
            return i;
        });

        Item item = itemService.createItem(reportDto, user);

        assertThat(item).isNotNull();
        assertThat(item.getId()).isEqualTo(10L);
        assertThat(item.getTitle()).isEqualTo("Lost Black Backpack");
        assertThat(item.getStatus()).isEqualTo(ItemStatus.OPEN);
        assertThat(item.getUser()).isEqualTo(user);
        assertThat(item.getContactPhone()).isEqualTo("+1555123456");

        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("Should filter items using search parameters")
    void testSearchItems() {
        Item item = new Item();
        item.setTitle("Lost Black Backpack");
        item.setType(ItemType.LOST);
        item.setCategory(Category.WALLETS_BAGS);
        item.setStatus(ItemStatus.OPEN);

        when(itemRepository.searchItems(ItemType.LOST, Category.WALLETS_BAGS, ItemStatus.OPEN, "Backpack"))
                .thenReturn(Collections.singletonList(item));

        List<Item> results = itemService.searchItems(ItemType.LOST, Category.WALLETS_BAGS, ItemStatus.OPEN, "Backpack");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Lost Black Backpack");
    }

    @Test
    @DisplayName("Should prevent unauthorized users from deleting an item")
    void testDeleteItem_Unauthorized() {
        Item item = new Item();
        item.setId(5L);
        item.setUser(user);

        User stranger = new User("Bob", "bob@example.com", "pass", "999");
        stranger.setId(2L);

        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.deleteItem(5L, stranger))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not authorized");

        verify(itemRepository, never()).delete(any(Item.class));
    }
}
