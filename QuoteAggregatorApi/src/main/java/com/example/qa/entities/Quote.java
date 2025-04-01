package com.example.qa.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quotes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String quoteText;
    private String author;
    private String category;
    private String source; // "simpsons" or "ninjas"
    private String image;
    private String characterDirection;
    private String character;
    private Integer votes = 0;
}