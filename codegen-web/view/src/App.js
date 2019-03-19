import React, { Component } from 'react';
import Header from './Header';
import Form from './Form';
import './App.css';

class App extends Component {
  render() {
    return (
      <div className="App">
        <Header/>
        <Form/>
      </div>
    );
  }
}

export default App;
