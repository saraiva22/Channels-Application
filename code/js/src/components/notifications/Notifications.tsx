import React, { useEffect, useReducer, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { Problem, ProblemComponent } from '../../services/media/Problem';
import { Event_Message } from '../../services/chat/ChatService';
import { apiRoutes, PREFIX_API } from '../../services/utils/HttpService';
import { formatDate } from '../messages/Messages';

type State =
  | { type: 'begin' }
  | { type: 'loading' }
  | { type: 'loaded'; data: Event_Message }
  | { type: 'error'; error: Problem };

type Action =
  | { type: 'start-loading' }
  | { type: 'loading-success'; data: Event_Message }
  | { type: 'loading-error'; error: Problem }
  | { type: 'cancel' };

function logUnexpectedAction(state: State, action: Action): State {
  console.log(`unexpected action ${action.type} on state ${state.type}`);
  return state;
}

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case 'start-loading':
      return { type: 'loading' };
    case 'loading-success':
      if (state.type !== 'loading') {
        return logUnexpectedAction(state, action);
      }
      return { ...state, type: 'loaded', data: action.data };
    case 'loading-error':
      if (state.type !== 'loading') {
        return logUnexpectedAction(state, action);
      }
      return { ...state, type: 'error', error: action.error };
    case 'cancel':
      return { type: 'begin' };
  }
}

const firstState: State = { type: 'begin' };
export function Notifications() {
  const [state, dispatch] = useReducer(reducer, firstState);
  const [data, setData] = useState<any>(null);
  const location = useLocation();

  useEffect(() => {
    const abort = new AbortController();
    let cancelled = false;

    async function doFetch() {
      dispatch({ type: 'start-loading' });

      try {
        const sse = new EventSource(PREFIX_API + apiRoutes.LISTEN_CHAT);

        sse.addEventListener('message', event => {
          const eventData = JSON.parse(event.data);
          setData(eventData);
          console.log('Mensagem recebida:', eventData);
          dispatch({ type: 'loading-success', data: eventData });
        });

        sse.onerror = error => {
          console.error('EventSource failed:', error);
          sse.close();
        };
        return () => {
          sse.close();
        };
      } catch (error) {
        if (!cancelled) {
          dispatch({ type: 'loading-error', error: error });
        }
      }
    }
    doFetch();
    return () => {
      console.log('cleanup');
      abort.abort;
      cancelled = true;
    };
  }, [dispatch, location]);

  switch (state.type) {
    case 'begin':
      return <p>Idel</p>;
    case 'loading':
      return <p>No notifications...</p>;
    case 'error':
      return <ProblemComponent problem={state.error} />;
    case 'loaded':
      return (
        <div>
          <h1>Notifications</h1>
          <p>ID: {data.id}</p>
          <p>Message ID: {data.messageId}</p>
          <p>Channel ID: {data.channelId}</p>
          <p>Username: {data.username}</p>
          <p>Message: {data.msg}</p>
          <p>CreateAt: {formatDate(data.created)}</p>
        </div>
      );
  }
}
