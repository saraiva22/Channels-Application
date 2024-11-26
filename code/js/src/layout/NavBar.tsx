import React from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { isLoggedIn } from '../components/authentication/RequireAuthentication';
import { webRoutes } from '../App';
import { logout } from '../services/users/UserServices';
import './NavBar.css';

function NavBar() {
  const loggedIn = isLoggedIn();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate(webRoutes.home);
  };

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
              <button className="bStyle" onClick={handleLogout}>
                Logout
              </button>
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
