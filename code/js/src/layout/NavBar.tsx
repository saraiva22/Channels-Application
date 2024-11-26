import React from 'react';
import { Link, Outlet } from 'react-router-dom';
import { isLoggedIn } from '../components/authentication/RequireAuthentication';
import { webRoutes } from '../App';
import './NavBar.css';

function NavBar() {
  const loggedIn = isLoggedIn();
  console.log(loggedIn);

  return (
    <nav className="navStyle">
      <ul className="ulStyle">
        <li className="liStyle">
          <Link className="linkStyle" to={webRoutes.home}>
            Home
          </Link>
        </li>
        <li className="liStyle">
          <Link className="linkStyle" to={webRoutes.about}>
            About
          </Link>
        </li>
        {loggedIn ? (
          <>
            <li className="liStyle">
              <Link className="linkStyle" to={webRoutes.channels}>
                Channel
              </Link>
            </li>
            <li className="liStyle">
              <Link className="linkStyle" to={webRoutes.logout}>
                Logout
              </Link>
            </li>
          </>
        ) : (
          <>
            <li className="liStyle">
              <Link className="linkStyle" to={webRoutes.login}>
                Login
              </Link>
            </li>
            <li className="liStyle">
              <Link className="linkStyle" to={webRoutes.register}>
                Register
              </Link>
            </li>
          </>
        )}
      </ul>
    </nav>
  );
}

export function LayoutWithNavBar() {
  return (
    <div>
      <NavBar />
      <Outlet />
    </div>
  );
}
