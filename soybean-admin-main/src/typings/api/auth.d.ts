declare namespace Api {
  /**
   * namespace Auth
   *
   * backend api module: "auth"
   */
  namespace Auth {
    interface LoginToken {
      token: string;
      tokenType: string;
      refreshToken?: string;
      user: {
        id: number;
        username: string;
        role: number;
        displayName?: string | null;
        avatarPath?: string | null;
      };
    }

    interface UserInfo {
      userId: string;
      userName: string;
      roles: string[];
      buttons: string[];
      avatar?: string | null;
      role?: number;
    }
  }
}
