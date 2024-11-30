import React from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { isLoggedIn } from '../components/authentication/RequireAuthentication';
import { logout } from '../services/users/UserServices';
import './NavBar.css';
import { webRoutes } from '../services/utils/HttpService';

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
          <Link to={webRoutes.home} className="linkStyle">
            Home
          </Link>
        </li>
        <li className="liStyle">
          <Link to={webRoutes.about} className="linkStyle">
            About
          </Link>
        </li>
        {loggedIn ? (
          <>
            <li className="liStyle">
              <Link to={webRoutes.channelsMembers} className="linkStyle">
                Member Channel
              </Link>
            </li>
            <li className="liStyle">
              <Link to={webRoutes.channelsOwned} className="linkStyle">
                Owned Channel
              </Link>
            </li>
            <li className="liStyle">
              <button className="bStyle" onClick={handleLogout}>
                Logout
              </button>
            </li>
            <li className="liStyle">
              <Link to={webRoutes.notifications} className="linkStyle">
                Notifications
              </Link>
            </li>
          </>
        ) : (
          <>
            <li className="liStyle">
              <Link to={webRoutes.login} className="linkStyle">
                Login
              </Link>
            </li>
            <li className="liStyle">
              <Link to={webRoutes.register} className="linkStyle">
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
