import React, { useContext, useEffect } from 'react';
import { createContext, useState } from 'react';
import { getCookie } from './RequireAuthentication';

type State = {
  username: string | undefined;
  setUsername: (username: string | undefined) => void;
};

const cookieName = 'login';

const AuthContext = createContext({ username: undefined, setUsername: _ => {} });

export function AuthProvider({ children }) {
  const [observedUsername, setUsername] = useState(undefined);

  useEffect(() => {
    const getUserName = getCookie(cookieName);
    if (getUserName) {
      setUsername(getUserName);
    }
  }, []);

  const value = {
    username: observedUsername,
    setUsername: setUsername,
  };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuthentication() {
  const state = useContext(AuthContext);

  return [
    state.username,
    username => {
      console.log(`setUsername: ${username}`);
      state.setUsername(username);
    },
  ];
}
