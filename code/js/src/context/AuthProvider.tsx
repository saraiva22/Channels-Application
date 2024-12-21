import React, { useContext, useEffect } from 'react';
import { createContext, useState } from 'react';
import { getCookie } from '../components/authentication/RequireAuthentication';
import { closeSSE, getSSE, initializeSSE, setSSE } from '../components/notifications/SSEManager';
import { apiRoutes, PREFIX_API } from '../services/utils/HttpService';

type State = {
  username: string | undefined;
  setUsername: (username: string | undefined) => void;
};

const cookieName = 'login';

const AuthContext = createContext({ username: undefined, setUsername: _ => {} });

export function AuthProvider({ children }) {
  const [observedUsername, setUsername] = useState(undefined);
  const eventSource = getSSE();

  useEffect(() => {
    const getUserName = getCookie(cookieName);
    if (getUserName) {
      setUsername(getUserName);
      if (!eventSource) {
        const newEventSource = initializeSSE(PREFIX_API + apiRoutes.LISTEN_CHAT);
        setSSE(newEventSource);
      }
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
