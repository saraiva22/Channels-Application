import React, { useEffect, useReducer, useState } from 'react';
import { MessageListOutputModel } from '../../services/messages/models/MessageListOutputModel';
import { useNavigate } from 'react-router-dom';
import { deleteMessage, getChannelMessages } from '../../services/messages/MessagesService';
import { Problem, ProblemComponent } from '../../services/media/Problem';
import { webRoutes } from '../../App';
import { useChannel } from '../../context/ChannelProvider';
import { Message } from '../../domain/messages/Message';
import { useAuthentication } from '../../context/AuthProvider';
import './css/MessageList.css';
import { UserInfo } from '../../domain/users/UserInfo';
import { ChannelOutputModel } from '../../services/channels/models/ChannelOutputModel';
import { getChannelById } from '../../services/channels/ChannelsServices';
import { MessageCreate } from './MessageCreate';
import { MessageGroup } from './MessageGroup';
import { getSSE } from '../notifications/SSEManager';

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
  const [channelState, setChannelState] = useState<ChannelOutputModel>(selectedChannel);
  const [messages, setMessages] = useState<Array<Message>>([]);

  if (!selectedChannel) {
    return <p>No channel selected</p>;
  }

  useEffect(() => {
    const abort = new AbortController();
    let cancelled = false;
    async function doFetch() {
      dispatch({ type: 'started-loading' });
      try {
        const resp = await getChannelMessages(selectedChannel.id);
        if (!cancelled) {
          dispatch({ type: 'success', rsp: resp });
          setMessages(resp.messages);
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

  useEffect(() => {
    const eventSource = getSSE();

    if (eventSource) {
      eventSource.addEventListener('message', event => {
        const eventData = JSON.parse(event.data);

        setMessages(prevMessages => [...messages, eventData]);
        eventSource.removeEventListener('message', eventData);
      });
    }
  }, [messages]);

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

  switch (state.type) {
    case 'start':
      return <p>Idle</p>;
    case 'loading':
      return <p>loading...</p>;
    case 'error':
      return <ProblemComponent problem={state.error} />;
    case 'success': {
      const groupedMessages = groupMessagesByUser(messages);

      return (
        <div>
          <div className="clickable-title" onClick={handleClick}>
            {selectedChannel.name}
          </div>
          <ul className="message-list">
            {groupedMessages.length === 0 ? (
              <div>
                <p>This channel is waiting for you!</p>
                <p>Send a message and start the conversation.</p>
              </div>
            ) : (
              groupedMessages.map(group => (
                <MessageGroup
                  key={group.user.id}
                  groupUser={group.user}
                  messagesList={group.messagesList}
                  onDeleteMessage={handleOnClickDelete}
                />
              ))
            )}
          </ul>

          <MessageCreate onMessageCreated={() => setMessages(prev => [...prev])} />
        </div>
      );
    }
  }
}
