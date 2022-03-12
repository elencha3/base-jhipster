package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Jugador;
import com.mycompany.myapp.domain.Partida;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data SQL repository for the Partida entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {

    //Métrica 1
    List<Partida> findByGanador_Apodo(String apodo);

    //Métrica 2

    long countByGanador_Apodo(String apodo);




}
