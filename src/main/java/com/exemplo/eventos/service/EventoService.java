package com.exemplo.eventos.service;

import com.exemplo.eventos.dto.InscricaoDto;
import com.exemplo.eventos.model.Evento;
import com.exemplo.eventos.model.Inscricao;
import com.exemplo.eventos.repository.EventoRepository;
import com.exemplo.eventos.repository.InscricaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.SequencedCollection;
import java.util.UUID;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final InscricaoRepository inscricaoRepository;

    public EventoService(
            EventoRepository eventoRepository,
            InscricaoRepository inscricaoRepository) {
        this.eventoRepository = eventoRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    public SequencedCollection<Evento> listarEventosOrdenados() {
        return eventoRepository.findAllByOrderByDataEventoAsc();
    }

    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Evento não encontrado."));
    }

    @Transactional
    public void salvarEvento(Evento dadosFormulario) {

        if (dadosFormulario.getTitulo() == null
                || dadosFormulario.getTitulo().isBlank()) {
            throw new IllegalArgumentException("Informe o título do evento.");
        }

        if (dadosFormulario.getDataEvento() == null) {
            throw new IllegalArgumentException("Informe a data do evento.");
        }

        if (dadosFormulario.getVagasTotais() == null
                || dadosFormulario.getVagasTotais() < 1) {
            throw new IllegalArgumentException(
                    "O evento precisa ter pelo menos uma vaga.");
        }

        if (dadosFormulario.getId() == null) {
            dadosFormulario.setVagasDisponiveis(
                    dadosFormulario.getVagasTotais());
            eventoRepository.save(dadosFormulario);
            return;
        }

        Evento existente = buscarPorId(dadosFormulario.getId());

        int inscritos = existente.getVagasTotais()
                - existente.getVagasDisponiveis();

        if (dadosFormulario.getVagasTotais() < inscritos) {
            throw new IllegalArgumentException(
                    "O total de vagas não pode ser menor que o número de inscritos.");
        }

        existente.setTitulo(dadosFormulario.getTitulo().trim());
        existente.setDataEvento(dadosFormulario.getDataEvento());
        existente.setVagasTotais(dadosFormulario.getVagasTotais());
        existente.setVagasDisponiveis(
                dadosFormulario.getVagasTotais() - inscritos);

        eventoRepository.save(existente);
    }

    @Transactional
    public void deletarEvento(Long id) {
        eventoRepository.deleteById(id);
    }

    // REALIZAR INSCRIÇÃO E GERAR CÓDIGO INDIVIDUAL
    @Transactional
    public String realizarInscricao(InscricaoDto dto) {

        Evento evento = buscarPorId(dto.eventoId());

        if (evento.getVagasDisponiveis() == null
                || evento.getVagasDisponiveis() <= 0) {
            throw new IllegalStateException(
                    "O evento está lotado!");
        }

        Inscricao inscricao = new Inscricao();

        inscricao.setEvento(evento);
        inscricao.setNomeParticipante(
                dto.nomeParticipante().trim());
        inscricao.setEmail(dto.email().trim());

        String codigo = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();

        inscricao.setCodigoCancelamento(codigo);

        evento.setVagasDisponiveis(
                evento.getVagasDisponiveis() - 1);

        inscricaoRepository.save(inscricao);
        eventoRepository.save(evento);

        return codigo;
    }

    // LISTAR PARTICIPANTES DE UM EVENTO
    @Transactional(readOnly = true)
    public List<Inscricao> listarInscricoesDoEvento(Long eventoId) {

        buscarPorId(eventoId);

        return inscricaoRepository
                .findByEvento_IdOrderByDataInscricaoAsc(eventoId);
    }

    // Atualiza nome e e-mail de um participante
    @Transactional
    public void atualizarInscricaoAdmin(
            Long inscricaoId, String nome, String email) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o nome do participante.");
        }

        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException(
                    "Informe um e-mail válido.");
        }

        Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inscrição não encontrada."));

        inscricao.setNomeParticipante(nome.trim());
        inscricao.setEmail(email.trim());

        inscricaoRepository.save(inscricao);
    }

    // Remove a inscrição e devolve uma vaga ao evento
    @Transactional
    public void removerInscricaoAdmin(Long inscricaoId) {

        Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inscrição não encontrada."));

        Evento evento = inscricao.getEvento();

        inscricaoRepository.delete(inscricao);

        int vagasAtuais = evento.getVagasDisponiveis();
        int total = evento.getVagasTotais();

        evento.setVagasDisponiveis(
                Math.min(total, vagasAtuais + 1));

        eventoRepository.save(evento);
    }
    // CANCELAR INSCRIÇÃO E DEVOLVER A VAGA
    @Transactional
    public void cancelarInscricao(
            String email,
            String codigoCancelamento) {

        if (email == null || email.isBlank()
                || codigoCancelamento == null
                || codigoCancelamento.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o e-mail e o código de cancelamento.");
        }

        Inscricao inscricao = inscricaoRepository
                .findByEmailIgnoreCaseAndCodigoCancelamento(
                        email.trim(),
                        codigoCancelamento.trim().toUpperCase())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Inscrição não encontrada. Confira os dados."));

        Evento evento = inscricao.getEvento();

        inscricaoRepository.delete(inscricao);

        int vagasAtuais = evento.getVagasDisponiveis() == null
                ? 0 : evento.getVagasDisponiveis();

        int total = evento.getVagasTotais() == null
                ? vagasAtuais : evento.getVagasTotais();

        evento.setVagasDisponiveis(
                Math.min(total, vagasAtuais + 1));

        eventoRepository.save(evento);
    }
}