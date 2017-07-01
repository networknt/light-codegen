/**
 * Created by Nicholas Azar on 2017-06-30.
 */

import React, {Component} from 'react';
import {Layout, Menu} from 'antd';

const {Header} = Layout;

class AppHeader extends Component {
    render() {
        return (
            <Header className="header">
                <div className="logo">Light-4j</div>
                <Menu theme="dark" mode="horizontal" defaultSelectedKeys={['1']}>
                    <Menu.Item key="1" style={{ marginTop: "6px"}}>Codegen</Menu.Item>
                    <Menu.Item key="2" style={{ float: "right", marginTop: "6px"}}>github</Menu.Item>
                </Menu>
            </Header>
        )
    }
}

export default AppHeader;