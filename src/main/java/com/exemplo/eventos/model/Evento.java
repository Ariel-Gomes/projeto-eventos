package com.exemplo.eventos.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eventos")
@Data
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Column(name = "data_evento")
    private LocalDateTime dataEvento;

    @Column(name = "vagas_totais")
    private Integer vagasTotais;

    @Column(name = "vagas_disponiveis")
    private Integer vagasDisponiveis;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inscricao> inscricoes = new ArrayList<>();
}