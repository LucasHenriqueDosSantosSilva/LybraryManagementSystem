package com.lucashenrique.library.repository;
import com.lucashenrique.library.entity.Loan;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.time.LocalDate;
public interface LoanRepository extends JpaRepository<Loan,Long> {
 boolean existsByCopyIdAndReturnedDateIsNull(Long copyId);
 boolean existsByReaderId(Long readerId);
 boolean existsByCopyId(Long copyId);
 boolean existsByCopyBookId(Long bookId);
 @Query(value="SELECT copy_id FROM loans WHERE id=:id",nativeQuery=true) Optional<Long> copyId(@Param("id") Long id);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select l from Loan l where l.id=:id") Optional<Loan> locked(@Param("id") Long id);
 @Query("select l from Loan l where (:readerId is null or l.reader.id=:readerId) and (:status is null or (:status='ACTIVE' and l.returnedDate is null) or (:status='RETURNED' and l.returnedDate is not null) or (:status='OVERDUE' and l.returnedDate is null and l.dueDate<:today))")
 Page<Loan> search(@Param("readerId") Long readerId,@Param("status") String status,@Param("today") LocalDate today,Pageable pageable);
}
