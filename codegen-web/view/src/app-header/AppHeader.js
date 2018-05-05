/**
 * Created by Nicholas Azar on 2017-06-30.
 */

import React, {Component} from 'react';
import {Layout, Menu, Icon} from 'antd';

const {Header} = Layout;

class AppHeader extends Component {

    render() {
        return (
            <Header className="header">
                <div className="logo">LIGHT</div>
                <Menu theme="dark" mode="horizontal" defaultSelectedKeys={['1']}>
                    <Menu.Item key="2" style={{ float: "right", marginTop: "6px"}}><Icon type="github" style={{fontSize: 30, verticalAlign: 'middle'}} onClick={AppHeader.onGithubIconClick}/></Menu.Item>
                </Menu>
            </Header>
        )
    }

    static onGithubIconClick() {
        window.location = 'https://github.com/networknt/light-codegen';
    }


}

export default AppHeader;