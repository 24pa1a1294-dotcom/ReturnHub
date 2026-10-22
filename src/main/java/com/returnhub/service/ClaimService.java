package com.returnhub.service;

import com.returnhub.dto.ClaimDto;
import com.returnhub.model.*;
import com.returnhub.repository.ClaimRepository;
import com.returnhub.repository.ItemRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;

    public ClaimService(ClaimRepository claimRepository, ItemRepository itemRepository) {
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
    }

    public Claim submitClaim(ClaimDto dto, User claimant) {
        if (claimant == null) {
            throw new AccessDeniedException("You must be logged in to claim an item.");
        }

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Item not found with id: " + dto.getItemId()));

        if (item.getUser().getId().equals(claimant.getId())) {
            throw new IllegalArgumentException("You cannot submit a claim on an item that you reported.");
        }

        if (item.getStatus() == ItemStatus.RESOLVED) {
            throw new IllegalStateException("This item has already been marked as resolved/returned.");
        }

        Optional<Claim> existingClaim = claimRepository.findByItemAndClaimant(item, claimant);
        if (existingClaim.isPresent() && existingClaim.get().getStatus() == ClaimStatus.PENDING) {
            throw new IllegalStateException("You already have a pending claim for this item.");
        }

        Claim claim = new Claim();
        claim.setItem(item);
        claim.setClaimant(claimant);
        claim.setProofDescription(dto.getProofDescription().trim());
        claim.setDateLost(dto.getDateLost());
        claim.setContactPhone(dto.getContactPhone().trim());
        claim.setContactEmail(dto.getContactEmail().trim().toLowerCase());
        claim.setMessage(dto.getMessage() != null ? dto.getMessage().trim() : null);
        claim.setStatus(ClaimStatus.PENDING);

        // Update item status to indicate an active claim is under review
        if (item.getStatus() == ItemStatus.OPEN) {
            item.setStatus(ItemStatus.CLAIM_PENDING);
            itemRepository.save(item);
        }

        return claimRepository.save(claim);
    }

    public Claim approveClaim(Long claimId, User reporter, String responseNote) {
        Claim claim = getClaimById(claimId);
        Item item = claim.getItem();

        if (!item.getUser().getId().equals(reporter.getId()) && reporter.getRole() != Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Only the user who reported this item can approve claims.");
        }

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setResponseNote(responseNote != null ? responseNote.trim() : "Claim approved by item reporter.");

        // Mark item as resolved/returned
        item.setStatus(ItemStatus.RESOLVED);
        itemRepository.save(item);

        // Automatically reject other pending claims for this item
        List<Claim> otherClaims = claimRepository.findByItemOrderByCreatedAtDesc(item);
        for (Claim other : otherClaims) {
            if (!other.getId().equals(claim.getId()) && other.getStatus() == ClaimStatus.PENDING) {
                other.setStatus(ClaimStatus.REJECTED);
                other.setResponseNote("Another claim was verified and approved for this item.");
                claimRepository.save(other);
            }
        }

        return claimRepository.save(claim);
    }

    public Claim rejectClaim(Long claimId, User reporter, String responseNote) {
        Claim claim = getClaimById(claimId);
        Item item = claim.getItem();

        if (!item.getUser().getId().equals(reporter.getId()) && reporter.getRole() != Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Only the user who reported this item can reject claims.");
        }

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setResponseNote(responseNote != null && !responseNote.trim().isEmpty()
                ? responseNote.trim()
                : "Claim could not be verified by the reporter.");

        Claim savedClaim = claimRepository.save(claim);

        // Check if there are remaining pending claims for this item
        List<Claim> remaining = claimRepository.findByItemOrderByCreatedAtDesc(item);
        boolean hasPending = remaining.stream().anyMatch(c -> c.getStatus() == ClaimStatus.PENDING);
        if (!hasPending && item.getStatus() == ItemStatus.CLAIM_PENDING) {
            item.setStatus(ItemStatus.OPEN);
            itemRepository.save(item);
        }

        return savedClaim;
    }

    @Transactional(readOnly = true)
    public Claim getClaimById(Long claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new NoSuchElementException("Claim not found with id: " + claimId));
    }

    @Transactional(readOnly = true)
    public List<Claim> getClaimsForItem(Item item, User user) {
        if (!item.getUser().getId().equals(user.getId()) && user.getRole() != Role.ROLE_ADMIN) {
            throw new AccessDeniedException("You are not authorized to view claims for this item.");
        }
        return claimRepository.findByItemOrderByCreatedAtDesc(item);
    }

    @Transactional(readOnly = true)
    public List<Claim> getClaimsByClaimant(User claimant) {
        return claimRepository.findByClaimantOrderByCreatedAtDesc(claimant);
    }

    @Transactional(readOnly = true)
    public List<Claim> getClaimsForReporter(User owner) {
        return claimRepository.findClaimsForOwner(owner);
    }

    @Transactional(readOnly = true)
    public Optional<Claim> getUserClaimForItem(Item item, User user) {
        if (user == null || item == null) {
            return Optional.empty();
        }
        return claimRepository.findByItemAndClaimant(item, user);
    }

    /**
     * Privacy Guard: Private contact info (phone/email) is NEVER exposed publicly.
     * It is only visible if:
     * 1. The user is the reporter of the item, OR
     * 2. The user has an APPROVED claim for this item, OR
     * 3. The user is an administrator.
     */
    @Transactional(readOnly = true)
    public boolean canViewPrivateContact(Item item, User currentUser) {
        if (currentUser == null || item == null) {
            return false;
        }

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            return true;
        }

        if (item.getUser().getId().equals(currentUser.getId())) {
            return true;
        }

        Optional<Claim> userClaim = claimRepository.findByItemAndClaimant(item, currentUser);
        return userClaim.isPresent() && userClaim.get().getStatus() == ClaimStatus.APPROVED;
    }
}
