import React, { useEffect, useReducer, useState } from 'react';
import { MessageListOutputModel } from '../../services/messages/models/MessageListOutputModel';
import { useNavigate } from 'react-router-dom';
import { deleteMessage, getChannelMessages } from '../../services/messages/MessagesService';
import { Problem, ProblemComponent } from '../../services/media/Problem';
import { webRoutes } from '../../App';
import { useChannel } from '../channels/ChannelProvider';
import { Message } from '../../domain/messages/Message';
import { useAuthentication } from '../authentication/AuthProvider';
import './css/MessageList.css';
import './css/MessageGroup.css';
import { UserInfo } from '../../domain/users/UserInfo';
import { ChannelOutputModel } from '../../services/channels/models/ChannelOutputModel';
import { getChannelById } from '../../services/channels/ChannelsServices';

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

type MessageGroupProps = {
  user: UserInfo;
  messagesList: Array<Message>;
};

function groupMessagesByUser(messages: Array<Message>): Array<MessageGroupProps> {
  const grouped: Array<MessageGroupProps> = [];
  let currentGroup: MessageGroupProps = null;

  messages.forEach(message => {
    if (!currentGroup || currentGroup.user.id !== message.user.id) {
      if (currentGroup) grouped.push(currentGroup);
      currentGroup = { user: message.user, messagesList: [] };
    }
    currentGroup.messagesList.push(message);
  });

  if (currentGroup) grouped.push(currentGroup);

  return grouped;
}

export function MessageList() {
  const [state, dispatch] = useReducer(reducer, firstState);
  const { selectedChannel } = useChannel();
  const [username] = useAuthentication();
  const [channelState, setChannelState] = useState<ChannelOutputModel>(selectedChannel);

  if (!selectedChannel) {
    return <p>No channel selected</p>; // redirect !!!!
  }

  useEffect(() => {
    const abort = new AbortController();
    let cancelled = false;
    async function doFetch() {
      dispatch({ type: 'started-loading' });
      try {
        const resp = await getChannelMessages(selectedChannel.id);
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
  }, [dispatch, channelState]);

  const navigate = useNavigate();

  function handleClick() {
    const route = webRoutes.channel;
    navigate(route, { replace: true });
  }

  async function handleOnClickDelete(channelId: number, messageId: number) {
    try {
      await deleteMessage(channelId, messageId);
      const channel = await getChannelById(channelId);
      if (channel) {
        setChannelState(channel);
      }
    } catch (error) {
      console.log(error); // melhorar
    }
  }

  const formatDate = (date: string) => {
    const specificDate = new Date(date);
    const day = String(specificDate.getDate()).padStart(2, '0');
    const month = String(specificDate.getMonth() + 1).padStart(2, '0');
    const year = specificDate.getFullYear();
    const hours = String(specificDate.getHours()).padStart(2, '0');
    const minutes = String(specificDate.getMinutes()).padStart(2, '0');
    return `${day}/${month}/${year} at ${hours}:${minutes}`;
  };

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
          <div className="clickable-title" onClick={handleClick}>
            {selectedChannel.name}
          </div>
          <ul className="message-list">
            {state.rsp.messages.length === 0 ? (
              <div>
                <p>This channel is waiting for you!</p>
                <p>Send a message and start the conversation.</p>
              </div>
            ) : (
              groupMessagesByUser(state.rsp.messages).map(group => (
                <div className="message-group">
                  <small className="message-group small">
                    <b>{group.user.username}</b>
                  </small>
                  {group.messagesList.map(message => (
                    <p key={message.id}>
                      <small className="message-group p small">{formatDate(message.created)}</small>
                      {message.text}
                      {(username === message.user.username || username === message.channel.owner.username) && (
                        <button
                          className="delete-button"
                          onClick={() => handleOnClickDelete(message.channel.id, message.id)}
                        >
                          Delete
                        </button>
                      )}
                    </p>
                  ))}
                </div>
              ))
            )}
          </ul>
        </div>
      );
    }
  }
}
