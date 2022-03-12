package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Partida;
import com.mycompany.myapp.repository.PartidaRepository;
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
 * Integration tests for the {@link PartidaResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PartidaResourceIT {

    private static final Long DEFAULT_PUNTOS = 1L;
    private static final Long UPDATED_PUNTOS = 2L;

    private static final String ENTITY_API_URL = "/api/partidas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPartidaMockMvc;

    private Partida partida;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Partida createEntity(EntityManager em) {
        Partida partida = new Partida().puntos(DEFAULT_PUNTOS);
        return partida;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Partida createUpdatedEntity(EntityManager em) {
        Partida partida = new Partida().puntos(UPDATED_PUNTOS);
        return partida;
    }

    @BeforeEach
    public void initTest() {
        partida = createEntity(em);
    }

    @Test
    @Transactional
    void createPartida() throws Exception {
        int databaseSizeBeforeCreate = partidaRepository.findAll().size();
        // Create the Partida
        restPartidaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(partida)))
            .andExpect(status().isCreated());

        // Validate the Partida in the database
        List<Partida> partidaList = partidaRepository.findAll();
        assertThat(partidaList).hasSize(databaseSizeBeforeCreate + 1);
        Partida testPartida = partidaList.get(partidaList.size() - 1);
        assertThat(testPartida.getPuntos()).isEqualTo(DEFAULT_PUNTOS);
    }

    @Test
    @Transactional
    void createPartidaWithExistingId() throws Exception {
        // Create the Partida with an existing ID
        partida.setId(1L);

        int databaseSizeBeforeCreate = partidaRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPartidaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(partida)))
            .andExpect(status().isBadRequest());

        // Validate the Partida in the database
        List<Partida> partidaList = partidaRepository.findAll();
        assertThat(partidaList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllPartidas() throws Exception {
        // Initialize the database
        partidaRepository.saveAndFlush(partida);

        // Get all the partidaList
        restPartidaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(partida.getId().intValue())))
            .andExpect(jsonPath("$.[*].puntos").value(hasItem(DEFAULT_PUNTOS.intValue())));
    }

    @Test
    @Transactional
    void getPartida() throws Exception {
        // Initialize the database
        partidaRepository.saveAndFlush(partida);

        // Get the partida
        restPartidaMockMvc
            .perform(get(ENTITY_API_URL_ID, partida.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(partida.getId().intValue()))
            .andExpect(jsonPath("$.puntos").value(DEFAULT_PUNTOS.intValue()));
    }

    @Test
    @Transactional
    void getNonExistingPartida() throws Exception {
        // Get the partida
        restPartidaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewPartida() throws Exception {
        // Initialize the database
        partidaRepository.saveAndFlush(partida);

        int databaseSizeBeforeUpdate = partidaRepository.findAll().size();

        // Update the partida
        Partida updatedPartida = partidaRepository.findById(partida.getId()).get();
        // Disconnect from session so that the updates on updatedPartida are not directly saved in db
        em.detach(updatedPartida);
        updatedPartida.puntos(UPDATED_PUNTOS);

        restPartidaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPartida.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedPartida))
            )
            .andExpect(status().isOk());

        // Validate the Partida in the database
        List<Partida> partidaList = partidaRepository.findAll();
        assertThat(partidaList).hasSize(databaseSizeBeforeUpdate);
        Partida testPartida = partidaList.get(partidaList.size() - 1);
        assertThat(testPartida.getPuntos()).isEqualTo(UPDATED_PUNTOS);
    }

    @Test
    @Transactional
    void putNonExistingPartida() throws Exception {
        int databaseSizeBeforeUpdate = partidaRepository.findAll().size();
        partida.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPartidaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, partida.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(partida))
            )
            .andExpect(status().isBadRequest());

        // Validate the Partida in the database
        List<Partida> partidaList = partidaRepository.findAll();
        assertThat(partidaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPartida() throws Exception {
        int databaseSizeBeforeUpdate = partidaRepository.findAll().size();
        partida.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPartidaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(partida))
            )
            .andExpect(status().isBadRequest());

        // Validate the Partida in the database
        List<Partida> partidaList = partidaRepository.findAll();
        assertThat(partidaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPartida() throws Exception {
        int databaseSizeBeforeUpdate = partidaRepository.findAll().size();
        partida.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPartidaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(partida)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Partida in the database
        List<Partida> partidaList = partidaRepository.findAll();
        assertThat(partidaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePartidaWithPatch() throws Exception {
        // Initialize the database
        partidaRepository.saveAndFlush(partida);

        int databaseSizeBeforeUpdate = partidaRepository.findAll().size();

        // Update the partida using partial update
        Partida partialUpdatedPartida = new Partida();
        partialUpdatedPartida.setId(partida.getId());

        partialUpdatedPartida.puntos(UPDATED_PUNTOS);

        restPartidaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPartida.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPartida))
            )
            .andExpect(status().isOk());

        // Validate the Partida in the database
        List<Partida> partidaList = partidaRepository.findAll();
        assertThat(partidaList).hasSize(databaseSizeBeforeUpdate);
        Partida testPartida = partidaList.get(partidaList.size() - 1);
        assertThat(testPartida.getPuntos()).isEqualTo(UPDATED_PUNTOS);
    }

    @Test
    @Transactional
    void fullUpdatePartidaWithPatch() throws Exception {
        // Initialize the database
        partidaRepository.saveAndFlush(partida);

        int databaseSizeBeforeUpdate = partidaRepository.findAll().size();

        // Update the partida using partial update
        Partida partialUpdatedPartida = new Partida();
        partialUpdatedPartida.setId(partida.getId());

        partialUpdatedPartida.puntos(UPDATED_PUNTOS);

        restPartidaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPartida.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPartida))
            )
            .andExpect(status().isOk());

        // Validate the Partida in the database
        List<Partida> partidaList = partidaRepository.findAll();
        assertThat(partidaList).hasSize(databaseSizeBeforeUpdate);
        Partida testPartida = partidaList.get(partidaList.size() - 1);
        assertThat(testPartida.getPuntos()).isEqualTo(UPDATED_PUNTOS);
    }

    @Test
    @Transactional
    void patchNonExistingPartida() throws Exception {
        int databaseSizeBeforeUpdate = partidaRepository.findAll().size();
        partida.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPartidaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partida.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partida))
            )
            .andExpect(status().isBadRequest());

        // Validate the Partida in the database
        List<Partida> partidaList = partidaRepository.findAll();
        assertThat(partidaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPartida() throws Exception {
        int databaseSizeBeforeUpdate = partidaRepository.findAll().size();
        partida.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPartidaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partida))
            )
            .andExpect(status().isBadRequest());

        // Validate the Partida in the database
        List<Partida> partidaList = partidaRepository.findAll();
        assertThat(partidaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPartida() throws Exception {
        int databaseSizeBeforeUpdate = partidaRepository.findAll().size();
        partida.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPartidaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(partida)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Partida in the database
        List<Partida> partidaList = partidaRepository.findAll();
        assertThat(partidaList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePartida() throws Exception {
        // Initialize the database
        partidaRepository.saveAndFlush(partida);

        int databaseSizeBeforeDelete = partidaRepository.findAll().size();

        // Delete the partida
        restPartidaMockMvc
            .perform(delete(ENTITY_API_URL_ID, partida.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Partida> partidaList = partidaRepository.findAll();
        assertThat(partidaList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
