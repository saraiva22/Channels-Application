import React, { useEffect, useReducer, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { PrivateListInviteOutputModel } from '../../services/channels/models/PrivateListInviteOutputModel';
import { Problem, ProblemComponent } from '../../services/media/Problem';
import { getReceivedChannelInvites } from '../../services/channels/ChannelsServices';
import { Invite } from './Invite';
import './css/InviteList.css';
import { PrivateInviteOutputModel } from '../../services/channels/models/PrivateInviteOutputModel';
import { getSSE } from '../notifications/SSEManager';

type State =
  | { type: 'begin' }
  | { type: 'loading' }
  | { type: 'loaded'; data: PrivateListInviteOutputModel }
  | { type: 'error'; error: Problem };

type Action =
  | { type: 'started-loading' }
  | { type: 'loading-success'; data: PrivateListInviteOutputModel }
  | { type: 'loading-error'; error: Problem }
  | { type: 'cancel' };

function logUnexpectedAction(state: State, action: Action): State {
  console.log(`unexpected action ${action.type} on state ${state.type}`);
  return state;
}

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case 'started-loading':
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

export function ReceivedInvites() {
  const [state, dispatch] = useReducer(reducer, firstState);
  const location = useLocation();
  const [invites, setInvites] = useState<Array<PrivateInviteOutputModel>>([]);
  const eventSource = getSSE();

  useEffect(() => {
    const abort = new AbortController();
    let cancelled = false;
    async function doFetch() {
      dispatch({ type: 'started-loading' });
      try {
        const result = await getReceivedChannelInvites();
        console.log(cancelled);
        if (!cancelled) {
          dispatch({ type: 'loading-success', data: result });
          setInvites(result.invites);
        }
      } catch (error) {
        if (!cancelled) {
          const problem = error as Problem;
          dispatch({ type: 'loading-error', error: problem });
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

  useEffect(() => {
    if (eventSource) {
      const handleInvite = (event: MessageEvent) => {
        const eventData = JSON.parse(event.data);
        const newInvite = {
          codHash: eventData.codHash,
          privacy: eventData.privacy,
          status: eventData.status,
          userInfo: {
            id: eventData.user.id,
            username: eventData.user.username,
            email: eventData.user.email,
          },
          channelId: eventData.channelId,
          channelName: eventData.channelName,
        };
        setInvites(prevInvites => [...prevInvites, newInvite]);
      };
      eventSource.addEventListener('invite', handleInvite);
      return () => {
        eventSource.removeEventListener('invite', handleInvite);
      };
    }
  }, [invites]);

  switch (state.type) {
    case 'begin':
      return <p>Idel</p>;
    case 'loading':
      return <p>loading...</p>;
    case 'error':
      return <ProblemComponent problem={state.error} />;
    case 'loaded':
      return (
        <div>
          <h1>Received Channel Invitations</h1>
          <ul className="invites-list">
            {invites.length === 0 ? (
              <div>
                <p>No invitations received</p>
              </div>
            ) : (
              invites.map(invite => <Invite key={invite.codHash} value={invite} isReceived={true} />)
            )}
          </ul>
        </div>
      );
  }
}
