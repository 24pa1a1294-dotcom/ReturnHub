package com.returnhub.repository;

import com.returnhub.model.Category;
import com.returnhub.model.Item;
import com.returnhub.model.ItemStatus;
import com.returnhub.model.ItemType;
import com.returnhub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByTypeOrderByCreatedAtDesc(ItemType type);

    List<Item> findByUserOrderByCreatedAtDesc(User user);

    List<Item> findTop6ByTypeOrderByCreatedAtDesc(ItemType type);

    List<Item> findTop8ByOrderByCreatedAtDesc();

    long countByType(ItemType type);

    long countByStatus(ItemStatus status);

    @Query("SELECT i FROM Item i WHERE " +
            "(:type IS NULL OR i.type = :type) AND " +
            "(:category IS NULL OR i.category = :category) AND " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY i.createdAt DESC")
    List<Item> searchItems(
            @Param("type") ItemType type,
            @Param("category") Category category,
            @Param("status") ItemStatus status,
            @Param("keyword") String keyword
    );
}
