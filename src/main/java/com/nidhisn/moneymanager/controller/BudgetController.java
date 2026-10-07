package com.nidhisn.moneymanager.controller;

import com.nidhisn.moneymanager.dto.BudgetDTO;
import com.nidhisn.moneymanager.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/budgets")
public class BudgetController {
    private final BudgetService budgetService;

    @PutMapping
    public ResponseEntity<BudgetDTO> saveBudget(@RequestBody BudgetDTO budget) {
        return ResponseEntity.status(HttpStatus.OK).body(budgetService.saveBudget(budget));
    }

    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getBudgets(
            @RequestParam(defaultValue = "") String month) {
        return ResponseEntity.ok(budgetService.getBudgets(month.isBlank() ? YearMonth.now().toString() : month));
    }
}
