import { IJugador } from 'app/entities/jugador/jugador.model';
import { IJuego } from 'app/entities/juego/juego.model';

export interface IPartida {
  id?: number;
  puntos?: number | null;
  ganador?: IJugador | null;
  perdedor?: IJugador | null;
  juego?: IJuego | null;
}

export class Partida implements IPartida {
  constructor(
    public id?: number,
    public puntos?: number | null,
    public ganador?: IJugador | null,
    public perdedor?: IJugador | null,
    public juego?: IJuego | null
  ) {}
}

export function getPartidaIdentifier(partida: IPartida): number | undefined {
  return partida.id;
}
