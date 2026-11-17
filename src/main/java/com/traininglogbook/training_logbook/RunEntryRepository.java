package com.traininglogbook.training_logbook;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RunEntryRepository extends JpaRepository<RunEntry, Long> {

    List<RunEntry> findByRunDateBetween(LocalDate startDate, LocalDate endDate);
}
