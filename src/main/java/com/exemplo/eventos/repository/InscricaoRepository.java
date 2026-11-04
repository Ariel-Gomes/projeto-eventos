package com.exemplo.eventos.repository;

import com.exemplo.eventos.model.Inscricao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InscricaoRepository
        extends JpaRepository<Inscricao, Long> {

    List<Inscricao> findByEvento_IdOrderByDataInscricaoAsc(
            Long eventoId);

    Optional<Inscricao>
    findByEmailIgnoreCaseAndCodigoCancelamento(
            String email,
            String codigoCancelamento);
}