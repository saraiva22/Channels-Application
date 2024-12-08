import React, { useEffect, useReducer } from 'react';
import { useLocation } from 'react-router-dom';
import { PrivateListInviteOutputModel } from '../../services/channels/models/PrivateListInviteOutputModel';
import { Problem, ProblemComponent } from '../../services/media/Problem';
import { getSentChannelInvites } from '../../services/channels/ChannelsServices';
import { Invite } from './Invite';
import './css/InviteList.css';

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

function unexpectedAction(state: State, action: Action): State {
  console.log(`unexpected action ${action.type} on state ${state.type}`);
  return state;
}

function reducer(state: State, action: Action): State {
  console.log(`handling ${action.type} on ${state.type}`);
  switch (action.type) {
    case 'started-loading':
      return { type: 'loading' };
    case 'loading-success':
      if (state.type !== 'loading') {
        return unexpectedAction(state, action);
      }
      return { ...state, type: 'loaded', data: action.data };
    case 'loading-error':
      if (state.type !== 'loading') {
        return unexpectedAction(state, action);
      }
      return { ...state, type: 'error', error: action.error };
    case 'cancel':
      return { type: 'begin' };
  }
}

const firstState: State = { type: 'begin' };
export function SendInvites() {
  const [state, dispatch] = useReducer(reducer, firstState);
  const location = useLocation();

  useEffect(() => {
    const abort = new AbortController();
    let cancelled = false;
    async function doFetch() {
      dispatch({ type: 'started-loading' });
      try {
        const result = await getSentChannelInvites();
        console.log(cancelled);
        if (!cancelled) {
          dispatch({ type: 'loading-success', data: result });
        }
      } catch (error) {
        if (!cancelled) {
          dispatch({ type: 'loading-error', error: error });
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
    case 'begin':
      return <p>Idel</p>;
    case 'loading':
      return <p>loading...</p>;
    case 'error':
      return <ProblemComponent problem={state.error} />;
    case 'loaded':
      return (
        <div>
          <h1>Send Channel Invites</h1>
          <ul className="invites-list">
            {state.data.invites.length === 0 ? (
              <div>
                <p>No invites sent</p>
              </div>
            ) : (
              state.data.invites.map(invite => <Invite key={invite.codHash} value={invite} isReceived={false} />)
            )}
          </ul>
        </div>
      );
  }
}
