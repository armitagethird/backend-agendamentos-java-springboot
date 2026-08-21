package com.armitagethird.agendamentos.service;


import com.armitagethird.agendamentos.dto.AgendamentoCreateRequest;
import com.armitagethird.agendamentos.dto.AgendamentoResponse;
import com.armitagethird.agendamentos.dto.AgendamentoUpdateRequest;
import com.armitagethird.agendamentos.mapper.AgendamentoMapper;
import com.armitagethird.agendamentos.model.Agendamento;
import com.armitagethird.agendamentos.model.StatusAgendamento;
import com.armitagethird.agendamentos.repository.AgendamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AgendamentoService {

    private final AgendamentoRepository repo;

    public AgendamentoService(AgendamentoRepository repo){
        this.repo = repo;
    }
    @Transactional
    public AgendamentoResponse criar(AgendamentoCreateRequest req) {

        validarIntervalo(req.dataInicio(), req.dataFim());
        checarConflito(req.usuario(), req.dataInicio(), req.dataFim(), null);
        Agendamento entity = AgendamentoMapper.toEntity(req);
        entity = repo.save(entity);
        return AgendamentoMapper.toResponse(entity);

    }
    @Transactional
    public AgendamentoResponse atualizar(Long id, AgendamentoUpdateRequest req){
        Agendamento entity = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado"));
        AgendamentoMapper.merge(entity, req);
        validarIntervalo(entity.getDataInicio(), entity.getDataFim());
        checarConflito(entity.getUsuario(), entity.getDataInicio(), entity.getDataFim(), entity.getId());

        entity = repo.save(entity);
        return AgendamentoMapper.toResponse(entity);
    }

    @Transactional
    public AgendamentoResponse cancelar(Long id){
        Agendamento entity = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado"));
        entity.setStatus(StatusAgendamento.CANCELADO);
        entity = repo.save(entity);
        return  AgendamentoMapper.toResponse(entity);
    }

    @Transactional
    public AgendamentoResponse concluir(Long id){
        Agendamento entity = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado"));
        entity.setStatus(StatusAgendamento.CONCLUIDO);
        entity = repo.save(entity);
        return  AgendamentoMapper.toResponse(entity);
    }

    public AgendamentoResponse buscarPorId(Long id){
        Agendamento a = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado"));
        return AgendamentoMapper.toResponse(a);
    }

    private void validarIntervalo(LocalDateTime inicio, LocalDateTime fim){
        if (inicio==null || fim == null || !inicio.isBefore(fim)) {
            throw new IllegalArgumentException("Intervalo inválido: dataInicio deve ser anterior ao dataFim");
        }
    }

    private void checarConflito(String usuario, LocalDateTime inicio, LocalDateTime fim, Long id){
        if (repo.existConflito(usuario, inicio, fim, id)){
            throw new IllegalArgumentException("Intervalo na agenda: já existe um agendamento nesse periodo");
        }

    }
}
