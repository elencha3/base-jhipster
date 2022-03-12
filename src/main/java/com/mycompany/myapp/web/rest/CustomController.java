package com.mycompany.myapp.web.rest;


import com.mycompany.myapp.domain.Jugador;
import com.mycompany.myapp.domain.Partida;
import com.mycompany.myapp.service.PartidaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class CustomController {

    PartidaService partidaService;

    public CustomController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    @GetMapping("/listado-partidas-ganadas")
    public ResponseEntity<List<Partida>> findByGanador_Apodo(@RequestParam String apodo) {
        return ResponseEntity.ok(partidaService.findByGanador_Apodo(apodo));
    }

    @GetMapping("/total-partidas-ganadas")
    public ResponseEntity<Long> countByGanador(@RequestParam String apodo) {
        return ResponseEntity.ok(partidaService.countByGanador_Apodo(apodo));
    }
}
