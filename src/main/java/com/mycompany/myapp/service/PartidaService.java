package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Jugador;
import com.mycompany.myapp.domain.Partida;
import com.mycompany.myapp.repository.PartidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartidaService {

    @Autowired
    PartidaRepository partidaRepository;

    public List<Partida> findByGanador_Apodo(String apodo) {
        return partidaRepository.findByGanador_Apodo(apodo);
    }

    public long countByGanador_Apodo(String apodo) {
        return partidaRepository.countByGanador_Apodo(apodo);
    }
}
