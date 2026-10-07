package com.nidhisn.moneymanager.repository;

import com.nidhisn.moneymanager.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {
    List<BudgetEntity> findByProfileIdAndMonthStart(Long profileId, LocalDate monthStart);
    Optional<BudgetEntity> findByProfileIdAndCategoryIdAndMonthStart(Long profileId, Long categoryId, LocalDate monthStart);
}
