import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { JugadorService } from '../service/jugador.service';
import { IJugador, Jugador } from '../jugador.model';

import { JugadorUpdateComponent } from './jugador-update.component';

describe('Jugador Management Update Component', () => {
  let comp: JugadorUpdateComponent;
  let fixture: ComponentFixture<JugadorUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let jugadorService: JugadorService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [JugadorUpdateComponent],
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
      .overrideTemplate(JugadorUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(JugadorUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    jugadorService = TestBed.inject(JugadorService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const jugador: IJugador = { id: 456 };

      activatedRoute.data = of({ jugador });
      comp.ngOnInit();

      expect(comp.editForm.value).toEqual(expect.objectContaining(jugador));
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<Jugador>>();
      const jugador = { id: 123 };
      jest.spyOn(jugadorService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ jugador });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: jugador }));
      saveSubject.complete();

      // THEN
      expect(comp.previousState).toHaveBeenCalled();
      expect(jugadorService.update).toHaveBeenCalledWith(jugador);
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<Jugador>>();
      const jugador = new Jugador();
      jest.spyOn(jugadorService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ jugador });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: jugador }));
      saveSubject.complete();

      // THEN
      expect(jugadorService.create).toHaveBeenCalledWith(jugador);
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<Jugador>>();
      const jugador = { id: 123 };
      jest.spyOn(jugadorService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ jugador });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(jugadorService.update).toHaveBeenCalledWith(jugador);
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
