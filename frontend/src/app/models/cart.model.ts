// Mirrors cart/dto/CartResponseDTO and CartItemResponseDTO.

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  price: number;
  quantity: number;
}

export interface Cart {
  id: number;
  items: CartItem[];
}
