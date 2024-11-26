import React, { useReducer } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { register } from '../../services/users/UserServices';

type State =
  | {
      tag: 'editing';
      error?: string;
      inputs: {
        username: string;
        email: string;
        password: string;
        confirmPassword: string;
        inviteCode: string;
      };
    }
  | { tag: 'submitting'; username: string; email: string; password: string; inviteCode: string }
  | { tag: 'redirect' };

type Action =
  | { type: 'edit'; inputName: string; inputValue: string }
  | { type: 'submit' }
  | { type: 'error'; message: string }
  | { type: 'success' };

function logUnexpectedAction(state: State, action: Action) {
  console.log(`Unexpected action '${action.type}' on state '${state.tag}'`);
}

function reduce(state: State, action: Action): State {
  switch (state.tag) {
    case 'editing':
      if (action.type === 'edit') {
        return { tag: 'editing', error: undefined, inputs: { ...state.inputs, [action.inputName]: action.inputValue } };
      } else if (action.type === 'submit') {
        const password = state.inputs.password;
        const confirmPassword = state.inputs.confirmPassword;
        if (password !== confirmPassword) {
          return { tag: 'editing', error: "Passwords don't match", inputs: state.inputs };
        } else {
          return {
            tag: 'submitting',
            username: state.inputs.username,
            email: state.inputs.email,
            password: password,
            inviteCode: state.inputs.inviteCode,
          };
        }
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'submitting':
      if (action.type === 'success') {
        return { tag: 'redirect' };
      } else if (action.type === 'error') {
        return {
          tag: 'editing',
          error: action.message,
          inputs: { username: state.username, email: state.email, password: '', confirmPassword: '', inviteCode: '' },
        };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'redirect':
      logUnexpectedAction(state, action);
      return state;
  }
}

export function Register() {
  const [state, dispatch] = useReducer(reduce, {
    tag: 'editing',
    inputs: { username: '', email: '', password: '', confirmPassword: '', inviteCode: '' },
  });
  const location = useLocation();

  if (state.tag === 'redirect') {
    return <Navigate to={location.state?.source || '/login'} replace={true} />;
  }

  function handleChange(ev: React.FormEvent<HTMLInputElement>) {
    dispatch({ type: 'edit', inputName: ev.currentTarget.name, inputValue: ev.currentTarget.value });
  }

  async function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
    ev.preventDefault();
    if (state.tag !== 'editing') {
      return;
    }

    dispatch({ type: 'submit' });
    const username = state.inputs.username;
    const email = state.inputs.email;
    const password = state.inputs.password;
    const inviteCode = state.inputs.inviteCode;

    try {
      const result = await register(username, email, password, inviteCode);
      if (!result) {
        dispatch({ type: 'error', message: 'invalid' });
        return;
      }
      dispatch({ type: 'success' });
    } catch (error) {
      dispatch({ type: 'error', message: 'operation could not be completed' });
    }
  }

  const { username, email, password, confirmPassword, inviteCode } =
    state.tag === 'submitting'
      ? { username: state.username, email: state.email, password: '', confirmPassword: '', inviteCode: '' }
      : state.inputs;

  return (
    <form onSubmit={handleSubmit}>
      <fieldset disabled={state.tag !== 'editing'}>
        <div>
          <label htmlFor="username">Username</label>
          <input id="username" type="text" name="username" value={username} onChange={handleChange} />
        </div>
        <div>
          <label htmlFor="email">Email</label>
          <input id="email" type="text" name="email" value={email} onChange={handleChange} />
        </div>
        <div>
          <label htmlFor="password">Password</label>
          <input id="password" type="text" name="password" value={password} onChange={handleChange} />
        </div>
        <div>
          <label htmlFor="confirmPassword">Confirm Password</label>
          <input
            id="confirmPassword"
            type="text"
            name="confirmPassword"
            value={confirmPassword}
            onChange={handleChange}
          />
        </div>
        <div>
          <label htmlFor="inviteCode">Invite Code</label>
          <input id="inviteCode" type="text" name="inviteCode" value={inviteCode} onChange={handleChange} />
        </div>
        <div>
          <button type="submit">Register</button>
        </div>
      </fieldset>
      {state.tag === 'editing' && state.error}
    </form>
  );
}
