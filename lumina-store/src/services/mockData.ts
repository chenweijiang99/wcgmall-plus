import { Product, BlogPost } from '../types';

const BRANDS = ['Lumina', 'Sony', 'Apple', 'Bose', 'Samsung'];
const CATEGORIES = ['Electronics', 'Accessories', 'Wearables', 'Audio'];

const PRODUCTS: Product[] = Array.from({ length: 40 }).map((_, i) => ({
  id: `prod-${i}`,
  name: `Lumina ${['Pro', 'Air', 'Max', 'Studio', 'Pad'][i % 5]} ${i + 1}`,
  price: 199 + (i * 50) % 2000, // Varied prices
  category: CATEGORIES[i % 4],
  brand: BRANDS[i % 5],
  image: `https://picsum.photos/id/${(i * 3) + 10}/800/800`,
  images: [
    `https://picsum.photos/id/${(i * 3) + 10}/800/800`,
    `https://picsum.photos/id/${(i * 3) + 11}/800/800`,
    `https://picsum.photos/id/${(i * 3) + 12}/800/800`,
    `https://picsum.photos/id/${(i * 3) + 13}/800/800`,
  ],
  description: "Experience the pinnacle of innovation with our latest design engineering. Sleek, powerful, and intuitively designed for your lifestyle. This product features aerospace-grade aluminum and our proprietary neural engine.",
  specs: ["M3 Chip", "256GB Storage", "Retina Display", "All-day Battery"],
  comments: []
}));

const BLOGS: BlogPost[] = Array.from({ length: 15 }).map((_, i) => ({
  id: `blog-${i}`,
  title: `The Future of ${['Design', 'Tech', 'Living', 'Minimalism', 'Audio'][i % 5]}`,
  excerpt: "Exploring how modern technology intersects with minimalist aesthetics to create better user experiences.",
  content: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
  author: "Alex Designer",
  date: new Date(Date.now() - i * 86400000).toLocaleDateString(),
  image: `https://picsum.photos/id/${i + 50}/1200/600`,
  tags: ['Tech', 'Design'],
  comments: []
}));

export interface FilterOptions {
  category?: string;
  search?: string;
  brands?: string[];
  minPrice?: number;
  maxPrice?: number;
}

export const getProducts = async (page: number, filters: FilterOptions = {}): Promise<{ data: Product[]; hasMore: boolean }> => {
  await new Promise(resolve => setTimeout(resolve, 600)); // Simulate network
  
  let filtered = PRODUCTS;
  
  if (filters.category && filters.category !== 'All') {
    filtered = filtered.filter(p => p.category === filters.category);
  }
  if (filters.search) {
    const lower = filters.search.toLowerCase();
    filtered = filtered.filter(p => p.name.toLowerCase().includes(lower));
  }
  if (filters.brands && filters.brands.length > 0) {
    filtered = filtered.filter(p => filters.brands!.includes(p.brand));
  }
  if (filters.minPrice !== undefined) {
    filtered = filtered.filter(p => p.price >= filters.minPrice!);
  }
  if (filters.maxPrice !== undefined) {
    filtered = filtered.filter(p => p.price <= filters.maxPrice!);
  }

  const pageSize = 9;
  const start = (page - 1) * pageSize;
  const end = start + pageSize;
  return {
    data: filtered.slice(start, end),
    hasMore: end < filtered.length
  };
};

export const getProductById = async (id: string): Promise<Product | undefined> => {
  await new Promise(resolve => setTimeout(resolve, 300));
  return PRODUCTS.find(p => p.id === id);
};

export const getLatestProducts = async (): Promise<Product[]> => {
  await new Promise(resolve => setTimeout(resolve, 300));
  return PRODUCTS.slice(0, 4);
};

export const getBlogs = async (page: number, search?: string): Promise<{ data: BlogPost[]; hasMore: boolean }> => {
  await new Promise(resolve => setTimeout(resolve, 600));
  let filtered = BLOGS;
  if (search) {
    filtered = filtered.filter(b => b.title.toLowerCase().includes(search.toLowerCase()));
  }
  const pageSize = 5;
  const start = (page - 1) * pageSize;
  const end = start + pageSize;
  return {
    data: filtered.slice(start, end),
    hasMore: end < filtered.length
  };
};

export const getLatestBlogs = async (): Promise<BlogPost[]> => {
  await new Promise(resolve => setTimeout(resolve, 300));
  return BLOGS.slice(0, 3);
};

export const getBlogById = async (id: string): Promise<BlogPost | undefined> => {
  await new Promise(resolve => setTimeout(resolve, 300));
  return BLOGS.find(b => b.id === id);
};