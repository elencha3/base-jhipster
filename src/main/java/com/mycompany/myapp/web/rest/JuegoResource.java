package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.Juego;
import com.mycompany.myapp.repository.JuegoRepository;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Juego}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class JuegoResource {

    private final Logger log = LoggerFactory.getLogger(JuegoResource.class);

    private static final String ENTITY_NAME = "juego";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final JuegoRepository juegoRepository;

    public JuegoResource(JuegoRepository juegoRepository) {
        this.juegoRepository = juegoRepository;
    }

    /**
     * {@code POST  /juegos} : Create a new juego.
     *
     * @param juego the juego to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new juego, or with status {@code 400 (Bad Request)} if the juego has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/juegos")
    public ResponseEntity<Juego> createJuego(@Valid @RequestBody Juego juego) throws URISyntaxException {
        log.debug("REST request to save Juego : {}", juego);
        if (juego.getId() != null) {
            throw new BadRequestAlertException("A new juego cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Juego result = juegoRepository.save(juego);
        return ResponseEntity
            .created(new URI("/api/juegos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /juegos/:id} : Updates an existing juego.
     *
     * @param id the id of the juego to save.
     * @param juego the juego to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated juego,
     * or with status {@code 400 (Bad Request)} if the juego is not valid,
     * or with status {@code 500 (Internal Server Error)} if the juego couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/juegos/{id}")
    public ResponseEntity<Juego> updateJuego(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody Juego juego)
        throws URISyntaxException {
        log.debug("REST request to update Juego : {}, {}", id, juego);
        if (juego.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, juego.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!juegoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Juego result = juegoRepository.save(juego);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, juego.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /juegos/:id} : Partial updates given fields of an existing juego, field will ignore if it is null
     *
     * @param id the id of the juego to save.
     * @param juego the juego to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated juego,
     * or with status {@code 400 (Bad Request)} if the juego is not valid,
     * or with status {@code 404 (Not Found)} if the juego is not found,
     * or with status {@code 500 (Internal Server Error)} if the juego couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/juegos/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Juego> partialUpdateJuego(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Juego juego
    ) throws URISyntaxException {
        log.debug("REST request to partial update Juego partially : {}, {}", id, juego);
        if (juego.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, juego.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!juegoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Juego> result = juegoRepository
            .findById(juego.getId())
            .map(existingJuego -> {
                if (juego.getNombre() != null) {
                    existingJuego.setNombre(juego.getNombre());
                }

                return existingJuego;
            })
            .map(juegoRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, juego.getId().toString())
        );
    }

    /**
     * {@code GET  /juegos} : get all the juegos.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of juegos in body.
     */
    @GetMapping("/juegos")
    public ResponseEntity<List<Juego>> getAllJuegos(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Juegos");
        Page<Juego> page = juegoRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /juegos/:id} : get the "id" juego.
     *
     * @param id the id of the juego to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the juego, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/juegos/{id}")
    public ResponseEntity<Juego> getJuego(@PathVariable Long id) {
        log.debug("REST request to get Juego : {}", id);
        Optional<Juego> juego = juegoRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(juego);
    }

    /**
     * {@code DELETE  /juegos/:id} : delete the "id" juego.
     *
     * @param id the id of the juego to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/juegos/{id}")
    public ResponseEntity<Void> deleteJuego(@PathVariable Long id) {
        log.debug("REST request to delete Juego : {}", id);
        juegoRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
