import React, { useEffect, useReducer } from 'react';
import { MessageListOutputModel } from '../../services/messages/models/MessageListOutputModel';
import { Message } from './Messages';
import { useLocation } from 'react-router-dom';
import { getChannelMessages } from '../../services/messages/MessagesService';
import { Problem, ProblemComponent } from '../../services/media/Problem';

type State =
  | { type: 'start' }
  | { type: 'loading' }
  | { type: 'success'; rsp: MessageListOutputModel }
  | { type: 'error'; error: Problem };

type Action =
  | { type: 'started-loading' }
  | { type: 'success'; rsp: MessageListOutputModel }
  | { type: 'error'; error: Problem }
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

export function MessageList() {
  const [state, dispatch] = useReducer(reducer, firstState);
  const location = useLocation();
  const { channel } = location.state || {};

  useEffect(() => {
    const abort = new AbortController();
    let cancelled = false;
    async function doFetch() {
      dispatch({ type: 'started-loading' });
      try {
        const resp = await getChannelMessages(channel.id);
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
      return <ProblemComponent problem={state.error}/>
    case 'success': {
      return (
        <div>
          <h1>{channel.name}</h1>
          <ul className="message-list">
            {state.rsp.messages.length === 0 ? (
              <div>
                <p>This channel is waiting for your voice!</p>
                <p>Send a message and start the conversation.</p>
              </div>
            ) : (
              state.rsp.messages.map(message => <Message key={message.id} message={message} />)
            )}
          </ul>
        </div>
      );
    }
  }
}
