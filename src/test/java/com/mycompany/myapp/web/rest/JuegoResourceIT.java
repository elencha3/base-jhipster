package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Juego;
import com.mycompany.myapp.repository.JuegoRepository;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link JuegoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class JuegoResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/juegos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private JuegoRepository juegoRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restJuegoMockMvc;

    private Juego juego;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Juego createEntity(EntityManager em) {
        Juego juego = new Juego().nombre(DEFAULT_NOMBRE);
        return juego;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Juego createUpdatedEntity(EntityManager em) {
        Juego juego = new Juego().nombre(UPDATED_NOMBRE);
        return juego;
    }

    @BeforeEach
    public void initTest() {
        juego = createEntity(em);
    }

    @Test
    @Transactional
    void createJuego() throws Exception {
        int databaseSizeBeforeCreate = juegoRepository.findAll().size();
        // Create the Juego
        restJuegoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(juego)))
            .andExpect(status().isCreated());

        // Validate the Juego in the database
        List<Juego> juegoList = juegoRepository.findAll();
        assertThat(juegoList).hasSize(databaseSizeBeforeCreate + 1);
        Juego testJuego = juegoList.get(juegoList.size() - 1);
        assertThat(testJuego.getNombre()).isEqualTo(DEFAULT_NOMBRE);
    }

    @Test
    @Transactional
    void createJuegoWithExistingId() throws Exception {
        // Create the Juego with an existing ID
        juego.setId(1L);

        int databaseSizeBeforeCreate = juegoRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restJuegoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(juego)))
            .andExpect(status().isBadRequest());

        // Validate the Juego in the database
        List<Juego> juegoList = juegoRepository.findAll();
        assertThat(juegoList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        int databaseSizeBeforeTest = juegoRepository.findAll().size();
        // set the field null
        juego.setNombre(null);

        // Create the Juego, which fails.

        restJuegoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(juego)))
            .andExpect(status().isBadRequest());

        List<Juego> juegoList = juegoRepository.findAll();
        assertThat(juegoList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllJuegos() throws Exception {
        // Initialize the database
        juegoRepository.saveAndFlush(juego);

        // Get all the juegoList
        restJuegoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(juego.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)));
    }

    @Test
    @Transactional
    void getJuego() throws Exception {
        // Initialize the database
        juegoRepository.saveAndFlush(juego);

        // Get the juego
        restJuegoMockMvc
            .perform(get(ENTITY_API_URL_ID, juego.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(juego.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE));
    }

    @Test
    @Transactional
    void getNonExistingJuego() throws Exception {
        // Get the juego
        restJuegoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewJuego() throws Exception {
        // Initialize the database
        juegoRepository.saveAndFlush(juego);

        int databaseSizeBeforeUpdate = juegoRepository.findAll().size();

        // Update the juego
        Juego updatedJuego = juegoRepository.findById(juego.getId()).get();
        // Disconnect from session so that the updates on updatedJuego are not directly saved in db
        em.detach(updatedJuego);
        updatedJuego.nombre(UPDATED_NOMBRE);

        restJuegoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedJuego.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedJuego))
            )
            .andExpect(status().isOk());

        // Validate the Juego in the database
        List<Juego> juegoList = juegoRepository.findAll();
        assertThat(juegoList).hasSize(databaseSizeBeforeUpdate);
        Juego testJuego = juegoList.get(juegoList.size() - 1);
        assertThat(testJuego.getNombre()).isEqualTo(UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void putNonExistingJuego() throws Exception {
        int databaseSizeBeforeUpdate = juegoRepository.findAll().size();
        juego.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJuegoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, juego.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(juego))
            )
            .andExpect(status().isBadRequest());

        // Validate the Juego in the database
        List<Juego> juegoList = juegoRepository.findAll();
        assertThat(juegoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchJuego() throws Exception {
        int databaseSizeBeforeUpdate = juegoRepository.findAll().size();
        juego.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJuegoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(juego))
            )
            .andExpect(status().isBadRequest());

        // Validate the Juego in the database
        List<Juego> juegoList = juegoRepository.findAll();
        assertThat(juegoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamJuego() throws Exception {
        int databaseSizeBeforeUpdate = juegoRepository.findAll().size();
        juego.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJuegoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(juego)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Juego in the database
        List<Juego> juegoList = juegoRepository.findAll();
        assertThat(juegoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateJuegoWithPatch() throws Exception {
        // Initialize the database
        juegoRepository.saveAndFlush(juego);

        int databaseSizeBeforeUpdate = juegoRepository.findAll().size();

        // Update the juego using partial update
        Juego partialUpdatedJuego = new Juego();
        partialUpdatedJuego.setId(juego.getId());

        partialUpdatedJuego.nombre(UPDATED_NOMBRE);

        restJuegoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedJuego.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedJuego))
            )
            .andExpect(status().isOk());

        // Validate the Juego in the database
        List<Juego> juegoList = juegoRepository.findAll();
        assertThat(juegoList).hasSize(databaseSizeBeforeUpdate);
        Juego testJuego = juegoList.get(juegoList.size() - 1);
        assertThat(testJuego.getNombre()).isEqualTo(UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void fullUpdateJuegoWithPatch() throws Exception {
        // Initialize the database
        juegoRepository.saveAndFlush(juego);

        int databaseSizeBeforeUpdate = juegoRepository.findAll().size();

        // Update the juego using partial update
        Juego partialUpdatedJuego = new Juego();
        partialUpdatedJuego.setId(juego.getId());

        partialUpdatedJuego.nombre(UPDATED_NOMBRE);

        restJuegoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedJuego.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedJuego))
            )
            .andExpect(status().isOk());

        // Validate the Juego in the database
        List<Juego> juegoList = juegoRepository.findAll();
        assertThat(juegoList).hasSize(databaseSizeBeforeUpdate);
        Juego testJuego = juegoList.get(juegoList.size() - 1);
        assertThat(testJuego.getNombre()).isEqualTo(UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void patchNonExistingJuego() throws Exception {
        int databaseSizeBeforeUpdate = juegoRepository.findAll().size();
        juego.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restJuegoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, juego.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(juego))
            )
            .andExpect(status().isBadRequest());

        // Validate the Juego in the database
        List<Juego> juegoList = juegoRepository.findAll();
        assertThat(juegoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchJuego() throws Exception {
        int databaseSizeBeforeUpdate = juegoRepository.findAll().size();
        juego.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJuegoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(juego))
            )
            .andExpect(status().isBadRequest());

        // Validate the Juego in the database
        List<Juego> juegoList = juegoRepository.findAll();
        assertThat(juegoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamJuego() throws Exception {
        int databaseSizeBeforeUpdate = juegoRepository.findAll().size();
        juego.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restJuegoMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(juego)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Juego in the database
        List<Juego> juegoList = juegoRepository.findAll();
        assertThat(juegoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteJuego() throws Exception {
        // Initialize the database
        juegoRepository.saveAndFlush(juego);

        int databaseSizeBeforeDelete = juegoRepository.findAll().size();

        // Delete the juego
        restJuegoMockMvc
            .perform(delete(ENTITY_API_URL_ID, juego.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Juego> juegoList = juegoRepository.findAll();
        assertThat(juegoList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
