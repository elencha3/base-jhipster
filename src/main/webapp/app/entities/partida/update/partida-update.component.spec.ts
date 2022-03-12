import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { PartidaService } from '../service/partida.service';
import { IPartida, Partida } from '../partida.model';
import { IJugador } from 'app/entities/jugador/jugador.model';
import { JugadorService } from 'app/entities/jugador/service/jugador.service';
import { IJuego } from 'app/entities/juego/juego.model';
import { JuegoService } from 'app/entities/juego/service/juego.service';

import { PartidaUpdateComponent } from './partida-update.component';

describe('Partida Management Update Component', () => {
  let comp: PartidaUpdateComponent;
  let fixture: ComponentFixture<PartidaUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let partidaService: PartidaService;
  let jugadorService: JugadorService;
  let juegoService: JuegoService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [PartidaUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(PartidaUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(PartidaUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    partidaService = TestBed.inject(PartidaService);
    jugadorService = TestBed.inject(JugadorService);
    juegoService = TestBed.inject(JuegoService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Jugador query and add missing value', () => {
      const partida: IPartida = { id: 456 };
      const ganador: IJugador = { id: 4544 };
      partida.ganador = ganador;
      const perdedor: IJugador = { id: 46829 };
      partida.perdedor = perdedor;

      const jugadorCollection: IJugador[] = [{ id: 65195 }];
      jest.spyOn(jugadorService, 'query').mockReturnValue(of(new HttpResponse({ body: jugadorCollection })));
      const additionalJugadors = [ganador, perdedor];
      const expectedCollection: IJugador[] = [...additionalJugadors, ...jugadorCollection];
      jest.spyOn(jugadorService, 'addJugadorToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ partida });
      comp.ngOnInit();

      expect(jugadorService.query).toHaveBeenCalled();
      expect(jugadorService.addJugadorToCollectionIfMissing).toHaveBeenCalledWith(jugadorCollection, ...additionalJugadors);
      expect(comp.jugadorsSharedCollection).toEqual(expectedCollection);
    });

    it('Should call Juego query and add missing value', () => {
      const partida: IPartida = { id: 456 };
      const juego: IJuego = { id: 28385 };
      partida.juego = juego;

      const juegoCollection: IJuego[] = [{ id: 18654 }];
      jest.spyOn(juegoService, 'query').mockReturnValue(of(new HttpResponse({ body: juegoCollection })));
      const additionalJuegos = [juego];
      const expectedCollection: IJuego[] = [...additionalJuegos, ...juegoCollection];
      jest.spyOn(juegoService, 'addJuegoToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ partida });
      comp.ngOnInit();

      expect(juegoService.query).toHaveBeenCalled();
      expect(juegoService.addJuegoToCollectionIfMissing).toHaveBeenCalledWith(juegoCollection, ...additionalJuegos);
      expect(comp.juegosSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const partida: IPartida = { id: 456 };
      const ganador: IJugador = { id: 97523 };
      partida.ganador = ganador;
      const perdedor: IJugador = { id: 79656 };
      partida.perdedor = perdedor;
      const juego: IJuego = { id: 68512 };
      partida.juego = juego;

      activatedRoute.data = of({ partida });
      comp.ngOnInit();

      expect(comp.editForm.value).toEqual(expect.objectContaining(partida));
      expect(comp.jugadorsSharedCollection).toContain(ganador);
      expect(comp.jugadorsSharedCollection).toContain(perdedor);
      expect(comp.juegosSharedCollection).toContain(juego);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<Partida>>();
      const partida = { id: 123 };
      jest.spyOn(partidaService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ partida });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: partida }));
      saveSubject.complete();

      // THEN
      expect(comp.previousState).toHaveBeenCalled();
      expect(partidaService.update).toHaveBeenCalledWith(partida);
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<Partida>>();
      const partida = new Partida();
      jest.spyOn(partidaService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ partida });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: partida }));
      saveSubject.complete();

      // THEN
      expect(partidaService.create).toHaveBeenCalledWith(partida);
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<Partida>>();
      const partida = { id: 123 };
      jest.spyOn(partidaService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ partida });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(partidaService.update).toHaveBeenCalledWith(partida);
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Tracking relationships identifiers', () => {
    describe('trackJugadorById', () => {
      it('Should return tracked Jugador primary key', () => {
        const entity = { id: 123 };
        const trackResult = comp.trackJugadorById(0, entity);
        expect(trackResult).toEqual(entity.id);
      });
    });

    describe('trackJuegoById', () => {
      it('Should return tracked Juego primary key', () => {
        const entity = { id: 123 };
        const trackResult = comp.trackJuegoById(0, entity);
        expect(trackResult).toEqual(entity.id);
      });
    });
  });
});
