package com.nidhisn.moneymanager.service;

import com.nidhisn.moneymanager.dto.BudgetDTO;
import com.nidhisn.moneymanager.entity.BudgetEntity;
import com.nidhisn.moneymanager.entity.CategoryEntity;
import com.nidhisn.moneymanager.entity.ExpenseEntity;
import com.nidhisn.moneymanager.entity.ProfileEntity;
import com.nidhisn.moneymanager.repository.BudgetRepository;
import com.nidhisn.moneymanager.repository.CategoryRepository;
import com.nidhisn.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private static final BigDecimal NEAR_LIMIT = new BigDecimal("80");

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    public BudgetDTO saveBudget(BudgetDTO request) {
        ProfileEntity profile = profileService.getCurrentProfile();
        YearMonth month = parseMonth(request.getMonth());
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Budget amount must be greater than zero");
        }
        CategoryEntity category = categoryRepository.findByIdAndProfileId(request.getCategoryId(), profile.getId())
                .filter(c -> "expense".equalsIgnoreCase(c.getType()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Choose an expense category belonging to your account"));

        BudgetEntity budget = budgetRepository.findByProfileIdAndCategoryIdAndMonthStart(
                        profile.getId(), category.getId(), month.atDay(1))
                .orElseGet(() -> BudgetEntity.builder().profile(profile).category(category)
                        .monthStart(month.atDay(1)).build());
        budget.setAmount(request.getAmount());
        BudgetEntity saved = budgetRepository.save(budget);
        return getBudgets(month.toString()).stream()
                .filter(item -> item.getCategoryId().equals(saved.getCategory().getId()))
                .findFirst().orElseThrow();
    }

    public List<BudgetDTO> getBudgets(String monthText) {
        ProfileEntity profile = profileService.getCurrentProfile();
        YearMonth month = parseMonth(monthText);
        LocalDate start = month.atDay(1);
        Map<Long, BigDecimal> spentByCategory = expenseRepository
                .findByProfileIdAndDateBetween(profile.getId(), start, month.atEndOfMonth())
                .stream().collect(Collectors.groupingBy(e -> e.getCategory().getId(),
                        Collectors.mapping(ExpenseEntity::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        return budgetRepository.findByProfileIdAndMonthStart(profile.getId(), start).stream()
                .map(b -> toDTO(b, spentByCategory.getOrDefault(b.getCategory().getId(), BigDecimal.ZERO)))
                .toList();
    }

    private BudgetDTO toDTO(BudgetEntity budget, BigDecimal spent) {
        BigDecimal remaining = budget.getAmount().subtract(spent);
        BigDecimal utilization = spent.multiply(new BigDecimal("100"))
                .divide(budget.getAmount(), 2, RoundingMode.HALF_UP);
        String status = spent.compareTo(budget.getAmount()) > 0 ? "OVER_BUDGET"
                : utilization.compareTo(NEAR_LIMIT) >= 0 ? "NEAR_LIMIT" : "ON_TRACK";
        return BudgetDTO.builder().id(budget.getId()).categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName()).month(YearMonth.from(budget.getMonthStart()).toString())
                .amount(budget.getAmount()).spent(spent).remaining(remaining.max(BigDecimal.ZERO))
                .utilizationPercent(utilization).status(status).build();
    }

    private YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Month must use YYYY-MM format");
        }
    }
}
