package com.traininglogbook.training_logbook;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Extending JPARepository gives us built-in save(), findALL(), and deleteById() methods without writing SQL ourselves
@Repository
public interface RunEntryRepository extends JpaRepository<RunEntry, Long> {

    // Spring automatically turns this method name into: SELECT * FROM run_entries WHERE run_date BETWEEN startDate AND endDate
    List<RunEntry> findByRunDateBetween(LocalDate startDate, LocalDate endDate);
}
