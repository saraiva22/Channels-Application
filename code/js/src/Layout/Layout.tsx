import React from 'react';
import { Link, Outlet } from 'react-router-dom';

export function Layout() {
  return (
    <>
      <h2>Layout</h2>
      <ul>
        <li>
          <Link to="/">Home</Link>
        </li>
        <li>
          <Link to="/login">Login</Link>
        </li>
        <li>
          <Link to="/register">Register</Link>
        </li>
      </ul>
      <Outlet />
    </>
  );
}
