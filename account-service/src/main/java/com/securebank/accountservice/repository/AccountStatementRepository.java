package com.securebank.accountservice.repository;

import com.securebank.accountservice.entity.AccountStatement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccountStatementRepository extends JpaRepository<AccountStatement,Long> {

    List<AccountStatement> findTop10ByAccountIdOrderByCreatedAtDesc(Long accountId);
    Page<AccountStatement> findByAccountIdAndCreatedAtBetween(
            Long accountId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);
}
