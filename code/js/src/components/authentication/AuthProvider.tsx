import React, { useContext } from 'react';
import { createContext, useState } from 'react';

type State = {
  username: string | undefined;
  setUsername: (username: string | undefined) => void;
};

const AuthContext = createContext({ username: undefined, setUsername: _ => {} });

export function AuthProvider({ children }) {
  const [observedUsername, setUsername] = useState(undefined);
  console.log(`provider ${observedUsername}`);

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
