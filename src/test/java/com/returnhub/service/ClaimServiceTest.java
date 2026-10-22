package com.returnhub.service;

import com.returnhub.dto.ClaimDto;
import com.returnhub.model.*;
import com.returnhub.repository.ClaimRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ClaimService claimService;

    private User owner;
    private User claimant;
    private Item item;
    private ClaimDto claimDto;

    @BeforeEach
    void setUp() {
        owner = new User("Owner User", "owner@example.com", "pass", "+111111111");
        owner.setId(1L);

        claimant = new User("Claimant User", "claimant@example.com", "pass", "+222222222");
        claimant.setId(2L);

        item = new Item();
        item.setId(10L);
        item.setTitle("Found Watch");
        item.setDescription("Silver mechanical watch found in park");
        item.setCategory(Category.JEWELRY_ACCESSORIES);
        item.setType(ItemType.FOUND);
        item.setLocation("Central Park");
        item.setEventDate(LocalDate.now().minusDays(1));
        item.setContactPhone("+111111111");
        item.setContactEmail("owner@example.com");
        item.setStatus(ItemStatus.OPEN);
        item.setUser(owner);

        claimDto = new ClaimDto();
        claimDto.setItemId(10L);
        claimDto.setProofDescription("Engraving on the back says 'For John 2020'");
        claimDto.setDateLost(LocalDate.now().minusDays(1));
        claimDto.setContactPhone("+222222222");
        claimDto.setContactEmail("claimant@example.com");
        claimDto.setMessage("This was a gift from my family.");
    }

    @Test
    @DisplayName("Should submit claim and change item status to CLAIM_PENDING")
    void testSubmitClaim_Success() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(claimRepository.findByItemAndClaimant(item, claimant)).thenReturn(Optional.empty());
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> {
            Claim c = invocation.getArgument(0);
            c.setId(100L);
            return c;
        });

        Claim claim = claimService.submitClaim(claimDto, claimant);

        assertThat(claim).isNotNull();
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.PENDING);
        assertThat(claim.getProofDescription()).contains("Engraving");
        assertThat(item.getStatus()).isEqualTo(ItemStatus.CLAIM_PENDING);

        verify(itemRepository).save(item);
        verify(claimRepository).save(any(Claim.class));
    }

    @Test
    @DisplayName("Should reject claim submission when user attempts to claim their own item")
    void testSubmitClaim_CannotClaimOwnItem() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> claimService.submitClaim(claimDto, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot submit a claim on an item that you reported");

        verify(claimRepository, never()).save(any(Claim.class));
    }

    @Test
    @DisplayName("Should approve claim, resolve item, and reject other pending claims")
    void testApproveClaim_Success() {
        Claim claim = new Claim();
        claim.setId(100L);
        claim.setItem(item);
        claim.setClaimant(claimant);
        claim.setStatus(ClaimStatus.PENDING);

        when(claimRepository.findById(100L)).thenReturn(Optional.of(claim));
        when(claimRepository.findByItemOrderByCreatedAtDesc(item)).thenReturn(Collections.singletonList(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Claim approved = claimService.approveClaim(100L, owner, "Proof matched engraving");

        assertThat(approved.getStatus()).isEqualTo(ClaimStatus.APPROVED);
        assertThat(approved.getResponseNote()).isEqualTo("Proof matched engraving");
        assertThat(item.getStatus()).isEqualTo(ItemStatus.RESOLVED);

        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("Privacy Guard: Contact info is shielded from public and revealed only to owner or approved claimant")
    void testPrivacyGuard_CanViewPrivateContact() {
        // 1. Unauthenticated or null user cannot view private contact
        assertThat(claimService.canViewPrivateContact(item, null)).isFalse();

        // 2. Unrelated user without approved claim cannot view private contact
        User stranger = new User("Stranger", "stranger@example.com", "pass", "123");
        stranger.setId(99L);
        when(claimRepository.findByItemAndClaimant(item, stranger)).thenReturn(Optional.empty());
        assertThat(claimService.canViewPrivateContact(item, stranger)).isFalse();

        // 3. Item owner CAN view private contact
        assertThat(claimService.canViewPrivateContact(item, owner)).isTrue();

        // 4. Claimant with PENDING claim CANNOT view private contact yet
        Claim pendingClaim = new Claim();
        pendingClaim.setStatus(ClaimStatus.PENDING);
        when(claimRepository.findByItemAndClaimant(item, claimant)).thenReturn(Optional.of(pendingClaim));
        assertThat(claimService.canViewPrivateContact(item, claimant)).isFalse();

        // 5. Claimant with APPROVED claim CAN view private contact
        Claim approvedClaim = new Claim();
        approvedClaim.setStatus(ClaimStatus.APPROVED);
        when(claimRepository.findByItemAndClaimant(item, claimant)).thenReturn(Optional.of(approvedClaim));
        assertThat(claimService.canViewPrivateContact(item, claimant)).isTrue();
    }
}
