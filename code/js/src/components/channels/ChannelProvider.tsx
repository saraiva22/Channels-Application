import React, { ReactNode, useContext, useState } from 'react';
import { createContext } from 'react';
import { ChannelOutputModel } from '../../services/channels/models/ChannelOutputModel';

type State = {
  selectedChannel: ChannelOutputModel | undefined;
  setChannel: (channel: ChannelOutputModel | undefined) => void;
};

const ChannelContext = createContext<State | undefined>(undefined);

export function ChannelProvider({ children }: { children: ReactNode }) {
  const [selectedChannel, setChannel] = useState<ChannelOutputModel | null>(null);

  const value = {
    selectedChannel: selectedChannel,
    setChannel: setChannel,
  };

  return <ChannelContext.Provider value={value}>{children}</ChannelContext.Provider>;
}

export function useChannel() {
  const state = useContext(ChannelContext);

  return state;
}
