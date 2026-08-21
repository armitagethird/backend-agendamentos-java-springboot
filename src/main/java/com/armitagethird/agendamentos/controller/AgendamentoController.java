package com.armitagethird.agendamentos.controller;

import com.armitagethird.agendamentos.dto.AgendamentoCreateRequest;
import com.armitagethird.agendamentos.dto.AgendamentoResponse;
import com.armitagethird.agendamentos.dto.AgendamentoUpdateRequest;
import com.armitagethird.agendamentos.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {

        this.service = service;

    }
    @PostMapping("")
    public AgendamentoResponse criar(@Valid @RequestBody AgendamentoCreateRequest request){
        return service.criar(request);
    }
    @PutMapping("/{id}")
    public AgendamentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AgendamentoUpdateRequest request){
        return service.atualizar(id, request);
    }
    @PutMapping("/{id}/cancelar")
    public AgendamentoResponse cancelar(@PathVariable Long id){
        return service.cancelar(id);
    }
    @PutMapping("/{id}/concluir")
    public AgendamentoResponse concluir(@PathVariable Long id) {
        return service.concluir(id);
    }
    @GetMapping("/{id}")
    public AgendamentoResponse buscarPorId(@PathVariable Long id){
        return service.buscarPorId(id);
    }
}
