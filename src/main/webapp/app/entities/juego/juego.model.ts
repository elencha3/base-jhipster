export interface IJuego {
  id?: number;
  nombre?: string;
}

export class Juego implements IJuego {
  constructor(public id?: number, public nombre?: string) {}
}

export function getJuegoIdentifier(juego: IJuego): number | undefined {
  return juego.id;
}
