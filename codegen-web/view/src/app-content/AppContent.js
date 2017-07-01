/**
 * Created by Nicholas Azar on 2017-07-01.
 */

import React, {Component} from 'react';
import {Layout} from 'antd';
import AppSteps from "./app-steps/AppSteps";

const {Content} = Layout;

class AppContent extends Component {
    render() {
        return (
            <Content className="root-content">
                <AppSteps currentStep={0}/>
                <Layout className="primary-paper">
                    <Content style={{ padding: '0 24px', minHeight: 280 }}>
                        Content
                    </Content>
                </Layout>
            </Content>
        )
    }
}

export default AppContent;