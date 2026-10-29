// Mirrors the backend DTOs in product/dto and category/dto.
// Keep these field names in sync with the Java records (Jackson uses them as JSON keys).

export interface Category {
  id: number;
  name: string;
  slug: string;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  imageUrl: string;
  category: Category;
}

// Mirrors ProductPageResponseDTO: one page of products plus paging info.
export interface ProductPage {
  content: Product[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
