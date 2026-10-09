package com.lucashenrique.library.readers.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "readers")
public class Reader {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 30)
  private String registrationNumber;

  @Column(nullable = false, length = 150)
  private String name;

  @Column(length = 254)
  private String email;

  @Column(nullable = false)
  private boolean active = true;

  public Long getId() {
    return id;
  }

  public String getRegistrationNumber() {
    return registrationNumber;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public boolean isActive() {
    return active;
  }

  public void setDetails(String registrationNumber, String name, String email) {
    this.registrationNumber = registrationNumber;
    this.name = name;
    this.email = email;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
