package com.example.qa.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "votes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "quote_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @Column(name = "user_id", nullable = false)
    private String userId;
}

