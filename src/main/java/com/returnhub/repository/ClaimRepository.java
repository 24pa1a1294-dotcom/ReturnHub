package com.returnhub.repository;

import com.returnhub.model.Claim;
import com.returnhub.model.ClaimStatus;
import com.returnhub.model.Item;
import com.returnhub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByItemOrderByCreatedAtDesc(Item item);

    List<Claim> findByClaimantOrderByCreatedAtDesc(User claimant);

    @Query("SELECT c FROM Claim c WHERE c.item.user = :owner ORDER BY c.createdAt DESC")
    List<Claim> findClaimsForOwner(@Param("owner") User owner);

    Optional<Claim> findByItemAndClaimant(Item item, User claimant);

    boolean existsByItemAndClaimant(Item item, User claimant);

    long countByClaimant(User claimant);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.item.user = :owner AND c.status = :status")
    long countByItemUserAndStatus(@Param("owner") User owner, @Param("status") ClaimStatus status);
}
