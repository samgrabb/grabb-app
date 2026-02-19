/**
 * grabb.ch App
 * Main application logic
 */

const App = {
    map: null,
    currentTab: 'news',
    posts: [],
    isLoading: false,

    /**
     * Initialize app
     */
    async init() {
        console.log('grabb.ch App starting...');
        
        // Setup tab navigation
        this.setupTabs();
        
        // Setup filter
        this.setupFilter();
        
        // Setup refresh button
        this.setupRefresh();
        
        // Load initial news
        await this.loadNews();
        
        console.log('App initialized');
    },

    /**
     * Setup tab navigation
     */
    setupTabs() {
        const tabs = document.querySelectorAll('.tab-btn');
        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const tabId = tab.dataset.tab;
                this.switchTab(tabId);
            });
        });
    },

    /**
     * Switch to a tab
     */
    switchTab(tabId) {
        // Update buttons
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.tab === tabId);
        });

        // Update content
        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.toggle('active', content.id === `${tabId}-tab`);
        });

        this.currentTab = tabId;

        // Initialize map on first view
        if (tabId === 'map' && !this.map) {
            this.initMap();
        }
    },

    /**
     * Setup gemeinde filter
     */
    setupFilter() {
        const filter = document.getElementById('gemeinde-filter');
        filter.addEventListener('change', () => {
            this.loadNews();
        });
    },

    /**
     * Setup refresh button
     */
    setupRefresh() {
        const btn = document.getElementById('refresh-btn');
        btn.addEventListener('click', () => {
            this.loadNews();
        });
    },

    /**
     * Load news from API
     */
    async loadNews() {
        if (this.isLoading) return;
        
        const list = document.getElementById('news-list');
        const filter = document.getElementById('gemeinde-filter');
        const category = filter.value || null;

        this.isLoading = true;
        list.innerHTML = '<div class="loading">Laden...</div>';

        try {
            this.posts = await GrabbAPI.getPosts({ 
                perPage: 30,
                category 
            });
            
            this.renderNews();
        } catch (error) {
            list.innerHTML = `
                <div class="empty-state">
                    <div class="emoji">😕</div>
                    <p>Konnte News nicht laden.</p>
                    <p><small>Bitte Internetverbindung prüfen.</small></p>
                </div>
            `;
        } finally {
            this.isLoading = false;
        }
    },

    /**
     * Render news list
     */
    renderNews() {
        const list = document.getElementById('news-list');
        
        if (this.posts.length === 0) {
            list.innerHTML = `
                <div class="empty-state">
                    <div class="emoji">📭</div>
                    <p>Keine News gefunden.</p>
                </div>
            `;
            return;
        }

        list.innerHTML = this.posts.map(post => `
            <article class="news-item">
                <a href="${post.url}" target="_blank">
                    <img src="${post.thumbnail}" alt="" class="news-thumb" 
                         onerror="this.src='img/placeholder.png'">
                    <div class="news-content">
                        <h3 class="news-title">${post.title}</h3>
                        <div class="news-meta">
                            ${post.primaryCategory ? 
                                `<span class="news-category">${post.primaryCategory.name}</span>` : ''}
                            <span class="news-date">${this.formatDate(post.date)}</span>
                        </div>
                    </div>
                </a>
            </article>
        `).join('');
    },

    /**
     * Format date for display
     */
    formatDate(date) {
        const now = new Date();
        const diff = now - date;
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const days = Math.floor(hours / 24);

        if (hours < 1) return 'Gerade eben';
        if (hours < 24) return `Vor ${hours}h`;
        if (days < 7) return `Vor ${days}d`;
        
        return date.toLocaleDateString('de-CH', { 
            day: 'numeric', 
            month: 'short' 
        });
    },

    /**
     * Initialize map
     */
    async initMap() {
        // Bülach center coordinates
        const center = [47.5173, 8.5410];
        
        this.map = L.map('map').setView(center, 13);

        // Add OpenStreetMap tiles
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap'
        }).addTo(this.map);

        // Load map points
        try {
            const points = await GrabbAPI.getMapPoints();
            this.addMapMarkers(points);
        } catch (error) {
            console.error('Could not load map points:', error);
        }

        // Add article markers
        try {
            const articles = await GrabbAPI.getArticlePoints(7);
            this.addArticleMarkers(articles);
        } catch (error) {
            console.error('Could not load article points:', error);
        }
    },

    /**
     * Add markers to map
     */
    addMapMarkers(points) {
        points.forEach(point => {
            if (point.lat && point.lon) {
                L.marker([point.lat, point.lon])
                    .addTo(this.map)
                    .bindPopup(`<b>${point.name}</b><br>${point.type || ''}`);
            }
        });
    },

    /**
     * Add article markers to map
     */
    addArticleMarkers(articles) {
        articles.forEach(article => {
            if (article.lat && article.lon) {
                const marker = L.circleMarker([article.lat, article.lon], {
                    radius: 8,
                    fillColor: '#10b981',
                    color: '#059669',
                    weight: 2,
                    opacity: 1,
                    fillOpacity: 0.8
                }).addTo(this.map);

                marker.bindPopup(`
                    <b>${article.title}</b><br>
                    <small>${this.formatDate(new Date(article.date))}</small><br>
                    <a href="${article.url}" target="_blank">Öffnen →</a>
                `);
            }
        });
    }
};

// Start app when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    App.init();
});

// Handle Capacitor back button (Android)
document.addEventListener('backbutton', () => {
    if (App.currentTab !== 'news') {
        App.switchTab('news');
    } else {
        // Exit app or go back in history
        if (window.Capacitor) {
            Capacitor.Plugins.App.exitApp();
        }
    }
});
