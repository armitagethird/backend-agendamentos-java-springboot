package com.armitagethird.agendamentos.mapper;

import com.armitagethird.agendamentos.dto.AgendamentoCreateRequest;
import com.armitagethird.agendamentos.dto.AgendamentoResponse;
import com.armitagethird.agendamentos.dto.AgendamentoUpdateRequest;
import com.armitagethird.agendamentos.model.Agendamento;
import com.armitagethird.agendamentos.model.StatusAgendamento;

import java.time.LocalDateTime;

public class AgendamentoMapper {

    public static Agendamento toEntity(AgendamentoCreateRequest req) {
            return Agendamento.builder()
                .titulo(req.titulo())
                .descricao(req.descricao())
                .dataFim(req.dataFim())
                .dataInicio(req.dataInicio())
                .usuario(req.usuario())
                .status(StatusAgendamento.AGENDADO)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
    }

    public static void merge(Agendamento entity, AgendamentoUpdateRequest req) {
        if (req.titulo() != null){
            entity.setTitulo(req.titulo());
        }
        if (req.descricao() != null){
            entity.setDescricao(req.descricao());
        }
        if (req.dataInicio() != null){
            entity.setDataInicio(req.dataInicio());
        }
        if (req.dataFim() != null){
            entity.setDataFim(req.dataFim());
        }
    }

    public static AgendamentoResponse toResponse(Agendamento a){
        return new AgendamentoResponse(
                a.getId(),
                a.getTitulo(),
                a.getDescricao(),
                a.getDataInicio(),
                a.getDataFim(),
                a.getStatus(),
                a.getUsuario(),
                a.getCriadoEm(),
                a.getAtualizadoEm()

        );
    }
}
