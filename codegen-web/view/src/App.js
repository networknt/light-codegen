import React, { Component } from 'react';
import {Switch, Route} from 'react-router-dom';
import { Router } from 'react-router';
import { createBrowserHistory } from 'history';
import Home from './components/Home';
import Document from './components/Document';
import GitHub from './components/GitHub';
import ResponsiveDrawer from './components/ResponsiveDrawer';
import Form from './components/Form';

export const history = createBrowserHistory();

class App extends Component {
  render() {
    return (
        <Router history={history}>
          <ResponsiveDrawer>
            <Switch>
              <Route exact path="/" component={Home} />
              <Route exact path="/document" component={Document} />
              <Route exact path="/github" component={GitHub} />
              <Route path="/form/:formId" component={Form} />
            </Switch>
          </ResponsiveDrawer>
        </Router>
    );
  }
}

export default App;
