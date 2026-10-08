package com.lucashenrique.library.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name="loans")
public class Loan {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="reader_id") private Reader reader;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="copy_id") private BookCopy copy;
 @Column(nullable=false) private LocalDate loanDate;
 @Column(nullable=false) private LocalDate dueDate;
 private LocalDate returnedDate;
 protected Loan(){}
 public Loan(Reader reader,BookCopy copy,LocalDate date,LocalDate due){this.reader=reader;this.copy=copy;this.loanDate=date;this.dueDate=due;}
 public Long getId(){return id;}public Reader getReader(){return reader;}public BookCopy getCopy(){return copy;}
 public LocalDate getLoanDate(){return loanDate;}public LocalDate getDueDate(){return dueDate;}public LocalDate getReturnedDate(){return returnedDate;}
 public void returnOn(LocalDate date){this.returnedDate=date;}
}
