import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { IJugador, Jugador } from '../jugador.model';
import { JugadorService } from '../service/jugador.service';

@Component({
  selector: 'jhi-jugador-update',
  templateUrl: './jugador-update.component.html',
})
export class JugadorUpdateComponent implements OnInit {
  isSaving = false;

  editForm = this.fb.group({
    id: [],
    apodo: [null, [Validators.required, Validators.pattern('^[A-Za-z0-9_]*$')]],
    nombre: [null, [Validators.required]],
    apellido: [null, [Validators.required]],
    fechaNacimiento: [null, [Validators.required]],
  });

  constructor(protected jugadorService: JugadorService, protected activatedRoute: ActivatedRoute, protected fb: FormBuilder) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ jugador }) => {
      this.updateForm(jugador);
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const jugador = this.createFromForm();
    if (jugador.id !== undefined) {
      this.subscribeToSaveResponse(this.jugadorService.update(jugador));
    } else {
      this.subscribeToSaveResponse(this.jugadorService.create(jugador));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IJugador>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(jugador: IJugador): void {
    this.editForm.patchValue({
      id: jugador.id,
      apodo: jugador.apodo,
      nombre: jugador.nombre,
      apellido: jugador.apellido,
      fechaNacimiento: jugador.fechaNacimiento,
    });
  }

  protected createFromForm(): IJugador {
    return {
      ...new Jugador(),
      id: this.editForm.get(['id'])!.value,
      apodo: this.editForm.get(['apodo'])!.value,
      nombre: this.editForm.get(['nombre'])!.value,
      apellido: this.editForm.get(['apellido'])!.value,
      fechaNacimiento: this.editForm.get(['fechaNacimiento'])!.value,
    };
  }
}
