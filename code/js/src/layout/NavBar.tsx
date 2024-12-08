import React, { useContext } from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { isLoggedIn } from '../components/authentication/RequireAuthentication';
import { logout } from '../services/users/UserServices';
import { webRoutes } from '../App';
import './NavBar.css';
import { useAuthentication } from '../context/AuthProvider';
import { closeSSE } from '../components/notifications/SSEManager';

function NavBar() {
  const loggedIn = isLoggedIn();
  const navigate = useNavigate();
  const [username, setUsername] = useAuthentication();
  console.log(`Username : ${username}`);

  const handleLogout = async () => {
    await logout();
    setUsername(undefined);
    closeSSE();
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
        {loggedIn ? (
          <>
            <li className="liStyle">
              <Link to={webRoutes.channelCreate} className="linkStyle">
                Create Channel
              </Link>
            </li>
            <li className="liStyle">
              <Link to={webRoutes.publicChannels} className="linkStyle">
                Public Channels
              </Link>
            </li>
            <li className="liStyle">
              <Link to={webRoutes.searchChannels} className="linkStyle">
                Search Channels
              </Link>
            </li>
            <li className="liStyle">
              <Link to={webRoutes.channelsMembers} className="linkStyle">
                Member Channels
              </Link>
            </li>
            <li className="liStyle">
              <Link to={webRoutes.channelsOwned} className="linkStyle">
                Owned Channels
              </Link>
            </li>
            <li className="liStyle">
              <Link to={webRoutes.receivedChannelInvites} className="linkStyle">
                My Received Invites
              </Link>
            </li>
            <li className="liStyle">
              <Link to={webRoutes.sentChannelInvites} className="linkStyle">
                My Sent Invites
              </Link>
            </li>
            <li className="liStyle">
              <Link to={webRoutes.me} className="linkStyle">
                {username}
              </Link>
            </li>
            <li className="liStyle">
              <Link to={webRoutes.about} className="linkStyle">
                About
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
              <Link to={webRoutes.about} className="linkStyle">
                About
              </Link>
            </li>
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
