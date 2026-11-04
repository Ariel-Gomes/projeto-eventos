package com.exemplo.eventos.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscricoes")
@Data
public class Inscricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(name = "nome_participante")
    private String nomeParticipante;

    private String email;

    @Column(name = "codigo_cancelamento", unique = true)
    private String codigoCancelamento;

    @Column(name = "data_inscricao")
    private LocalDateTime dataInscricao = LocalDateTime.now();
}