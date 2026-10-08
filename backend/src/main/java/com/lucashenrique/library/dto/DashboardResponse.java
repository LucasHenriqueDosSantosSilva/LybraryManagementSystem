package com.lucashenrique.library.dto;
import java.util.List;
public record DashboardResponse(long totalBooks,long totalCopies,long totalReaders,long availableCopies,long activeLoans,long overdueLoans,List<PopularBook> mostBorrowed,List<LoanResponse> recentLoans) {
 public record PopularBook(long bookId,String title,long loanCount) {}
}
