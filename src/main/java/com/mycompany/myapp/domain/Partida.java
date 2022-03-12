package com.mycompany.myapp.domain;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Partida.
 */
@Entity
@Table(name = "partida")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Partida implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "puntos")
    private Long puntos;

    @ManyToOne
    private Jugador ganador;

    @ManyToOne
    private Jugador perdedor;

    @ManyToOne
    private Juego juego;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Partida id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPuntos() {
        return this.puntos;
    }

    public Partida puntos(Long puntos) {
        this.setPuntos(puntos);
        return this;
    }

    public void setPuntos(Long puntos) {
        this.puntos = puntos;
    }

    public Jugador getGanador() {
        return this.ganador;
    }

    public void setGanador(Jugador jugador) {
        this.ganador = jugador;
    }

    public Partida ganador(Jugador jugador) {
        this.setGanador(jugador);
        return this;
    }

    public Jugador getPerdedor() {
        return this.perdedor;
    }

    public void setPerdedor(Jugador jugador) {
        this.perdedor = jugador;
    }

    public Partida perdedor(Jugador jugador) {
        this.setPerdedor(jugador);
        return this;
    }

    public Juego getJuego() {
        return this.juego;
    }

    public void setJuego(Juego juego) {
        this.juego = juego;
    }

    public Partida juego(Juego juego) {
        this.setJuego(juego);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Partida)) {
            return false;
        }
        return id != null && id.equals(((Partida) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Partida{" +
            "id=" + getId() +
            ", puntos=" + getPuntos() +
            "}";
    }
}
