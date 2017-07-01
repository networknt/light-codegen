import React, {Component} from 'react';
import {Layout, LocaleProvider} from 'antd';
import './App.less';
import enUs from 'antd/lib/locale-provider/en_US'
import AppHeader from "./app-header/AppHeader";
import AppContent from "./app-content/AppContent";
import AppFooter from "./app-footer/AppFooter";



class App extends Component {
    render() {
        return (
            <LocaleProvider locale={enUs}>
                <Layout>
                    <AppHeader/>
                    <AppContent/>
                    <AppFooter/>
                </Layout>
            </LocaleProvider>
        );
    }
}

export default App;
