import React, { useReducer } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { Status } from '../../domain/channels/Status';
import { webRoutes } from '../../App';
import { validateChannelInvite } from '../../services/channels/ChannelsServices';
import { Problem, ProblemComponent } from '../../services/media/Problem';
import { error } from 'console';

type State =
  | { type: 'editing'; error?: Problem; status: Status }
  | { type: 'submitting'; status: Status }
  | { type: 'redirect' };

type Action =
  | { type: 'edit'; inputStatus: string }
  | { type: 'submit' }
  | { type: 'error'; error: Problem }
  | { type: 'success' };

function logUnexpectedAction(state: State, action: Action) {
  console.log(`Unexpected action '${action.type} on state '${state.type}'`);
}

function reduce(state: State, action: Action): State {
  switch (state.type) {
    case 'editing':
      if (action.type === 'edit') {
        const statusValue = action.inputStatus === Status.ACCEPT.toString() ? Status.ACCEPT : Status.REJECT;
        return { type: 'editing', error: undefined, status: statusValue };
      } else if (action.type === 'submit') {
        return { type: 'submitting', status: state.status };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }
    case 'submitting':
      if (action.type === 'success') {
        return { type: 'redirect' };
      } else if (action.type === 'error') {
        return { type: 'editing', error: action.error, status: state.status };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }
    case 'redirect':
      logUnexpectedAction(state, action);
      return state;
  }
}

const firstState: State = { type: 'editing', status: Status.PENDING };
export function ValidateChannelInvite() {
  const [state, dispatch] = useReducer(reduce, firstState);
  const location = useLocation();
  const { valuesNav } = location.state || {};
  console.log(valuesNav.channelId);
  console.log(state.type);

  if (state.type === 'redirect') {
    console.log(state);

    return <Navigate to={location.state?.source || webRoutes.channelsMembers} replace={true} />;
  }

  function handleChange(ev: React.FormEvent<HTMLInputElement>) {
    dispatch({ type: 'edit', inputStatus: ev.currentTarget.value });
  }

  async function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
    const abort = new AbortController();
    let cancelled = false;
    ev.preventDefault();
    if (state.type !== 'editing') {
      return;
    }
    dispatch({ type: 'submit' });
    try {
      const result = await validateChannelInvite(valuesNav.channelId, valuesNav.cod, state.status);
      if (result) {
        dispatch({ type: 'success' });
      }
    } catch (error) {
      dispatch({ type: 'error', error: error });
    }
  }

  const status = state.type === 'submitting' ? '' : state.status;

  return (
    <div>
      <form onSubmit={handleSubmit}>
        <fieldset disabled={state.type !== 'editing'}>
          <h1>Validation Channel Invite</h1>
          <div>
            <div>
              <label>
                <input
                  type="radio"
                  name="status"
                  value={Status.ACCEPT}
                  checked={status === Status.ACCEPT}
                  onChange={handleChange}
                />
                Accept
              </label>
              <label>
                <input
                  type="radio"
                  name="status"
                  value={Status.REJECT}
                  checked={status === Status.REJECT}
                  onChange={handleChange}
                />
                Reject
              </label>
            </div>
          </div>
          <div>
            <button type="submit">Validate Invitate</button>
          </div>
        </fieldset>
      </form>
      {state.type === 'editing' && state.error && (
        <div className="error-alert">
          <span className="error-icon">❗</span>
          <span className="error-message">{typeof state.error === 'string' ? state.error : state.error.detail}</span>
        </div>
      )}
    </div>
  );
}
