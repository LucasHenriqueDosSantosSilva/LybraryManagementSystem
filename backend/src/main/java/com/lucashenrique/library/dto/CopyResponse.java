package com.lucashenrique.library.dto;
public record CopyResponse(Long id,String inventoryCode,Long bookId,String circulationStatus,boolean available) {}
