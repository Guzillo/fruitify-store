// Mirrors auth/dto/AuthenticationResponse, SignupUserDto and users/dto/UserResponseDTO.

export interface AuthenticationResponse {
  accessToken: string;
  refreshToken: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  firstName: string | null;
  lastName: string | null;
}

export interface UserProfile {
  email: string;
  firstName: string | null;
  lastName: string | null;
}
