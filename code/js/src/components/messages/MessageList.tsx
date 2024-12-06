import React, { useEffect, useReducer } from 'react';
import { MessageListOutputModel } from '../../services/messages/models/MessageListOutputModel';
import { Link, useLocation } from 'react-router-dom';
import { getChannelMessages } from '../../services/messages/MessagesService';
import { Problem, ProblemComponent } from '../../services/media/Problem';
import { MessageGroup } from './MessageGroup';
import { webRoutes } from '../../App';
import './MessageList.css';

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

function groupMessagesByUser(messages) {
  const grouped = [];
  let currentGroup = null;

  messages.forEach(message => {
    if (!currentGroup || currentGroup.user.id !== message.user.id) {
      if (currentGroup) grouped.push(currentGroup);
      currentGroup = { user: message.user, messages: [] };
    }
    currentGroup.messages.push(message);
  });

  if (currentGroup) grouped.push(currentGroup);

  return grouped;
}

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
      return <ProblemComponent problem={state.error} />;
    case 'success': {
      return (
        <div>
          <h1>
            <Link to={webRoutes.channel}>{channel.name}</Link>
          </h1>
          <ul className="message-list">
            {state.rsp.messages.length === 0 ? (
              <div>
                <p>This channel is waiting for you!</p>
                <p>Send a message and start the conversation.</p>
              </div>
            ) : (
              groupMessagesByUser(state.rsp.messages).map((group, index) => (
                <MessageGroup key={index} user={group.user} messages={group.messages} />
              ))
            )}
          </ul>
        </div>
      );
    }
  }
}
