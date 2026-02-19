/**
 * grabb.ch API Client
 * Connects to WordPress REST API
 */

const API_BASE = 'https://grabb.ch/wp-json';

const GrabbAPI = {
    /**
     * Fetch news posts
     * @param {Object} options - Query options
     * @param {number} options.page - Page number (default: 1)
     * @param {number} options.perPage - Posts per page (default: 20)
     * @param {number} options.category - Category ID filter
     * @returns {Promise<Array>} Posts array
     */
    async getPosts(options = {}) {
        const { page = 1, perPage = 20, category = null } = options;
        
        let url = `${API_BASE}/wp/v2/posts?_embed&page=${page}&per_page=${perPage}`;
        
        if (category) {
            url += `&categories=${category}`;
        }

        try {
            const response = await fetch(url);
            if (!response.ok) throw new Error('API Error');
            
            const posts = await response.json();
            return posts.map(this.transformPost);
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },

    /**
     * Transform WP post to app format
     */
    transformPost(post) {
        // Get featured image
        let thumbnail = 'img/placeholder.png';
        if (post._embedded && post._embedded['wp:featuredmedia']) {
            const media = post._embedded['wp:featuredmedia'][0];
            if (media && media.media_details && media.media_details.sizes) {
                // Prefer thumbnail or medium size
                const sizes = media.media_details.sizes;
                thumbnail = sizes.thumbnail?.source_url || 
                           sizes.medium?.source_url || 
                           media.source_url;
            }
        }

        // Get categories
        let categories = [];
        if (post._embedded && post._embedded['wp:term']) {
            const terms = post._embedded['wp:term'][0] || [];
            categories = terms.map(t => ({ id: t.id, name: t.name }));
        }

        // Get primary category (first non-"Top" category)
        const primaryCategory = categories.find(c => c.name !== 'Top') || categories[0];

        return {
            id: post.id,
            title: this.decodeHtml(post.title.rendered),
            excerpt: this.decodeHtml(post.excerpt.rendered).replace(/<[^>]*>/g, '').substring(0, 150),
            url: post.link,
            date: new Date(post.date),
            thumbnail,
            categories,
            primaryCategory
        };
    },

    /**
     * Decode HTML entities
     */
    decodeHtml(html) {
        const txt = document.createElement('textarea');
        txt.innerHTML = html;
        return txt.value;
    },

    /**
     * Get regions/gemeinden
     */
    async getRegions() {
        try {
            const response = await fetch(`${API_BASE}/grabb/v1/regionen`);
            if (!response.ok) throw new Error('API Error');
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            // Return hardcoded fallback
            return {
                buelach: {
                    name: 'Bülach',
                    gemeinden: ['Bülach', 'Höri', 'Hochfelden', 'Winkel', 'Bachenbülach']
                }
            };
        }
    },

    /**
     * Get map points
     */
    async getMapPoints() {
        try {
            const response = await fetch(`${API_BASE}/grabb/v1/map-points`);
            if (!response.ok) throw new Error('API Error');
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            return [];
        }
    },

    /**
     * Get article points for map
     */
    async getArticlePoints(days = 7) {
        try {
            const response = await fetch(`${API_BASE}/grabb/v1/article-points?days=${days}`);
            if (!response.ok) throw new Error('API Error');
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            return [];
        }
    }
};

// Bind transformPost context
GrabbAPI.transformPost = GrabbAPI.transformPost.bind(GrabbAPI);
GrabbAPI.decodeHtml = GrabbAPI.decodeHtml.bind(GrabbAPI);
