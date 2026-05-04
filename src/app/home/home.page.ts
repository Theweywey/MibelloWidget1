import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CheapsharkService } from '../services/cheapshark';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { Preferences } from '@capacitor/preferences';
import { Browser } from '@capacitor/browser';
import { App } from '@capacitor/app';
import { registerPlugin } from '@capacitor/core';

const WidgetUpdater = registerPlugin<any>('WidgetUpdater');

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
  standalone: false
})
export class HomePage implements OnInit, AfterViewInit {
  // --- Variables de Interfaz ---
  topDeals: any[] = [];
  searchResults: any[] = [];
  isSearching = false;
  isLoading = true;
  searchSubject = new Subject<string>();

  // --- Variables de Estado ---
  favoriteGameId: string | null = null;
  stores: any[] = [];
  selectedGameDetails: any = null;
  isModalOpen = false;

  // --- Variables de Alertas ---
  selectedGameIdForAlert: string = '';
  alertEmail: string = '';
  alertPrice: number | null = null;

  constructor(private api: CheapsharkService) {}

  async ngOnInit() {
    this.loadTopDeals();
    await this.loadFavorite();

    this.api.getStores().subscribe(res => {
      this.stores = res;
    });

    this.checkWidgetTrigger();

    App.addListener('appStateChange', ({ isActive }) => {
      if (isActive) this.checkWidgetTrigger();
    });

    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(query => {
      if (query.trim() !== '') {
        this.executeSearch(query);
      } else {
        this.searchResults = [];
        this.isSearching = false;
      }
    });
  }

  ngAfterViewInit() {
    this.spawnParticles();
    setInterval(() => this.spawnParticles(), 5000);
  }

  // --- Efectos Visuales ---
  spawnParticles() {
    const container = document.getElementById('particles');
    if (!container) return;
    for (let i = 0; i < 10; i++) {
      setTimeout(() => {
        const p = document.createElement('div');
        const isRed = Math.random() > 0.7;
        Object.assign(p.style, {
          position: 'absolute',
          width: '2px', height: '2px',
          borderRadius: '50%',
          background: isRed ? '#ff2a6d' : '#00ffc8',
          boxShadow: isRed ? '0 0 5px rgba(255,42,109,0.9)' : '0 0 5px rgba(0,255,200,0.9)',
          left: Math.random() * 100 + '%',
          bottom: '0',
          opacity: '0',
          animation: `particleFloat ${3 + Math.random() * 4}s linear ${Math.random() * 2}s forwards`,
        });
        container.appendChild(p);
        setTimeout(() => p.remove(), 8000);
      }, i * 200);
    }
  }

  // --- Lógica de Datos ---
  loadTopDeals() {
    this.isLoading = true;
    this.api.getTopDeals().subscribe({
      next: (res) => {
        this.topDeals = res.slice(0, 5);
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  executeSearch(query: string) {
    this.api.searchDeals(query).subscribe(res => {
      this.searchResults = res;
    });
  }

  onSearchChange(event: any) {
    const query = event.detail.value;
    this.isSearching = query.trim() !== '';
    this.searchSubject.next(query);
  }

  // --- MODIFICACIÓN DE FAVORITOS Y WIDGET ---
  async setFavorite(game: any) {
    const id = game.gameID || game.gameId;

    if (id) {
      console.log("Guardando favorito y notificando al widget:", id);
      await Preferences.set({ key: 'favoriteGame', value: id.toString() });
      this.favoriteGameId = id.toString(); 

      // CAMBIADO: Enviamos el gameId al plugin
      try {
        await WidgetUpdater.update({ gameId: id.toString() }); 
      } catch (error) {
        console.warn("El plugin nativo solo funciona en Android", error);
      }
    }
  }

  async loadFavorite() {
    const { value } = await Preferences.get({ key: 'favoriteGame' });
    if (value) this.favoriteGameId = value;
  }

  async checkWidgetTrigger() {
    const { value } = await Preferences.get({ key: 'trigger_modal_id' });
    if (value) {
      this.openGameDetails(value);
      await Preferences.remove({ key: 'trigger_modal_id' });
    }
  }

  // --- Modal y Alertas ---
  openGameDetails(gameID: string) {
    if (!gameID) return;
    this.selectedGameIdForAlert = gameID;
    this.api.getGameDetails(gameID).subscribe(res => {
      this.selectedGameDetails = res;
      this.isModalOpen = true;
    });
  }

  closeModal() {
    this.isModalOpen = false;
    this.selectedGameDetails = null;
  }

  createAlert() {
    if (!this.alertEmail || !this.alertPrice || !this.selectedGameIdForAlert) return;
    this.api.setPriceAlert(this.alertEmail, this.selectedGameIdForAlert, this.alertPrice)
      .subscribe({
        next: () => {
          alert('Alerta creada con éxito');
          this.alertEmail = '';
          this.alertPrice = null;
        }
      });
  }

  async openDealUrl(dealID: string) {
    await Browser.open({ url: `https://www.cheapshark.com/redirect?dealID=${dealID}` });
  }

  getStoreName(storeID: string): string {
    const store = this.stores.find(s => s.storeID === storeID);
    return store ? store.storeName : 'Tienda desconocida';
  }
}