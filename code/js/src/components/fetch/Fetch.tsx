import * as React from 'react';
import { useEffect, useReducer } from 'react';
import { useLocation } from 'react-router-dom';
import { ChannelsListOutputModel } from '../../services/channels/models/ChannelsListOutputModel';
import { ChannelList } from '../channels/ChannelList';
import { InputType } from './InputType';
import { ReturnType } from './ReturnType';

type FetchProps = {
  fetchFunction: (...args: Array<InputType>) => Promise<ReturnType>;
  fetchArgs: Array<InputType>;
};

export type State =
  | { type: 'start' }
  | { type: 'loading' }
  | { type: 'success'; rsp: ReturnType }
  | { type: 'error'; error: Error };

type Action =
  | { type: 'started-loading' }
  | { type: 'success'; rsp: ReturnType }
  | { type: 'error'; error: Error }
  | { type: 'cancel' };

function unexpectedAction(action: Action, state: State): State {
  console.log(`unexpected action ${action.type} on state ${state.type}`);
  return state;
}

function reducer(state: State, action: Action): State {
  console.log(`handling ${action.type} on ${state.type}`);
  switch (action.type) {
    case 'started-loading':
      return { type: 'loading' };
    case 'success':
      switch (state.type) {
        case 'loading':
          return { ...state, type: 'success', rsp: action.rsp };
        default:
          return unexpectedAction(action, state);
      }
    case 'error':
      switch (state.type) {
        case 'loading':
          return { ...state, type: 'error', error: action.error };
        default:
          return unexpectedAction(action, state);
      }
    case 'cancel':
      return { type: 'start' };
  }
}

const firstState: State = { type: 'start' };

export function Fetch(props: FetchProps) {
  const [state, dispatch] = useReducer(reducer, firstState);
  const location = useLocation();

  useEffect(() => {
    const abort = new AbortController();
    let cancelled = false;
    async function doFetch() {
      dispatch({ type: 'started-loading' });
      try {
        const resp = await props.fetchFunction(...props.fetchArgs);
        console.log(cancelled);
        if (!cancelled) {
          dispatch({ type: 'success', rsp: resp });
        }
      } catch (error) {
        if (!cancelled) {
          dispatch({ type: 'error', error: error });
        }
      }
    }
    doFetch();
    return () => {
      console.log('cleanup');
      abort.abort();
      cancelled = true;
    };
  }, [dispatch, location]);

  switch (state.type) {
    case 'start':
      return <p>Idle</p>;
    case 'loading':
      return <p>loading...</p>;
    case 'error':
      return <p>Error: {state.error.message}</p>;
    case 'success': {
      switch (state.rsp) {
        case state.rsp as ChannelsListOutputModel:
          return ChannelList(state.rsp);
      }
    }
  }
}
