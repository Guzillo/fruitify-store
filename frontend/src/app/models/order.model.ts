// Mirrors order/dto: OrderCreateRequestDTO, ShippingAddressDTO, OrderSummaryDTO,
// OrderResponseDTO, OrderItemResponseDTO, OrderPageResponseDTO.

export type OrderStatus = 'PENDING' | 'PAID' | 'CANCELLED';

export interface ShippingAddress {
  recipientName: string;
  street: string;
  city: string;
  postalCode: string;
  country: string;
}

export interface OrderItem {
  productId: number;
  productName: string;
  priceAtPurchase: number;
  quantity: number;
}

export interface OrderSummary {
  id: number;
  orderNumber: string;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
}

export interface Order extends OrderSummary {
  shippingAddress: ShippingAddress;
  items: OrderItem[];
}

export interface OrderPage {
  content: OrderSummary[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// Mirrors payment/dto/CheckoutSessionResponseDTO.
export interface CheckoutSession {
  url: string;
  sessionId: string;
}
