import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CheapsharkService {
  // URL base de la API para centralizar las peticiones
  private readonly BASE_URL = 'https://www.cheapshark.com/api/1.0';

  constructor(private http: HttpClient) { }

  /**
   * Endpoint 1: Obtener las mejores ofertas actuales.
   */
  getTopDeals(): Observable<any> {
    return this.http.get(`${this.BASE_URL}/deals?pageSize=5`);
  }

  /**
   * Endpoint 2: Buscar ofertas de juegos por nombre.
   */
  searchDeals(query: string): Observable<any> {
    return this.http.get(`${this.BASE_URL}/deals?title=${query}`);
  }

  /**
   * Endpoint 3: Obtener el catálogo de tiendas disponibles.
   */
  getStores(): Observable<any> {
    return this.http.get(`${this.BASE_URL}/stores`);
  }

  /**
   * Endpoint 4: Obtener detalles específicos de un juego (precios y tiendas).
   */
  getGameDetails(gameId: string): Observable<any> {
    return this.http.get(`${this.BASE_URL}/games?id=${gameId}`);
  }

  /**
   * Endpoint 5: Configurar una alerta de precio por correo electrónico.
   * @param email Correo donde se recibirá la notificación.
   * @param gameId ID único del juego (gameID).
   * @param targetPrice Precio objetivo para disparar la alerta.
   */
  setPriceAlert(email: string, gameId: string, targetPrice: number): Observable<any> {
    // Definimos la URL con los parámetros necesarios para la acción 'set'
    const url = `${this.BASE_URL}/alerts?action=set&email=${email}&gameID=${gameId}&price=${targetPrice}`;
    
    // Usamos responseType: 'text' porque la API responde con un string "true" o "false"
    return this.http.get(url, { responseType: 'text' });
  }
}